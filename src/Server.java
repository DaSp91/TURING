
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Server {

	private static Registry registry = null; //oggetto remoto server
	final static int port_socket = 2000; //porta socket principale
	final static int port_rmi = 2001; //porta utilizzata per il registro rmi
	static DataBaseUtenti db = new DataBaseUtenti(1024); //database per gli utenti registrati
	static BlockingQueue<Messaggio> codamessaggi = new ArrayBlockingQueue<Messaggio>(1024);//coda condivisa con il threadDispatcher: inserisco e prelevo i messaggi del client
	static BlockingQueue<SocketChannel> crashclient = new ArrayBlockingQueue<>(1024);//coda condivisa con il threadcrash in cui verranno inserite le socket dei client crashati
	static DataBaseDocumenti dbDoc = new DataBaseDocumenti(1024); //database per i documenti
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		try {
			//creazione di un'istanza dell'oggetto ServerInterfaceImpl da esportare come oggetto remoto
			ServerInterfaceImpl remote_server = new ServerInterfaceImpl(); 		
			//esportazione dell'oggetto
			ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(remote_server, 0);
			//Creazione di un registry sulla porta 2001
			LocateRegistry.createRegistry(port_rmi);
			
			registry = LocateRegistry.getRegistry(port_rmi);
			//pubblicazione dello stub nel registry
			registry.rebind("Server", stub);
							
		} catch (RemoteException e) {
			System.out.println("Remote exception");
			System.exit(0);
		} 
		
		ServerSocketChannel server = ServerSocketChannel.open(); //socket principale del server
		server.bind(new InetSocketAddress(port_socket));
		server.configureBlocking(false);//configuro il channel a false per poterlo registrare con un selettore
		
		//apro un selettore:
		//componente che esamina uno o più NIO channels e 
		//determina quali canali sono pronti per leggere/scrivere
		Selector s = Selector.open(); 
		server.register(s, SelectionKey.OP_ACCEPT);
		
		//avvio ThreadDispatcher che gestirà la richiesta
		ThreadDispatcher threadDisp = new ThreadDispatcher();
		Thread thread = new Thread(threadDisp);
		thread.start();
		//avvio il thread che controlla se un client è crashato e nel caso fa endedit e logout
		ThreadCrash threadcrash = new ThreadCrash();
		Thread thread2 = new Thread(threadcrash);
		thread2.start();
		System.out.println("Server avviato..");
		
		while(true) {
			
			int ready = s.select(); //salvo in ready il numero di socket pronte
			if(ready == 0) continue; //se non ho socket pronte rifaccio la select
			
			Set<SelectionKey> readyKeys = s.selectedKeys(); //insieme delle chiavi associate alle socket pronte
			Iterator<SelectionKey> iterator = readyKeys.iterator();//iteratore per scorrere le chiavi
			
			while(iterator.hasNext()) { //rimango nel while fino a quando ho elementi
				
				SelectionKey key = iterator.next(); //ottengo la chiave associata ad una socket
				iterator.remove();
				//key.isAcceptable --> un nuovo client si è connesso(ho terminato il three-way-handshake)
				if(key.isAcceptable()) {
					ServerSocketChannel s1 = (ServerSocketChannel) key.channel(); //prendo la socket
					SocketChannel client = s1.accept(); //accetto la connessione
					client.configureBlocking(false);
					client.register(s, SelectionKey.OP_READ);
					System.out.println("Ho accettato una nuova connessione");//un client tenta di fare il login
					
				}
				//key.isReadable --> posso leggere un messaggio del client
				else if(key.isReadable()) {
					
					//estraggo la socket da cui leggere
					SocketChannel sc = (SocketChannel)key.channel();
					String messaggio = null;
					try{
						messaggio = Messaggio.riceviMessaggio(sc);
					}
					catch(Exception e) {
						System.err.println("Errore di comunicazione con il client, il client verrà disconnesso");
						crashclient.put(sc);	
					}
					finally {
						if(messaggio == null) {
							key.cancel();//cancello la chiave associata alla socket dal selettore
							sc.close();
							continue;
						}
						//inserisco il messaggio nella coda condivisa,
						//inserisco anche la socket cosi posso sapere da quale client proviene il messaggio
						codamessaggi.put(new Messaggio(sc, messaggio));
					}
				}	
			}	
		}		
	}
}

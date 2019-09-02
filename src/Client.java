import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Client {

	static SocketChannel sc; //socket per connettersi l server
	static String username; //username del client
	static int socket_port = 2000; //numero porta server
	static int rmi_port = 2001; //numero porta utilizzata per rmi
	static boolean online = false; //variabile che indica se l' utente è online
	static SocketChannel invitisc; //socket in cui ricevero' gli inviti
	static int portachat; //numero porta utilizzata per la chat 
	static MulticastSocket chatsocket; //socket multicast per la chat
	static int MAX_LENGTH = 512; //lunghezza massima di un messaggio della chat
	static BlockingQueue<DatagramPacket> packets = new ArrayBlockingQueue<>(1024); //coda dei messaggi della chat
	static boolean edit = false; //variabile che indica se l'utente ha eseguito una edit
	
	public static void main(String [] args) throws UnknownHostException, IOException, NotBoundException {
		
		Registry r = LocateRegistry.getRegistry(rmi_port);//recupero il riferimento al registro RMI
		ServerInterface server = (ServerInterface)r.lookup("Server");
		
		if(args.length != 0){
			System.err.println("argomenti non riconosciuti, per avviare il client: java Client ");
		}
		
		System.out.println("Benvenuto in turing!");
		System.out.println("Sono in attesa di un comando, per info: turing --help");
		
		//uso scanner per leggere da tastiera
		Scanner input = new Scanner(System.in);
		String comando = input.nextLine();
		while(true) {
			String [] comandi = comando.split(" ");
			switch(comandi[0]) {
			
			case "turing":{
				if(comandi.length == 2 && comandi[1].equals("--help")) {
					//stampa le istruzioni di uso di turing 
					System.out.println("usage: COMMAND [ARGS ...]\n");
					System.out.println("commands:");
					System.out.println("register <username> <password> registra l'utente"); 
					System.out.println("login <username> <password> effettua il login"); 
					System.out.println("logout <username> <password> effettua il logout"); 
					System.out.println("create <doc > <numsezioni> crea un documento"); 
					System.out.println("share <doc> <username> condivide il documento"); 
					System.out.println("showsec <doc> <sec> mostra una sezione del documento"); 
					System.out.println("showdoc <doc> mostra l'intero documento"); 
					System.out.println("list mostra la lista dei documenti");
					System.out.println("edit <doc> <sec> modifica una sezione del documento"); 
					System.out.println("endedit <doc> <sec> fine modifica della sezione del documento");
					System.out.println("send <msg> invia un messaggio sulla chat");
					System.out.println("receive visualizza i messaggi ricevuti sulla chat");
					System.out.println("exit termina il client");
				}
				else System.err.println("Errore negli argomenti! per info turing --help");
				break;
			}
			case "register":{
				if (online) {
					System.err.println("Impossibile registrare un utente dopo avere fatto il login!");
					break;
				}
				
				if(comandi.length == 3) {
					String username = comandi[1];
					String password = comandi[2];
					boolean ris = server.regUser(username, password);
					if(ris) System.out.println("Registrazione effettuata con successo!");
					else System.err.println("Registrazione fallita! Utente già registrato!");
				}
				else {
					System.err.println("Registrazione fallita! usage: register <username> <password>");
				}
				break;
			}
			case "login":{
				if (online) {
					System.err.println("Login fallito! Accesso gia' effettuato!");
					break;
				}
				if (comandi.length != 3) {
					System.err.println("Login fallito! usage: login <username> <password>");
					break;
				}
				
				sc = SocketChannel.open(new InetSocketAddress(InetAddress.getLocalHost(), socket_port));
				//creo il messaggio da inviare al server
				String mess = Messaggio.creaMessaggio(comandi);
				//invio il messaggio al server
				Messaggio.inviaMessaggio(sc,mess);
				//prelevo la risposta del server e stampo l'esito
				String risposta = Messaggio.riceviMessaggio(sc);
				
				if(risposta.equals("-1")) System.err.println("login fallito! Utente non registrato!");
				
				if(risposta.equals("-2")) System.err.println("login fallito! Password errata!");
				
				if(risposta.equals("-3")) System.err.println("login fallito! Utente gia' online!");
				
				if(risposta.equals("0")) {
					System.out.println("login effettuato con successo!");
					username = comandi[1];//
					online = true;
					int n = Integer.parseInt(Messaggio.riceviMessaggio(sc)); //ricevo il numero di porta della socketinviti
					invitisc = SocketChannel.open(new InetSocketAddress(InetAddress.getLocalHost(), n));
					//thread anonimo per stampare la notifica di invito
					Runnable run = new Runnable() {
						public void run() {
							try {
								invitisc.configureBlocking(true);
							} catch (IOException e) {
								e.printStackTrace();
							}
							while(true) {
								try {
								String mess = Messaggio.riceviMessaggio(invitisc);
								
								if(mess == null) break;
								System.out.println(mess);
								}
								catch(IOException e){
									System.err.println("Socket degli inviti chiusa");
									break;
								}
							}
						}
					};
					new Thread(run).start();
				}
				break;
			}
			case "logout":{
				if (!online) {
					System.err.println("Logout fallito! Occorre fare il login prima del logout!");
					break;
				}
				if (comandi.length != 3) {
					System.err.println("Logout fallito! usage: logout <username> <password>");
					break;
				}
				if(edit) {
					System.err.println("edit in corso! comando logout non disponibile");
					break;
				}
				String mess = Messaggio.creaMessaggio(comandi);
				Messaggio.inviaMessaggio(sc, mess);
				String risposta = Messaggio.riceviMessaggio(sc);
				
				if(risposta.equals("-1")) System.err.println("logout fallito! Utente non registrato!");
				
				if(risposta.equals("-2")) System.err.println("logout fallito! Password errata!");
				
				if(risposta.equals("-3")) System.err.println("logout fallito! Utente offline!");
				
				if(risposta.equals("-4")) System.err.println("logout fallito!");
				
				if(risposta.equals("0")) {
					System.out.println("logout effettuato con successo!");
					sc.close();
					invitisc.close();
					online = false;
				}
				break;	 
			}
			case "create":{
				if(!online) {
					System.err.println("create fallita! Occorre fare il login prima di poter creare un documento!");
					break;
				}
				if(comandi.length != 3 ) {
					System.err.println("Create fallita! usage: create <nomedoc> <numsezioni>");
					break;
				}
				//controllo che il secondo argomento passato sia un intero
				try{
					Integer.parseInt(comandi[2]);
				}catch(NumberFormatException e){
					System.err.println("Create fallita! usage: create <nomedoc> <numsezioni> (numsezioni deve essere un intero!)");
					break;
				}
				if(edit) {
					System.err.println("edit in corso! comando create non disponibile");
					break;
				}
				String mess = Messaggio.creaMessaggio(comandi);
				Messaggio.inviaMessaggio(sc, mess);
				String risposta = Messaggio.riceviMessaggio(sc);
				if(risposta.equals("true")) System.out.println("Documento creato con successo!");
				else System.err.println("Create fallita! Documento gia' esistente!");
				break;
			}
			case "list":{
				if(!online) {
					System.err.println("list fallita! Occorre fare il login prima di poter richiedere la lista dei documenti!");
					break;
				}
				if(comandi.length != 1 ) {
					System.err.println("List fallita! usage: list");
					break;
				}
				if(edit) {
					System.err.println("edit in corso! comando list non disponibile");
					break;
				}
				
				String mess = Messaggio.creaMessaggio(comandi);
				Messaggio.inviaMessaggio(sc, mess);
				String risposta = Messaggio.riceviMessaggio(sc);
				System.out.println(risposta);
				break;
			}
			case "edit":{
				if(!online) {
					System.err.println("edit fallita! Occorre fare il login prima di poter fare la edit!");
					break;
				}
				if(comandi.length != 3 ) {
					System.err.println("Edit fallita! usage: edit <nomedoc> <numsezione>");
					break;
				}
				//controllo che il secondo argomento passato sia un intero
				try{
					Integer.parseInt(comandi[2]);
				}catch(NumberFormatException e){
					System.err.println("Edit fallita! usage: edit <nomedoc> <numsezioni> (numsezioni deve essere un intero!)");
					break;
				}
				if(edit) {
					System.err.println("edit in corso! eseguire una endendit prima di fare altre edit");
					break;
				}
				
				String mess = Messaggio.creaMessaggio(comandi);
				Messaggio.inviaMessaggio(sc, mess);
				String risposta = Messaggio.riceviMessaggio(sc);
				if(risposta.equals("true")) {
					portachat = Integer.parseInt(Messaggio.riceviMessaggio(sc));
					File f = new File(username+"_" + comandi[1] + "_" + comandi[2] + ".txt");
					f.createNewFile();
					FileChannel outChannel = FileChannel.open(Paths.get(username+"_"+ comandi[1] + "_" + comandi[2] + ".txt"), StandardOpenOption.WRITE);
					ByteBuffer buffer = Messaggio.riceviFile(sc);
					
					while(buffer.hasRemaining()) {
						outChannel.write(buffer);
					}
					outChannel.close();
					
					//apro la chat
					chatsocket = new MulticastSocket(portachat);
					InetAddress multicastgroup = InetAddress.getByName("239.1.1.1");
					chatsocket.setSoTimeout(100000000);
					chatsocket.joinGroup(multicastgroup);
					
					//avvio un thread che si occupa di ricevere e accodare i messaggi della chat
					Runnable run = new Runnable() {
						public void run() {
							DatagramPacket p = new DatagramPacket(new byte[MAX_LENGTH], MAX_LENGTH);;
							while(true) {
								try {
									chatsocket.receive(p);
									packets.add(p);
									p = new DatagramPacket(new byte[MAX_LENGTH], MAX_LENGTH);
								} catch (IOException e) {
									System.out.println("Socket chat chiusa");
									break;
								}
							}
						}
					};
					new Thread(run).start();
					edit = true;
					System.out.println("edit eseguita con successo!");
					break;
				}
				else System.err.println("edit fallita! " +risposta);
				break;
			}
			case "endedit":{
				if(!online) {
					System.err.println("endedit fallita! Occorre fare il login prima di poter fare la endedit!");
					break;
				}
				if(comandi.length != 3 ) {
					System.err.println("endedit fallita! usage: endedit <nomedoc> <numsezione>");
					break;
				}
				//controllo che il secondo argomento passato sia un intero
				try{
					Integer.parseInt(comandi[2]);
				}catch(NumberFormatException e){
					System.err.println("endedit fallita! usage: endedit <nomedoc> <numsezioni> (numsezioni deve essere un intero!)");
					break;
				}
				if(!edit) {
					System.err.println("Non è possibile fare la endedit senza prima aver fatto la edit!");
					break;
				}
				String mess = Messaggio.creaMessaggio(comandi);
				Messaggio.inviaMessaggio(sc, mess);
				String risposta = Messaggio.riceviMessaggio(sc);
				
				if(!risposta.equals("false")) { //se posso fare la endedit invio il file
					int numPort = Integer.parseInt(risposta);
					SocketChannel filesocket = SocketChannel.open(new InetSocketAddress(InetAddress.getLocalHost(), numPort));
					filesocket.configureBlocking(true);
					Messaggio.inviaFile(filesocket,username+"_"+ comandi[1] + "_" + comandi[2] + ".txt");
					risposta = Messaggio.riceviMessaggio(sc);
					if(risposta.equals("true")) {
						edit = false;
						chatsocket.close();
						System.out.println("endedit eseguita con successo!");
					}
					
					else System.err.println("endedit fallita!Errore nella ricezione del file!");
				}
				else System.err.println("endedit fallita! Nome documento e/o numero sezione non validi!");
				
				break;
			}
			case "share":{
				if(!online) {
					System.err.println("share fallita! Occorre fare il login prima di poter fare la share!");
					break;
				}
				if(comandi.length != 3 ) {
					System.err.println("share fallita! usage: share <nomedoc> <numsezione>");
					break;
				}
				if(edit) {
					System.err.println("edit in corso! comando share non disponibile");
					break;
				}
				String mess = Messaggio.creaMessaggio(comandi);
				Messaggio.inviaMessaggio(sc, mess);
				String risposta = Messaggio.riceviMessaggio(sc);
				if(!risposta.equals("true")) System.err.println(risposta);
				else System.out.println("share eseguita con successo!");
				break;
			}
			case "showsec":{
				if(!online) {
					System.err.println("showsec fallita! Occorre fare il login prima di poter fare la showsec!");
					break;
				}
				if(comandi.length != 3 ) {
					System.err.println("showsec fallita! usage: showsec <nomedoc> <numsezione>");
					break;
				}
				//controllo che il secondo argomento passato sia un intero
				try{
					Integer.parseInt(comandi[2]);
				}catch(NumberFormatException e){
					System.err.println("showsec fallita! usage:  <nomedoc> <numsezioni> (numsezioni deve essere un intero!)");
					break;
				}
				if(edit) {
					System.err.println("edit in corso! comando showsec non disponibile");
					break;
				}
				String mess = Messaggio.creaMessaggio(comandi);
				Messaggio.inviaMessaggio(sc, mess);
				String risposta = Messaggio.riceviMessaggio(sc);
				if(risposta.equals("true")) {
					String contenutofile = new String(Messaggio.riceviMessaggio(sc));
					System.out.println(contenutofile);
					System.out.println("showsec eseguita con successo!");
					break;
				}
				
				else System.err.println("showsec fallita! " +risposta);
				break;
			}
			case "showdoc":{
				if(!online) {
					System.err.println("showdoc fallita! Occorre fare il login prima di poter fare la showdoc!");
					break;
				}
				if(comandi.length != 2 ) {
					System.err.println("showdoc fallita! usage: showdoc <nomedoc>");
					break;
				}
				if(edit) {
					System.err.println("edit in corso! comando showdoc non disponibile");
					break;
				}
				String mess = Messaggio.creaMessaggio(comandi);
				Messaggio.inviaMessaggio(sc, mess);
				String risposta = Messaggio.riceviMessaggio(sc);
				if(risposta.equals("true")) {
					String contenuto = Messaggio.riceviMessaggio(sc);
					System.out.println(contenuto);
				}
				else System.err.println("showdoc fallita! " +risposta);
				break;
			}
			case "send":{
				if(!online) {
					System.err.println("send fallita! Occorre fare il login prima di poter fare la send!");
					break;
				}
				if(comandi.length < 2 ) {
					System.err.println("send fallita! usage: send msg ");
					break;
				}
				if(!edit) {
					System.err.println("Non è possibile mandare messaggi senza aver fatto la edit!");
					break;
				}
				String messaggio = username + ":";
				for(int i=1;i<comandi.length;i++) {
					messaggio = messaggio+ " " +comandi[i];
				}
				//costruisco il pacchetto da mandare in multicast
				DatagramPacket pacchetto = new DatagramPacket(messaggio.getBytes(), messaggio.getBytes().length,
						InetAddress.getByName("239.1.1.1"), portachat);
				//invio il messaggio nella chat
				chatsocket.send(pacchetto);
				System.out.println("messaggio inviato!");
				break;
			}
			case "receive": {
				if(!online) {
					System.err.println("receive fallita! Occorre fare il login prima di poter fare la receive!");
					break;
				}
				if(!edit) {
					System.err.println("Non è possibile fare la receive senza aver fatto la edit!");
					break;
				}
				int num_messaggi = 0; //variabile che conta il num di messaggi per evitare di stampare sempre alla fine nessun messaggio
				while(true) {
					DatagramPacket p = packets.poll();
					if(p != null) {
						System.out.println(new String(p.getData()));
						num_messaggi++;
					}
					else {
						if(num_messaggi == 0) System.out.println("Nessun messaggio");
						break;
					}
				}
				break;
			}
			case "exit":{
				if(online) {
					System.err.println("exit fallita! Occorre fare il logout prima di poter fare la exit!");
					break;
				}
				System.out.println("Client terminato!");
				input.close();
				System.exit(0);
			}
			default:{
				System.err.println("Comando non riconosciuto! per info turing --help");
				break;
			}
			}
			comando = input.nextLine();
		}

	}
	
	
}

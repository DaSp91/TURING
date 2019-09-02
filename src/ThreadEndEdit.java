import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadEndEdit implements Runnable {

	String nomedoc; //nomedocumento
	int numsez;//numero sezione
	SocketChannel sc;
	static AtomicInteger portNum = new AtomicInteger(2003); //numero di porta per la socket dei file
	

	public ThreadEndEdit(String nomedoc, int numsez, SocketChannel sc) {
		this.nomedoc = nomedoc;
		this.numsez = numsez;
		this.sc = sc;
	}

	@Override
	public void run() {
		
		String user = Server.db.usernameFromSocket(sc); 
		Documento d = Server.dbDoc.getDoc(this.nomedoc);
		if(d==null) {
			Messaggio.inviaMessaggio(sc, "false");//documento non esistente
			return;
		}
		Sezione s = d.getSezione(this.numsez);
		if(s==null) {
			Messaggio.inviaMessaggio(sc, "false");//Sezione non esistente
			return;
		}
		if(s.Edit(user, false)) {//invoco la Edit con false perchè in realtà voglio fare la endedit
			int p = portNum.incrementAndGet(); //numero porta per la socket dei file
			Messaggio.inviaMessaggio(sc, Integer.toString(p));
			try {
				ServerSocketChannel filesocket = ServerSocketChannel.open(); //socket principale del server
				filesocket.bind(new InetSocketAddress(p));
				SocketChannel fileclient = filesocket.accept();
				fileclient.configureBlocking(true);
				ByteBuffer buffer = Messaggio.riceviFile(fileclient); //ricevo il file dal client
				if(buffer == null) Messaggio.inviaMessaggio(sc, "false");
				else { 
					Messaggio.inviaMessaggio(sc, "true");
					FileChannel outChannel = FileChannel.open(Paths.get(nomedoc + "_" + numsez + ".txt"), StandardOpenOption.WRITE); //apro il file in scrittura
					while(buffer.hasRemaining()) {
						outChannel.write(buffer);//salvo il file su disco
					}
				} 
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
		else Messaggio.inviaMessaggio(sc, "false");
		portNum.compareAndSet(15000, 2003); //controlla se numPort è uguale 15000 e se lo è lo riporta a 2003(per evitare di usare numeri infiniti)
	}
	
}


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadLogin implements Runnable {
	
	String username, password;
	SocketChannel sc;
	static AtomicInteger portNum = new AtomicInteger(15001); //numero di porta per la socket degli inviti
	
	public ThreadLogin(String username, String password, SocketChannel sc) {
		this.username = username;
		this.password = password;
		this.sc = sc;
	}

	@Override
	public void run() {
		
		
		int ris = Server.db.loginUser(this.username, this.password, this.sc);
		Messaggio.inviaMessaggio(this.sc, Integer.toString(ris));
		
		int p = portNum.incrementAndGet();
		Messaggio.inviaMessaggio(sc, Integer.toString(p));//mando in numero di porta della socke degli inviti
		ServerSocketChannel socketinviti;
		try {
			socketinviti = ServerSocketChannel.open();
			socketinviti.bind(new InetSocketAddress(p));
			SocketChannel inviti = socketinviti.accept();
			inviti.configureBlocking(true);
			Server.db.setSocketinviti(this.username, inviti);
			String s = null;
			while((s=Server.db.takeInvito(this.username))!=null) {
				Messaggio.inviaMessaggio(inviti, s);
			}
		
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
}

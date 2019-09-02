import java.io.IOException;
import java.nio.channels.SocketChannel;

public class ThreadLogout implements Runnable {

	String username, password;
	SocketChannel sc;
	
	public ThreadLogout(String username, String password, SocketChannel sc) {
		this.username = username;
		this.password = password;
		this.sc = sc;
	}

	@Override
	public void run() {	
		int ris = Server.db.logoutUser(this.username, this.password, this.sc);
		Messaggio.inviaMessaggio(this.sc, Integer.toString(ris));
		if (ris==0) {//controllo se il logout è andato a buon fine e nel caso chiudo la socket
			try {
				this.sc.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

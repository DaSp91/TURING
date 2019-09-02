import java.nio.channels.SocketChannel;

public class ThreadList implements Runnable {

	SocketChannel sc;
	public ThreadList(SocketChannel sc) {
		this.sc = sc;
	}

	@Override
	public void run() {
		String username = Server.db.usernameFromSocket(sc);
		if(username != null) {
			Messaggio.inviaMessaggio(sc, Server.db.getUser(username).getDocumenti());
		}

	}

}

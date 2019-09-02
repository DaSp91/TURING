import java.nio.channels.SocketChannel;

public class ThreadCreate implements Runnable {
	
	String nomedoc; //nomedocumento da creare
	int numsezioni;// numero sezioni
	private SocketChannel sc;//socket
	
	public ThreadCreate(String nomedoc, int numsezioni, SocketChannel sc) {
		this.nomedoc = nomedoc;
		this.numsezioni = numsezioni;
		this.sc = sc;
	}

	@Override
	public void run() {
		String username = Server.db.usernameFromSocket(this.sc);
		
		boolean ris = Server.dbDoc.addDoc(username, this.nomedoc, this.numsezioni);
		Messaggio.inviaMessaggio(sc, Boolean.toString(ris));
		if(ris) {
			Utente u = Server.db.getUser(username);
			u.addDocument(this.nomedoc);//agiungo il documento all'utente u
		}
	}
}
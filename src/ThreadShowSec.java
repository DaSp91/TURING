import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ThreadShowSec implements Runnable {
	
	String nomedoc;
	int numsez;
	SocketChannel sc;
	
	public ThreadShowSec(String nomedoc, int numsez, SocketChannel sc) {
		this.nomedoc = nomedoc;
		this.numsez = numsez;
		this.sc = sc;
	}

	@Override
	public void run() {
		String user = Server.db.usernameFromSocket(sc); 
		Documento d = Server.dbDoc.getDoc(this.nomedoc);
		if(d==null) {
			Messaggio.inviaMessaggio(sc, "Documento non esistente");
			return;
		}
		Sezione s = d.getSezione(this.numsez);
		if(s==null) {
			Messaggio.inviaMessaggio(sc, "Sezione non esistente");
			return;
		}
		if(Server.db.autorizzato(user, this.nomedoc)) {
			Messaggio.inviaMessaggio(sc, "true");
			ByteBuffer buffer = Sezione.leggisezione(s.path);
			String contenutosezione;
			String usermodifica = s.getUser();
			//controllo se la sezione è in edit da qualcuno
			if(usermodifica == null) contenutosezione = new String(buffer.array());
			else contenutosezione = new String(buffer.array()) + "\nSezione in modifica da: " + usermodifica; 
			Messaggio.inviaMessaggio(sc, contenutosezione);
			
		}
		else {
			Messaggio.inviaMessaggio(sc, "Utente non autorizzato ad accedere alla sezione del documento!");
		}

	}

}

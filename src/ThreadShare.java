import java.nio.channels.SocketChannel;


public class ThreadShare implements Runnable {
	
	String nomedoc, invitato;
	SocketChannel sc;
	
	public ThreadShare(String nomedoc, String invitato, SocketChannel sc) {
		this.nomedoc = nomedoc;
		this.invitato = invitato;
		this.sc = sc;
	}

	@Override
	public void run() {
		String user = Server.db.usernameFromSocket(sc);
		Documento doc = Server.dbDoc.getDoc(this.nomedoc);
		if (doc == null) {
			Messaggio.inviaMessaggio(sc, "share fallita! Documento inesistente!");
			return;
		}
		if(doc.proprietario.equals(user)) {
			if(Server.db.getUser(this.invitato) != null) {//controllo che l'utente invitato sia registrato a Turing
				if(doc.autorizzaUtente(this.invitato) == true) {
					Server.db.getUser(this.invitato).addDocument(this.nomedoc); //aggiungo il documento tra quelli accessibili dall'utente invitato
					if(Server.db.isOnline(this.invitato)) { //se l'utente è online gli mando subito la notifica dell'invito
						SocketChannel socketinviti = Server.db.getSocketinviti(this.invitato); //recupero la socket degli inviti dell'utente
						Messaggio.inviaMessaggio(socketinviti, "Sei stato invitato al documento "+this.nomedoc );
					}
					else Server.db.addInvito(this.invitato, "Sei stato invitato al documento "+this.nomedoc);//accodo l'invito nella lista degli inviti dell utente invitato
					Messaggio.inviaMessaggio(sc, "true");
				}
				else Messaggio.inviaMessaggio(sc, "Utente gia' invitato");
				}
			else Messaggio.inviaMessaggio(sc, "share fallita! " + this.invitato + " non e' registrato/a a Turing!");
		}
		else {
			Messaggio.inviaMessaggio(sc, "share fallita! Non sei autorizzato ad effettuare questa operazione");
		}

	}

}

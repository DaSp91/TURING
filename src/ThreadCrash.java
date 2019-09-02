import java.io.IOException;
import java.nio.channels.SocketChannel;

/* ThreadCrash:
 * thread che preleva la socket dalla coda dei client crashati
 * e verifica se il client crashato era in edit e nel caso esegue la endedit.
 * in ogni caso fa il logout
 * 
 */
public class ThreadCrash implements Runnable {

	@Override
	public void run() {
		while(true) {
			try {
				SocketChannel sc = Server.crashclient.take();
				if(sc==null) continue;
				String username = Server.db.usernameFromSocket(sc);
				if(username==null) {
					try {
						sc.close();
						continue;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				Utente u = Server.db.getUser(username);
				
				String docs = u.getDocumenti();
				
				if (docs != null && !docs.equals("[]")) { //se l'utente ha documenti
					String[] documenti = docs.substring(1, docs.length()-1).split(",");//tolgo le quadre e le virgole
					for(int i=0; i<documenti.length; i++) {
						Documento d = Server.dbDoc.getDoc(documenti[i].trim());//trim rimuove gli spazi
						Object[] sezioni = d.getSezioni();
						for(int j=0; j<sezioni.length; j++) {
							if(((Sezione)sezioni[j]).Edit(username, false) == true) { //se l'utente stava editando una sezione di un documento
								System.out.println("Endedit di client crashato");
								break;
							}
						}
					}
				}
				u.setSocket(null);//faccio il logout
				System.out.println("Logout di client crashato");
				Server.db.rimuoviOnline(sc);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}

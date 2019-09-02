import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentSkipListSet;

/*
 * oggetto Utente contenuto nella hashmap dataBase;
 * un oggetto U della classe Utente ha 6 campi:
 * 1) nome utente;
 * 2) password;
 * 3) socket associata all'utente;
 * 4) lista dei documenti accessibili dall'utente
 * 5) lista degli inviti pendenti
 * 6) socket per ricevere gli inviti
 * 
 */
public class Utente implements Comparable<Utente>{

	private String user;
	private String password;
	private SocketChannel sc;
	private ConcurrentSkipListSet<String> documenti;
	private ConcurrentSkipListSet<String> inviti;
	private SocketChannel socketInviti;
	
	public Utente(String user, String password) {
		this.user = user;
		this.password = password;
		this.sc = null;
		this.documenti = new ConcurrentSkipListSet<>();
		this.inviti = new ConcurrentSkipListSet<>();
	}
	
	public String getUser() {
		return this.user;
	}
	
	public String getPassword() {
		return this.password;
	}

	//ridefinisco il metodo equals: due utenti sono uguali se hanno lo stesso nome utente
	public boolean equals(Utente U) {
		return (this.getUser().equals(U.getUser()));
	}
	
	@Override
	/* metodo compareTo, restituisce:
	 * -1, se this.getUser() < U.getUser()
	 *  0, se this.getUser() == U.getUser()
	 *  1, se this.getUser() > U.getUser()
	 * 
	 */
	public int compareTo(Utente U) {
		return (this.getUser().compareTo(U.getUser()));
	}

	public void setSocket(SocketChannel sc) {
		this.sc = sc;	
	}
	
	public SocketChannel getSocket() {
		return this.sc;
	}
	
	public void addDocument(String nomeDoc) {
		this.documenti.add(nomeDoc);
		
	}
	
	public String getDocumenti() {
		return this.documenti.toString();
	}
	
	/* autorizzato:
	 * prende in input una stringa nomeDoc e restituisce:
	 * true, se il documento nomeDoc è nella lista dei documenti dell'utente (in pratica controlla se l'utente ha diritto a "lavorare" su quel documento)
	 * false altrimenti
	 * 
	 */
	public boolean autorizzato(String nomeDoc) {
		return this.documenti.contains(nomeDoc);
	}

	public void setSocketinviti(SocketChannel sc2) {
		this.socketInviti = sc2;
		
	}

	public SocketChannel getSocketinviti() {
		return this.socketInviti;
	}
	
	public void addInvito(String invito) {
		this.inviti.add(invito);
	}

	public String takeInvito() {
		return this.inviti.pollFirst();
	}
}

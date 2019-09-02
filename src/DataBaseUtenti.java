
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;

public class DataBaseUtenti {

	static private ConcurrentHashMap<String,Utente> dataBase; //hashmap degli utenti registrati avente come chiave il nome utente e come valori l'oggetto Utente
	static private ConcurrentHashMap<SocketChannel, String> utentiOnline; //hashmap degli utenti online avente come chiave la socket e come valori il nomeutente
	
	public DataBaseUtenti(int size) {
		dataBase = new ConcurrentHashMap<String, Utente>(size);
		utentiOnline = new ConcurrentHashMap<SocketChannel, String>(size);
	}
	
	/* AddUser:
	 * metodo per aggiungere un Utente nel dataBase, restituisce:
	 * true se ha aggiunto l'utente
	 * false altrimenti
	 * 
	 */
	public boolean AddUser(String user, String password) {
		//uso putIfAbsent in quanto mi garantisce l'atomicità evitando che due client facciano la registrazione contemporaneamente
		if(dataBase.putIfAbsent(user, new Utente(user,password)) == null) return true; 
		else return false;
	}
	
	//restituisce l'oggetto Utente associato all'utente di nome user
	public Utente getUser(String user) {
	
		return dataBase.get(user);
	}
	
	/* loginUser:
	 * prende in input user, password e socket e restituisce un codice che indica l'esito dell'operazione:
	 * -1 -> login fallito, utente non registrato
	 * -2 -> login fallito, password errata
	 * -3 -> login fallito, utente già online
	 * 0  -> login effettuato con successo
	 * 
	 */
	public int loginUser(String user, String password, SocketChannel sc) {
		
		Utente u = dataBase.get(user);
		if(u==null) { //l'utente non è registrato
			return -1;
		}
		else{
			if(!(u.getPassword().equals(password))) return -2;//password errata
			else{
				if(u.getSocket() != null) return -3; //utente gia' online
				else{
					u.setSocket(sc);
					utentiOnline.put(sc, user);
					return 0; //login effettuato
				}
			}
		}
	}
	
	/*
	 * metodo per il logout:
	 * prende in input user, password e socket e restituisce un codice che indica l'esito dell'operazione:
	 * -1 -> logout fallito, utente non registrato
	 * -2 -> logout fallito, password errata
	 * -3 -> logout fallito, utente offline
	 * -4 -> logout fallito, socket diverse -> cerco di fare il logout di un altro utente
	 * 0  -> login effettuato con successo
	 * 
	 */
	public int logoutUser(String user, String password, SocketChannel sc) {
		
		Utente u = dataBase.get(user);
		if(u==null) { //l'utente non è registrato
			return -1;
		}
		else{
			if(!(u.getPassword().equals(password))) return -2; //password errata
			else{
				SocketChannel sock = u.getSocket();
				if(sock == null) return -3; //utente offline
				else{
					if(!sock.equals(sc)) return -4; //logout fallito(cerco di fare il logout di un altro utente)
					else{
						u.setSocket(null);
						utentiOnline.remove(sc);
						return 0;//logout effettuato
					}
				}
			}
		}
	}
	
	/* autorizzato:
	 * prende in input user e nomeDoc e restituisce:
	 * -true , se il documento nomeDoc è presente nella lista dei documenti di user
	 * -false altrimenti
	 * 
	 */
	public boolean autorizzato(String user, String nomeDoc) {
		
		Utente u = this.getUser(user);
		return u.autorizzato(nomeDoc);
	}
	
	//recupera lo username dalla socket
	public String usernameFromSocket(SocketChannel sc) {
		
		return utentiOnline.get(sc);
	}
	
	//verifica se un utente è online
	public boolean isOnline(String user) {
		Utente u = this.getUser(user);
		return u.getSocket()!=null;
	}
	
	
	public void setSocketinviti(String user, SocketChannel sc) {
		Utente u = this.getUser(user);
		u.setSocketinviti(sc);
	}
	
	public SocketChannel getSocketinviti(String user) {
		Utente u = this.getUser(user);
		return u.getSocketinviti();
	}
	
	public void addInvito(String user, String invito) {
		Utente u = this.getUser(user);
		u.addInvito(invito);
	}
	
	public String takeInvito(String user) {
		Utente u = this.getUser(user);
		return u.takeInvito();
	}

	public void rimuoviOnline(SocketChannel sc) {
		utentiOnline.remove(sc);
	}
}

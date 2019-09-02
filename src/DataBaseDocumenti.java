import java.util.concurrent.ConcurrentHashMap;

public class DataBaseDocumenti {

		ConcurrentHashMap<String, Documento> dbDoc; //database in cui vengono memorizzate le coppie <nomeutente, documenti_dell_utente>
		
		public DataBaseDocumenti(int dim) {
			dbDoc = new ConcurrentHashMap<>(dim);
		}
		
		public boolean addDoc(String user, String nomeDoc, int numsez) {
			
			if(!dbDoc.containsKey(nomeDoc)) {
				dbDoc.put(nomeDoc, new Documento(user, nomeDoc, numsez));
				return true;
			}
			else return false;
		}
		
		public Documento getDoc(String nomedoc) {
			return this.dbDoc.get(nomedoc);
		}
		
}


import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Documento {

	String proprietario; //proprietario documento
	String nomeDoc; //nome documento
	int numsez; // numero sezione
	ConcurrentSkipListSet<String> invitati; //lista invitati
	ConcurrentSkipListSet<Sezione> sezioni; //lista sezioni
	AtomicBoolean edit; //variabile atomica per vedere se una sezione del documento è in edit(mi serve per sapere se la chat è gia stata aperta)
	AtomicInteger portachat; //variabile atomica che indica il numero di porta della socket relativa alla chat
	
	public Documento(String proprietario,String nomeDoc, int numsez) {
		this.proprietario = proprietario;
		this.nomeDoc = nomeDoc;
		this.invitati = new ConcurrentSkipListSet<>();
		this.sezioni = new ConcurrentSkipListSet<>();
		this.numsez = numsez;
		this.edit = new AtomicBoolean(false);
		this.portachat = new AtomicInteger(0);
		
		for(int i=1; i<numsez+1; i++) {
			this.sezioni.add(new Sezione(this.nomeDoc,i));
		}
	}
	
	public boolean autorizzaUtente(String user) {
		return this.invitati.add(user);
	}
	
	public boolean equals(Documento doc) {
		return this.nomeDoc.equals(doc.nomeDoc);
	}
	
	public Sezione getSezione(int numsez) {
		
		for(Sezione s: this.sezioni) {
			if(s.num == numsez) return s;
		}
		return null;
	}
	
	public Object[] getSezioni() {
		return this.sezioni.toArray();
	}
	
	
}

import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadEdit implements Runnable {

	String nomedoc;//nomedocumento
	int numsez; //numero sezione
	SocketChannel sc;
	AtomicInteger porta; //numero di porta per la chat da aprire 
	
	public ThreadEdit(String nomedoc, int numsez, SocketChannel sc) {
		this.nomedoc = nomedoc;
		this.numsez = numsez;
		this.sc = sc;
		this.porta = new AtomicInteger(1025);
	}

	@Override
	public void run() {
		String user = Server.db.usernameFromSocket(sc); 
		Documento d = Server.dbDoc.getDoc(this.nomedoc);
		if(d==null) {
			Messaggio.inviaMessaggio(sc, "Documento non esistente!");
			return;
		}
		Sezione s = d.getSezione(this.numsez);
		if(s==null) {
			Messaggio.inviaMessaggio(sc, "Sezione non esistente!");
			return;
		}
		if(Server.db.autorizzato(user, this.nomedoc)) {
			boolean ris = s.Edit(user, true);
			
			if(ris) {//se nessun'altro sta editando la sezione del documento invio il file
				Messaggio.inviaMessaggio(sc, Boolean.toString(ris));
				//se d.edit è true (quindi qualcun altro sta editando un'altra sezione del documento) la chat è gia stata aperta,
				//quindi invio il numero di porta attuale 
				if(d.edit.get()) {
					Messaggio.inviaMessaggio(sc, Integer.toString(d.portachat.get()));
				}
				else { //altrimenti occorre aprire una nuova chat quindi incremento il numero di porta e lo invio
					d.edit.set(true);//setto la variabile edit a true
					d.portachat.set(porta.incrementAndGet());//incremento il numero di porta
					Messaggio.inviaMessaggio(sc, Integer.toString(d.portachat.get()));
				}
				Messaggio.inviaFile(sc,s.path);
				
			}
			else Messaggio.inviaMessaggio(sc, "edit della sezione in corso!");
		}
		else {
			Messaggio.inviaMessaggio(sc, "Utente non autorizzato ad accedere al documento!");
		}
		porta.compareAndSet(15000, 1025); //controlla se numPort è uguale 15000 e se lo è lo riporta a 1025(per evitare di usare numeri infiniti)
	}

}

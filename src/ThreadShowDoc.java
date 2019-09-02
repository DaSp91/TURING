import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class ThreadShowDoc implements Runnable {

	String nomedoc;
	SocketChannel sc;
	public ThreadShowDoc(String nomedoc, SocketChannel sc) {
		this.nomedoc = nomedoc;
		this.sc = sc;
	}

	@Override
	public void run() {
		String user = Server.db.usernameFromSocket(sc);//estraggo il nome utente dalla socket
		Documento d = Server.dbDoc.getDoc(this.nomedoc);//prelevo il documento dal database dei documenti
		if(d==null) {
			Messaggio.inviaMessaggio(sc, "Documento non esistente");
			return;
		}
		Object[] sezioni = d.getSezioni(); //prelevo le sezioni
		if(sezioni==null) {
			Messaggio.inviaMessaggio(sc, "Errore nel recupero delle sezioni");
			return;
		}
		if(Server.db.autorizzato(user, this.nomedoc)) { //controllo se il documento è tra quelli accessibili all'utente
			Messaggio.inviaMessaggio(sc, "true");
			Arrays.sort(sezioni); //ordino le sezioni in ordine alfabetico
			ByteBuffer[] buffers = new ByteBuffer[sezioni.length]; //alloco array di buffer di dimensione numerosezioni in cui inseriro' le sezioni lette
			int sizetotale = 0;
			boolean inmodifica[] = new boolean[d.numsez];//array di booleani per verificare se una sezione è in modifica
			for(int k=0; k<inmodifica.length;k++ ) inmodifica[k] = false; //inizializzo l'array a false
			
			//leggo le sezioni
			for(int i=0; i<sezioni.length; i++) {
				buffers[i] = Sezione.leggisezione(((Sezione)sezioni[i]).path);
				sizetotale += buffers[i].capacity(); //aggiorno la sizetotale
				if(( (Sezione) sezioni[i]).getUser()!=null) inmodifica[i] = true;
			}
			ByteBuffer file = ByteBuffer.allocate(sizetotale);//alloco un buffer con dimensione la dimensione totale del documento
			for(ByteBuffer b: buffers) {
				file.put(b);
			}
			file.flip();
			String contenuto = new String(file.array());//trasformo il bytebuffer prima in array di byte e poi in stringa per poterla passare a inviaMessaggio
			int numsez = 1;//perchè le sezioni partono da 1 (quindi non posso usare j) 
			for(int j=0; j<inmodifica.length; j++) { //controllo se qualche sezione è in modifica(edit) da qualche altro utente
				if(inmodifica[j]) contenuto+=("\n"+ "Sezione " + numsez + " in modifica"); //stampo una stringa che indica se una sezione è in modifica 
				numsez++;
			}
			Messaggio.inviaMessaggio(sc, contenuto);//invio il contenuto del documento sotto forma di stringa
		}
		
		else {
			Messaggio.inviaMessaggio(sc, "Utente non autorizzato ad accedere al documento!");
		}

	}

}

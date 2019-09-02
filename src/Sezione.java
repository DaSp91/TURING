import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Sezione implements Comparable<Sezione> {
	
	int num;//numero sezione
	String path;//nome documento
	boolean modifica;//variabile che indica se una sezione è in edit
	String user;//nome utente che esegue l'edit
	
	public Sezione(String nomeDoc, int num) {
		this.path = nomeDoc + "_" + num + ".txt";
		this.num = num;
		modifica = false; 
		user = null;
		File f = new File(this.path);
		try {
			f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getUser() {
		return this.user;
	}
	
	/* Edit:
	 * metodo che uso sia per fare la Edit che la endEdit:
	 * se vorro' fare la edit chiamero' il metodo con start == true;
	 * se invece vorro' fare la endEdit chiamero il metodo con start == false
	 * in questo modo sono sicuro di avere operazioni atomiche
	 * 
	 */
	public synchronized boolean Edit(String user, boolean start) {
		if(start) {
			if(modifica == false) { //controllo che nessuno abbia fatto la edit in precedenza 
				modifica = true; //setto la variabile di edit
				this.user = user;
				return true; 
			}
			else return false;
		}
		else {
			if(modifica == true && this.user.equals(user)) {
				modifica = false;
				this.user = null;
				return true;
			}
			else return false;
		}
	}
	
	public String getPath() {
		return this.path;
	}
	
	public int compareTo(Sezione s) {
		return this.num - s.num;
	}
	
	//metodo che prende il path di una sezione e restituisce un ByteBuffer con il contenuto di quella sezione
	public static ByteBuffer leggisezione(String path) {
		FileChannel inChannel;
		try {
			inChannel = FileChannel.open(Paths.get(path), StandardOpenOption.READ); //apro il file(sezione) in lettura
			String sep = new String("\n---fine sezione: "+path+"---\n");//stringa che indica la fine di una sezione
			ByteBuffer buffer = ByteBuffer.allocate((int) inChannel.size() + sep.getBytes().length); //alloco il buffer della dimensione del file + la dimensione della stringa di fine sezione
			while(buffer.position() != inChannel.size()) { //fino a quando c'è qualcosa da leggere dal fileChannel
				inChannel.read(buffer);//leggo dal file e lo metto nel buffer
			}
			buffer.put(sep.getBytes());//inserisco la stringa di fine sezione nel buffer
			buffer.flip();
			inChannel.close(); //chiudo il file
			return buffer;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}

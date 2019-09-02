import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Messaggio {
	
	public SocketChannel sc; 
	public String msg;
	
	public Messaggio(SocketChannel sc, String msg) {
		this.sc = sc;
		this.msg = msg;
	}
	/* creaMessaggio:
	 * prende in input un array di stringhe e restituisce una stringa nel formato "stringa1_stringai_stringan"
	 * 
	 */
	public static String creaMessaggio(String[] s) {
		
		if(s!=null) {
			String str = s[0];
				
			for(int i=1; i<s.length; i++) {
				str = str + "_" + s[i];
			}
			return str;
		}
		else return null;
	}
	
	/* inviaMessaggio:
	 * prende in input una socket sc e una stringa mess 
	 * ed esegue le operazioni necessarie per scrivere il messaggio mess nella socket sc
	 * protocollo di comunicazione:
	 * 1)invio la dimensione del messaggio da inviare;
	 * 2)invio il messaggio vero e proprio
	 * 
	 */
	public static void inviaMessaggio(SocketChannel sc, String mess) {
		
		//alloco un buffer avente dimensione il numero di byte di un intero(generalmente 4)
		ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
		//inserisco la dimensione di mess nel buffer
		buffer.putInt(mess.length());
		//riporto position a 0
		buffer.flip();
		//scrivo nella socket prima la dimensione e il messaggio
		try {
			sc.write(buffer);
			buffer = ByteBuffer.wrap(mess.getBytes("UTF-8"));//wrap alloca direttamente un bytebuffer delle dimensione opportne
			while(buffer.hasRemaining()) {//fino a quando ci sono elementi nel buffer compresi tra position e limit
				sc.write(buffer);
			}
			buffer.clear();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/* riceviMessaggio:
	 * prende in input una socket sc, legge il messaggio dalla socket e lo retituisce sotto forma di stringa
	 * 
	 */
	public static String riceviMessaggio(SocketChannel sc) throws IOException {
		
		ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
	
		sc.read(buffer);
		buffer.flip();
		buffer = ByteBuffer.allocate(buffer.getInt());
		
		while(buffer.hasRemaining()) {
			sc.read(buffer);
		}
		buffer.flip();
		//codifica stringa letta
		Charset cs = Charset.forName("UTF-8");
		return cs.decode(buffer).toString();

	}
	
	//invia il file di nome path nella socket sc
	public static boolean inviaFile(SocketChannel sc, String path) {
		
		FileChannel inChannel;
		try {
			inChannel = FileChannel.open(Paths.get(path), StandardOpenOption.READ);
			ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
			buffer.putInt((int) inChannel.size());
			buffer.flip();
			sc.write(buffer);//invio la dimensione
			buffer = ByteBuffer.allocate((int) inChannel.size());
			while(buffer.hasRemaining()) {
				inChannel.read(buffer);//leggo dal file e lo metto nel buffer
			}
			buffer.flip();
			sc.write(buffer);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	//legge un file dalla socket sc e restituisce il buffer contenete il file
	public static ByteBuffer riceviFile(SocketChannel sc) {
		ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
		try {
			sc.read(buffer);
			
			buffer.flip();
			int filesize = buffer.getInt();
			buffer = ByteBuffer.allocate(filesize);
			while(buffer.hasRemaining()) {
				sc.read(buffer);
			}
			buffer.flip();
			return buffer;
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}

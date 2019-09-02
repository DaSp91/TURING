
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/*
 * Thread che si occupa di estrare i messaggi dalla coda condivisa, leggere il messaggio
 * e in base al comando avviare il thread opportuno che gestirà la richiesta
 * 
 */

public class ThreadDispatcher implements Runnable {
	
	static int poolsize = 10;
	//ThreadPool ausiliario per i thread che gestiranno le varie richieste
	static ThreadPoolExecutor thread_pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(poolsize);


	@Override
	public void run() {
		while(true) {
			
			try {
				Messaggio messaggio = Server.codamessaggi.take(); //estraggo(e rimuovo) il messaggio dalla coda condivisa
				String[] argomenti = messaggio.msg.split("_"); 
				//parametri passati ai threadworker:
				String username, password, nomedoc, invitato;
				int numsez;
				
				//in base al comando da eseguire che si troverà nella prima posizione dell'array,
				//starto un thread che gestisce l'operazione passandogli come parametri gli argomenti necessari 
				switch(argomenti[0]) {
				
					case "login":
						username = argomenti[1];
						password = argomenti[2];
						ThreadLogin login = new ThreadLogin(username,password, messaggio.sc);
						thread_pool.execute(login);
						break;
					case "logout":
						username = argomenti[1];
						password = argomenti[2];
						ThreadLogout logout = new ThreadLogout(username,password, messaggio.sc);
						thread_pool.execute(logout);
						break;
					case "create":
						nomedoc = argomenti[1];
						numsez= Integer.parseInt(argomenti[2]); //numero sezioni totali del documento
						ThreadCreate create = new ThreadCreate(nomedoc, numsez, messaggio.sc);
						thread_pool.execute(create);
						break;
					case "share":
						nomedoc = argomenti[1];
						invitato = argomenti[2]; //invitato si riferisce allo username dell'utente con cui si vuole condividere il documento
						ThreadShare share = new ThreadShare(nomedoc, invitato, messaggio.sc);
						thread_pool.execute(share);
						break;
					case "showsec":
						nomedoc = argomenti[1];
						numsez = Integer.parseInt(argomenti[2]); //numero sezione che si vuole "vedere"
						ThreadShowSec showsec = new ThreadShowSec(nomedoc, numsez, messaggio.sc);
						thread_pool.execute(showsec);
						break;
					case "showdoc":
						nomedoc = argomenti[1];
						ThreadShowDoc showdoc = new ThreadShowDoc(nomedoc,messaggio.sc);
						thread_pool.execute(showdoc);
						break;	
					case "list":
						ThreadList list = new ThreadList(messaggio.sc);
						thread_pool.execute(list);
						break;
					case "edit":
						nomedoc = argomenti[1];
						numsez = Integer.parseInt(argomenti[2]); //numero sezione che si vuole editare
						ThreadEdit edit = new ThreadEdit(nomedoc, numsez, messaggio.sc);
						thread_pool.execute(edit);
						break;
					case "endedit":
						nomedoc = argomenti[1];
						numsez = Integer.parseInt(argomenti[2]);
						ThreadEndEdit endedit = new ThreadEndEdit(nomedoc, numsez, messaggio.sc);
						thread_pool.execute(endedit);
						break;
					default:
						System.out.println("Sono arrivato qua");
						break;
				}
				
				
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
		}
	}

}

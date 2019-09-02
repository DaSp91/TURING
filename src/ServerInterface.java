import java.rmi.Remote;
import java.rmi.RemoteException;

//Interfaccia remota del server per la fase di registrazione

public interface ServerInterface extends Remote{

	public boolean regUser(String user, String password) throws RemoteException;
	
}

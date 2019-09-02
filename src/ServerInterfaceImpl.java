
import java.rmi.RemoteException;


public class ServerInterfaceImpl implements ServerInterface {

	public boolean regUser(String user, String password) throws RemoteException {
	
		return Server.db.AddUser(user, password);
	}
	
}

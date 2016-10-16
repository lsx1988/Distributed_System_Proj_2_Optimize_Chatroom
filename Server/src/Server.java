import java.io.IOException;
import java.net.ServerSocket;

public class Server {

	public static String serverID, path;
		
	public static void main(String[] args) {
		
		//Get the path and serverID data from command line	
		ArgsReader.read(args);
		path = ArgsReader.getPath();
		serverID = ArgsReader.getServerID();
			
		//Read the config file to get all the server info		
		ConfigReader configReader = new ConfigReader();
		configReader.read(serverID, path);
		
		//Specify the keystore details (this can be specified as VM arguments as well)
		//the keystore file contains an application's own certificate and private key
		//keytool -genkey -keystore <keystorename> -keyalg RSA
		System.setProperty("javax.net.ssl.keyStore","/Users/maria/ssl/mykeystore");
		//Password to access the private key from the keystore file
		System.setProperty("javax.net.ssl.keyStorePassword","mypassword");

		// Enable debugging to view the handshake and communication which happens between the SSLClient and the SSLServer
		System.setProperty("javax.net.debug","all");
		

		
		/*
		 * After reading the config file, set up two listening thread
		 * One for client port and one for server port
		 */
		
		
			
		try {
			//Create serversocket for client port
			ServerSocket server_client = 
					new ServerSocket(configReader.getClientPort(),50,configReader.getServerIP());
			
			//Create serversocket for server port
			ServerSocket server_server = 
					new ServerSocket(configReader.getServerPort(),50,configReader.getServerIP());
			
			//Listening to client port thread
			new ListenToClient(server_client).start();
			
			//Listening to server port thread
			new ListenToServer(server_server).start();
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}

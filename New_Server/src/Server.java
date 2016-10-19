import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

public class Server {

	public static String serverID, Configpath,userFilePath;
	public static int client_port,server_port,heartbeat_port;
	public static InetAddress server_IP;
		
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		
/**
 * Setup the SSl protocal
 */
		//Specify the keystore details (this can be specified as VM arguments as well)
		//the keystore file contains an application's own certificate and private key
		//keytool -genkey -keystore <keystorename> -keyalg RSA
		System.setProperty("javax.net.ssl.keyStore","/home/ubuntu/dsproj2/mykeystore");
		
		//Password to access the private key from the keystore file
		System.setProperty("javax.net.ssl.keyStorePassword","mypassword");
		
		System.setProperty("javax.net.ssl.trustStore", "/home/ubuntu/dsproj2/mykeystore");	
		
/**
 * When the server run, read parameter (serverID and configFile path) from command line	
 */
		//Get the path and serverID data from command line	
		ArgsReader.read(args);
		Configpath = ArgsReader.getConfigPath();
		userFilePath = ArgsReader.getUserFilePath();
		serverID = ArgsReader.getServerID();
			
		//Read the config file to get all the server info		
		ConfigReader configReader = new ConfigReader();
		configReader.read(serverID, Configpath,userFilePath);
		
		client_port=configReader.getClientPort();
		server_port = configReader.getServerPort();
		heartbeat_port = configReader.getHearbeatPort();
		server_IP = configReader.getServerIP();

	
/**
*  After reading the config file, set up two listening thread
*  One for client port and one for server port
*/
		 				
		try {
			
			//Create SSL server socket for client port
			SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory
					.getDefault();
			
			SSLServerSocket server_client = (SSLServerSocket) sslserversocketfactory
					.createServerSocket(client_port,50, server_IP);
			
			//Create SSL server socket for server port
			SSLServerSocket server_server = (SSLServerSocket) sslserversocketfactory
					.createServerSocket(server_port,50, server_IP);
			
			System.out.println(heartbeat_port);

			//Listening to client port thread
			new ListenToClient(server_client).start();
			
			//Listening to server port thread
			new ListenToServer(server_server).start();
			
			for(String[] s : configReader.getInfo()){
				new HeartBeatThread(s).start();
			}			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}

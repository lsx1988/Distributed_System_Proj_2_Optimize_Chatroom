package au.edu.unimelb.tcp.client;
import java.io.IOException;
import java.net.UnknownHostException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.json.simple.parser.ParseException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public class Client {

	public static void main(String[] args) throws IOException, ParseException {

/**
 * Set up the SSLSocket library		
 */
		//Location of the Java keystore file containing the collection of 
		//certificates trusted by this application (trust store).
		System.setProperty("javax.net.ssl.trustStore", "C:\\Users\\liush\\Documents\\GitHub_Root\\DS_Proj_2_Optimize_Chatroom\\mykeystore");
		SSLSocketFactory sslsocketfactory=(SSLSocketFactory) SSLSocketFactory.getDefault();
		
		SSLSocket socket = null;
		String identity = null;
		boolean debug = false;
		
		try {
			//load command line args
			ComLineValues values = new ComLineValues();
			CmdLineParser parser = new CmdLineParser(values);
			try {
				parser.parseArgument(args);
				String hostname = values.getHost();
				identity = values.getIdeneity();
				int port = values.getPort();
				debug = values.isDebug();
				socket = (SSLSocket) sslsocketfactory.createSocket(hostname, port);
			} catch (CmdLineException e) {
				e.printStackTrace();
			}
			
			State state = new State(identity, "");
			
			// start sending thread
			MessageSendThread messageSendThread = new MessageSendThread(socket, state, debug);
			Thread sendThread = new Thread(messageSendThread);
			sendThread.start();
			
			//start receiving thread
			Thread receiveThread = new Thread(new MessageReceiveThread(socket, state, messageSendThread, debug));
			receiveThread.start();
			
		} catch (UnknownHostException e) {
			System.out.println("Unknown host");
		} catch (IOException e) {
			System.out.println("Communication Error: " + e.getMessage());
		}
	}
}

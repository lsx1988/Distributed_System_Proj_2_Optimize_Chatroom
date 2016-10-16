import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;

public class ConfigReader {
	
	private  InetAddress serverIP;
	private  int clientPort;
	private  int serverPort;
	private  ServerDatabase ds = ServerDatabase.getInstance();

	public InetAddress getServerIP() {
		return serverIP;
	}

	public int getClientPort() {
		return clientPort;
	}

	public int getServerPort() {
		return serverPort;
	}
	
	public void read(String serverID, String path){
		
		String[] info = new String[4];
	
		try {
			//Get the br of config file object
			BufferedReader br=
					new BufferedReader(
							new FileReader(
									new File(path)));
			String str=null;
			
			while((str=br.readLine())!=null){
				
				//split the string by tab
				info=str.split("\t");
				
				if(serverID.equals(info[0])){
					
					this.serverIP = InetAddress.getByName(info[1]);
					this.clientPort = Integer.parseInt(info[2]);
					this.serverPort = Integer.parseInt(info[3]);					
					
				}else{
					ds.addServer(info);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
}

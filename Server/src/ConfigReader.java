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
	
	public void read(String serverID, String configPath,String userFilePath){
	
		try {
			//Get the br of config file object
			BufferedReader br_config=
					new BufferedReader(
							new FileReader(
									new File(configPath)));
			String str=null;
			
			while((str=br_config.readLine())!=null){
				
				//split the string by tab
				String[] info=str.split("\t");
				
				if(serverID.equals(info[0])){
					
					this.serverIP = InetAddress.getByName(info[1]);
					this.clientPort = Integer.parseInt(info[2]);
					this.serverPort = Integer.parseInt(info[3]);					
					
				}else{
					ds.addServer(info);
				}
				
				//Get the br of user file object
				BufferedReader br_user=
						new BufferedReader(
								new FileReader(
										new File(userFilePath)));
				while((str=br_user.readLine())!=null){
					//split the string by tab
					String[] info_2=str.split("\t");
					ds.addUser(info_2[0],info_2[1],info_2[2]);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
}

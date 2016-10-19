import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public class ArgsReader {
	
	private static String serverID, configPath,userFilePath;
		
	public static void read(String[] args){
		
		//Object that will store the parsed command line arguments
		CmdLineArgs argsBean = new CmdLineArgs();
		
		//Parser provided by args4j
		CmdLineParser parser = new CmdLineParser(argsBean);
		
		try {
			
			//Parse the arguments
			parser.parseArgument(args);
		    serverID=argsBean.getServerID();
			configPath=argsBean.getConfigPath();
			userFilePath=argsBean.getUserFilePath();
			
		} catch (CmdLineException e) {				
			System.err.println(e.getMessage());					
			parser.printUsage(System.err);			
		}
	}
	
	public static String getServerID() {
		return serverID;
	}

	public static String getConfigPath() {
		return configPath;
	}
	
	public static String getUserFilePath() {
		return userFilePath;
	}
}

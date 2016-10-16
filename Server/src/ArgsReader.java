import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public class ArgsReader {
	
	private static String serverID, path;
		
	public static void read(String[] args){
		
		//Object that will store the parsed command line arguments
		CmdLineArgs argsBean = new CmdLineArgs();
		
		//Parser provided by args4j
		CmdLineParser parser = new CmdLineParser(argsBean);
		
		try {
			
			//Parse the arguments
			parser.parseArgument(args);
		    serverID=argsBean.getServerID();
			path=argsBean.getPath();
			
		} catch (CmdLineException e) {				
			System.err.println(e.getMessage());					
			parser.printUsage(System.err);			
		}
	}
	
	public static String getServerID() {
		return serverID;
	}

	public static String getPath() {
		return path;
	}
}

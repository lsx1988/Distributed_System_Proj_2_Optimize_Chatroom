import org.kohsuke.args4j.Option;

public class CmdLineArgs {

	@Option(required = true, name = "-n", aliases = {"--ID"}, usage = "serverID")
	private String serverID;
	
	@Option(required = true, name = "-l", aliases = {"--Path_1"}, usage = "Config Path")
	private String configPath;
	
	@Option(required = true, name = "-p", aliases = {"--Path_2"}, usage = "User record Path")
	private String userFilePath;

	public String getServerID() {
		return serverID;
	}

	public String getConfigPath() {
		return configPath;
	}
	
	public String getUserFilePath() {
		return userFilePath;
	}
}

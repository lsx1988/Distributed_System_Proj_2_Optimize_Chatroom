import org.kohsuke.args4j.Option;

public class CmdLineArgs {

	@Option(required = true, name = "-n", aliases = {"--ID"}, usage = "serverID")
	private String serverID;
	
	@Option(required = true, name = "-l", aliases = {"--Path"}, usage = "Config Path")
	private String configPath;

	public String getServerID() {
		return serverID;
	}

	public String getPath() {
		return configPath;
	}
}

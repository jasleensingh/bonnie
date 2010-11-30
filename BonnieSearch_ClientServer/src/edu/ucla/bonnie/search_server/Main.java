package edu.ucla.bonnie.search_server;

public class Main {
	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			System.err.println("Usage: java bonnie_searcher <master|slave> <args...>");
			System.exit(1);
		}
		String type = args[0];
		String[] nargs = new String[args.length - 1];
		if (nargs.length > 0) {
			System.arraycopy(args, 1, nargs, 0, nargs.length);
		}
		if ("master".equals(type)) {
			SearchMaster.main(nargs);
		} else if ("slave".equals(type)) {
			SearchSlave.main(nargs);
		}
	}
}

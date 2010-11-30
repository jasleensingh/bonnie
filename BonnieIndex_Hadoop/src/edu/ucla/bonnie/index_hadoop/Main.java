package edu.ucla.bonnie.index_hadoop;

public class Main {
	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			System.err.println("Usage: java bonnie_indexer <cmd> <args...>");
			System.exit(1);
		}
		String cmd = args[0];
		String[] nargs = new String[args.length - 1];
		if (nargs.length > 0) {
			System.arraycopy(args, 1, nargs, 0, nargs.length);
		}
		if ("copy".equals(cmd)) {
			CopyData.main(nargs);
		} else if ("index".equals(cmd)) {
			Indexer.main(nargs);
		}
	}
}

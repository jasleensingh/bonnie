package edu.ucla.bonnie.search_server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import edu.ucla.bonnie.common.AppData;
import edu.ucla.bonnie.common.Constants;
import edu.ucla.bonnie.search_common.Connection;
import edu.ucla.bonnie.search_common.SearchConstants;

public class SearchMaster {
	private static void printUsageAndExit() {
		System.err
				.println("Usage: java SearchMaster -nslaves <num-slaves> -dfsOutDir <dfs-output-directory> [-port <port>]");
		System.exit(0);
	}

	public static void main(String[] args) throws Exception {
		int nslaves = 0;
		int port = SearchConstants.DEFAULT_MASTER_PORT;
		String dfsOutDir = null;
		for (int i = 0; i < args.length; i += 2) {
			if (args[i].equals("-nslaves")) {
				nslaves = Integer.parseInt(args[i + 1]);
			} else if (args[i].equals("-dfsOutDir")) {
				dfsOutDir = args[i + 1];
			} else if (args[i].equals("-port")) {
				port = Integer.parseInt(args[i + 1]);
			}
		}
		if (nslaves <= 0 || dfsOutDir == null) {
			printUsageAndExit();
		}
		Connection[] conns = new Connection[nslaves];
		ServerSocket server = new ServerSocket(port);
		System.out.println("Running Bonnie Search Master.");
		System.out.println("Waiting for " + nslaves + " slaves.");
		for (int i = 0; i < nslaves; i++) {
			Socket s = server.accept();
			BufferedReader r = new BufferedReader(new InputStreamReader(s
					.getInputStream()));
			PrintWriter w = new PrintWriter(s.getOutputStream());
			int n = Integer.parseInt(r.readLine());
			System.out.println("Slave " + n + " connected.");
			conns[n] = new Connection(s, r, w);
		}
		System.out.println("Transferring hash files...");
		Configuration conf = new Configuration();
		conf.set("fs.default.name", "hdfs://localhost:9000");
		FileSystem fs = FileSystem.get(conf);
		Path dir = new Path(Constants.BONNIE_DFS_HOME + "/" + dfsOutDir);
		List<Path> paths = new LinkedList<Path>();
		for (FileStatus status : fs.listStatus(dir)) {
			Path path = status.getPath();
			if (status.isFile() && path.getName().startsWith("part-")) {
				paths.add(path);
			}
		}
		int slave = 0;
		for (Path path : paths) {
			BufferedReader r = new BufferedReader(new InputStreamReader(fs
					.open(path)));
			String line;
			while ((line = r.readLine()) != null) {
				if ((line = line.trim()).length() <= 0) {
					continue;
				}
				conns[slave].w.println(line);
				slave = (slave + 1) % nslaves;
			}
		}
		PrintWriter searchSlaves = new PrintWriter(new AppData.Store("Search")
				.getFile("slaves"));
		for (int i = 0; i < nslaves; i++) {
			conns[i].w.println("/");
			conns[i].w.flush();
			searchSlaves.println(conns[i].r.readLine());
			conns[i].close();
		}
		searchSlaves.close();
	}
}

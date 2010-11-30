package edu.ucla.bonnie.index_hadoop;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import edu.ucla.bonnie.common.AppData;
import edu.ucla.bonnie.common.Constants;
import edu.ucla.bonnie.common.Utils;

public class CopyData {
	private static final Path defaultStore = new Path(Constants.BONNIE_DFS_HOME
			+ "/store");

	private static int __docID;

	private static void copy(FileSystem fs, File dir,
			Map<String, String> invMap, PrintWriter w) throws IOException {
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				copy(fs, file, invMap, w);
			} else if (Utils.accept(file.getPath())) {
				String name = file.getName();
				Path src = new Path(file.getAbsolutePath());
				Path dst = new Path(defaultStore + "/" + (++__docID)
						+ name.substring(name.lastIndexOf(".")));
				if (!invMap.containsKey(src.toString())) {
					System.out.println("Copying: " + src.toString());
					fs.copyFromLocalFile(src, dst);
					w.println(dst + "\t" + src);
					invMap.put(src.toString(), dst.toString());
				} else {
					System.out.println("Skipping: " + src.toString());
				}
			}
		}
	}

	private static void copy(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println("Usage: java CopyData <namenode-addr> <copy-dir>");
			System.exit(1);
		}
		String namenode = args[0];
		File root = new File(args[1]);
		Configuration conf = new Configuration();
		conf.set("fs.default.name", "hdfs://" + namenode + ":9000");

		FileSystem fs = FileSystem.get(conf);
		Path mapPath = new Path(Constants.BONNIE_DFS_HOME + "/store/map");
		Map<String, String> currMap;
		OutputStream out;
		if (fs.exists(mapPath)) {
			currMap = readInvMap(fs, mapPath);
			out = fs.append(mapPath);
		} else {
			currMap = new HashMap<String, String>();
			out = fs.create(mapPath);
		}
		__docID = currMap.size();
		PrintWriter pw;
		pw = new PrintWriter(out);
		copy(fs, root, currMap, pw);
		pw.close();

		// Write file names on hdfs to 'input/files'
		pw = new PrintWriter(fs.create(new Path(Constants.BONNIE_DFS_HOME
				+ "/input/files")));
		for (String filename : currMap.values()) {
			pw.println(filename);
		}
		pw.close();

		// Copy map to local filesystem
		fs.copyToLocalFile(mapPath, new Path(new AppData.Store("Index")
				.getFile("map").getAbsolutePath()));
	}

	private static Map<String, String> readInvMap(FileSystem fs, Path mapPath)
			throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		BufferedReader r = new BufferedReader(new InputStreamReader(fs
				.open(mapPath)));
		String line;
		while ((line = r.readLine()) != null) {
			String[] tok = line.split("\\t");
			map.put(tok[1], tok[0]);
		}
		return map;
	}

	public static void main(String[] args) throws Exception {
		copy(args);
	}
}

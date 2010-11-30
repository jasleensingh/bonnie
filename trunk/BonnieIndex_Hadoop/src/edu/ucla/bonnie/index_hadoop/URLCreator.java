package edu.ucla.bonnie.index_hadoop;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import edu.ucla.bonnie.common.Constants;
import edu.ucla.bonnie.common.Utils;

public class URLCreator {
	private static final Path defaultRoot = new Path(Constants.BONNIE_DFS_HOME
			+ "/store");

	private static void create(FileSystem fs, Path dir, PrintWriter w)
			throws IOException {
		for (FileStatus fileStatus : fs.listStatus(dir)) {
			Path filePath = fileStatus.getPath();
			if (fileStatus.isDirectory()) {
				create(fs, filePath, w);
			} else if (Utils.accept(filePath.toString())) {
				w.println(filePath);
			}
		}
	}

	private static void create(String[] args) throws Exception {
		Path root;
		if (args != null && args.length == 1) {
			root = new Path(args[0]);
		} else {
			root = defaultRoot;
		}
		Configuration conf = new Configuration();
		conf.set("fs.default.name", "hdfs://localhost:9000");

		FileSystem fs = FileSystem.get(conf);
		PrintWriter pw = new PrintWriter(fs.create(new Path(
				Constants.BONNIE_DFS_HOME + "/input/urls")));
		create(fs, root, pw);
		pw.close();
	}

	public static void main(String[] args) throws Exception {
		create(args);
	}
}

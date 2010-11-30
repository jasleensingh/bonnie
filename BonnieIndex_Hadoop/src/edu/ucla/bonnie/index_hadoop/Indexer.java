package edu.ucla.bonnie.index_hadoop;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.NLineInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import edu.ucla.bonnie.common.Base;
import edu.ucla.bonnie.common.Utils;

@SuppressWarnings("deprecation")
public class Indexer extends Configured implements Tool {
	public static class Map extends Mapper<LongWritable, Text, Text, Text> {
		private static class IndexHelper extends Base {
			private final FileSystem fileSystem;

			public IndexHelper(FileSystem fileSystem) {
				this.fileSystem = fileSystem;
			}

			// 1. Copy from dfs to local fs
			// 2. Convert to wav using ffmpeg
			// 3. Create hashes
			public String[] createHashes(Path path) throws Exception {
				File outFile;
				System.err.println("Copying to local fs");
				String in = tempDir + "/" + path.getName();
				fileSystem.copyToLocalFile(path, new Path(in));
				File inFile = new File(in);
				if (!inFile.getName().endsWith(".wav")) {
					String cmd = convert.getAbsolutePath();
					outFile = new File(tempDir + "/tmp"
							+ Utils.randomAlphaNum(5) + ".wav");
					System.err.println("Converting to " + outFile);
					String out = outFile.toString();
					ProcessBuilder pb = new ProcessBuilder(cmd, in, out);
					pb.redirectErrorStream(true);
					Process p = pb.start();
					BufferedReader r = new BufferedReader(
							new InputStreamReader(p.getInputStream()));
					while (r.readLine() != null)
						;
					r.close();
				} else {
					outFile = inFile;
				}
				return getHashes(outFile);
			}
		}

		private IndexHelper indexHelper;

		public Map() {
			Configuration conf = new Configuration();
			conf.set("fs.default.name", "hdfs://localhost:9000");
			try {
				FileSystem fs = FileSystem.get(conf);
				indexHelper = new IndexHelper(fs);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		protected void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			Path path = new Path(value.toString());
			if (indexHelper != null) {
				try {
					String[] hashes = indexHelper.createHashes(path);
					for (String hash : hashes) {
						context.write(new Text(hash), value);
					}
				} catch (Exception e) {
					context.write(value, new Text("Error creating hashes"));
				}
			} else {
				context
						.write(value,
								new Text("Error initializing file system"));
			}
		}
	}

	public static class Reduce extends Reducer<Text, Text, Text, Text> {
		protected void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			context.write(key, values.iterator().next());
		}
	}

	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = getConf();
		conf.set(NLineInputFormat.LINES_PER_MAP, "2");
		Job job = new Job(conf);
		job.setJarByClass(Indexer.class);
		job.setJobName("bonnie_index");
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);

		job.setInputFormatClass(NLineInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		FileInputFormat.setInputPaths(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static void main(String[] args) throws Exception {
		System.err.println("\n\nrunning\n\n");
		int ret = ToolRunner.run(new Indexer(), args);
		System.exit(ret);
	}
}

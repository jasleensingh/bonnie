package edu.ucla.bonnie.search_client;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ucla.bonnie.common.Base;

public class SearchHelper extends Base {
//	public static Match[] search(File inFile) throws Exception {
//		File outFile;
//		if (!inFile.getName().endsWith(".wav")) {
//			String cmd = convert.getAbsolutePath();
//			String in = inFile.getAbsolutePath();
//			outFile = new File(tempDir + "/" + "tmp.wav");
//			String out = outFile.getAbsolutePath();
//			Runtime.getRuntime().exec(new String[] { cmd, in, out });
//		} else {
//			outFile = inFile;
//		}
//		String[] query = getHashes(outFile, false);
//		List<Match> matches = new ArrayList<Match>();
//		for (Map.Entry<String, String> entry : getMapTable().entrySet()) {
//			// System.err.println(entry.getValue());
//			String[] db_entry = getHashes(new File(entry.getKey()), true);
//			matches.add(new Match(entry.getKey(), find(query, db_entry)));
//		}
//		Match[] array = matches.toArray(new Match[0]);
//		Arrays.sort(array, new Comparator<Match>() {
//			@Override
//			public int compare(Match m1, Match m2) {
//				return m2.dataPoints.length - m1.dataPoints.length;
//			}
//		});
//		// for (Match m : array) {
//		// System.out.println(m.matchedFilename + " ("
//		// + m.dataPoints.length + " matches)");
//		// }
//		return array;
//	}
//
//	public static class Match {
//		public final String matchedFilename;
//		public final DataPoint[] dataPoints;
//
//		public Match(String matchedFilename, DataPoint[] dataPoints) {
//			this.matchedFilename = matchedFilename;
//			this.dataPoints = dataPoints;
//		}
//	}
//
//	public static class DataPoint {
//		public final int query_t;
//		public final int match_t;
//
//		public DataPoint(int queryT, int matchT) {
//			query_t = queryT;
//			match_t = matchT;
//		}
//	}
//
//	private static DataPoint[] find(String[] query, String[] hashes) {
//		Map<String, Integer> hashes_map = new HashMap<String, Integer>();
//		for (int i = 0; i < hashes.length; i++) {
//			hashes_map.put(hashes[i], i);
//		}
//		List<DataPoint> matches = new ArrayList<DataPoint>();
//		for (int i = 0; i < query.length; i++) {
//			Integer t = hashes_map.get(query[i]);
//			if (t != null) {
//				matches.add(new DataPoint(i, t));
//			}
//		}
//		DataPoint[] array = matches.toArray(new DataPoint[0]);
//		Arrays.sort(array, new Comparator<DataPoint>() {
//			public int compare(DataPoint o1, DataPoint o2) {
//				return o1.match_t - o2.match_t;
//			}
//		});
//		// printMatches(query, hashes, array);
//		return array;
//	}
}

package edu.ucla.bonnie.server;

import java.util.Comparator;

public class Match {
	public static Comparator<Match> NumMatchesDescending = new Comparator<Match>() {
		@Override
		public int compare(Match o1, Match o2) {
			return o2.nmatches - o1.nmatches;
		}
	};
	public String filepath;
	public int nmatches;

	public Match(String path, int nmatches) {
		this.filepath = path;
		this.nmatches = nmatches;
	}
}

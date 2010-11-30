package edu.ucla.bonnie.server;

import java.util.Properties;

public class Result {
	public String title;
	public String filepath;
	public Properties metadata;
	public int matchPercent;

	public String toString() {
		return title + "," + filepath + "," + metadata + "," + matchPercent;
	}
}
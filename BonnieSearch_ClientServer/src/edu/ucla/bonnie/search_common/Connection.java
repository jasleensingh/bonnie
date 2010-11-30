package edu.ucla.bonnie.search_common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class Connection {
	public final Socket socket;
	public final BufferedReader r;
	public final PrintWriter w;

	public Connection(Socket socket, BufferedReader r, PrintWriter w) {
		this.socket = socket;
		this.r = r;
		this.w = w;
	}

	public void close() throws IOException {
		socket.close();
	}
}
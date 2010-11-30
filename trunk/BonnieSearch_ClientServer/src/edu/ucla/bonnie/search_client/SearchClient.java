package edu.ucla.bonnie.search_client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import edu.ucla.bonnie.search_common.Connection;
import edu.ucla.bonnie.search_common.SearchConstants;

public class SearchClient {
	private String[] searchServers;

	private Connection[] connections;

	public SearchClient(String[] servers) {
		searchServers = servers;
		connections = new Connection[servers.length];
	}

	public void close() throws IOException {
		for (Connection conn : connections) {
			conn.close();
		}
	}

	public int connect() {
		int error = 0;
		for (int i = 0; i < searchServers.length; i++) {
			try {
				String ipAddr;
				int port;
				int sep = searchServers[i].indexOf(":");
				if (sep < 0) {
					ipAddr = searchServers[i];
					port = SearchConstants.DEFAULT_SLAVE_PORT;
				} else {
					ipAddr = searchServers[i].substring(0, sep);
					port = Integer
							.parseInt(searchServers[i].substring(sep + 1));
				}
				Socket socket = new Socket(ipAddr, port);
				connections[i] = new Connection(socket, new BufferedReader(
						new InputStreamReader(socket.getInputStream())),
						new PrintWriter(socket.getOutputStream()));
			} catch (Exception e) {
				error--;
				connections[i] = null;
			}
		}
		return error;
	}

	public synchronized String[] search(String[] hashes) throws Exception {
		for (int i = 0; i < connections.length; i++) {
			Connection sd = connections[i];
			for (String hash : hashes) {
				sd.w.println(hash);
			}
			sd.w.println("/");
			sd.w.flush();
		}
		String[] docIDs = new String[hashes.length];
		for (int i = 0; i < connections.length; i++) {
			Connection sd = connections[i];
			for (int j = 0; j < hashes.length; j++) {
				String docID = sd.r.readLine();
				if (docIDs[j] == null && docID.length() > 0) {
					docIDs[j] = docID;
				}
			}
		}
		return docIDs;
	}

	private static final void ensure(boolean b) throws RuntimeException {
		if (!b) {
			throw new RuntimeException();
		}
	}
}

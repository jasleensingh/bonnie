package edu.ucla.bonnie.search_server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import edu.ucla.bonnie.common.Constants;
import edu.ucla.bonnie.search_common.SearchConstants;

public class SearchSlave extends Thread {
	private String masterIPAddr;
	private int masterPort;
	private int num;
	private int port;

	private class RequestHandler extends Thread {
		public Socket socket;
		public final BufferedReader r;
		public final PrintWriter w;

		public RequestHandler(Socket socket, BufferedReader r, PrintWriter w) {
			this.socket = socket;
			this.r = r;
			this.w = w;
		}

		@Override
		public void run() {
			try {
				while (true) {
					String hash;
					while ((hash = r.readLine()) != null && !hash.equals("/")) {
						int index = Arrays.binarySearch(indexItems,
								new IndexItem(hash, null), IndexItem.Cmp);
						w.println(index < 0 ? "" : indexItems[index].docID);
					}
					if (hash == null) {
						// connection broken
						break;
					}
					w.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static final void ensure(boolean b) throws RuntimeException {
		if (!b) {
			throw new RuntimeException();
		}
	}

	public SearchSlave(String masterIPAddr, int masterPort, int num, int port) {
		this.masterIPAddr = masterIPAddr;
		this.masterPort = masterPort;
		this.num = num;
		this.port = port;
	}

	@Override
	public void run() {
		try {
			System.out.println("Reading index.");
			readIndex(true);
			System.out.println("Listening for client requests at port " + port
					+ ".");
			ServerSocket server = new ServerSocket(port);
			while (true) {
				Socket socket = server.accept();
				new RequestHandler(socket, new BufferedReader(
						new InputStreamReader(socket.getInputStream())),
						new PrintWriter(socket.getOutputStream())).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static class IndexItem {
		public static final Comparator<IndexItem> Cmp = new Comparator<IndexItem>() {
			@Override
			public int compare(IndexItem i1, IndexItem i2) {
				return i1.hash.compareTo(i2.hash);
			}
		};

		public final String hash;
		public final String docID;

		public IndexItem(String hash, String docID) {
			this.hash = hash;
			this.docID = docID;
		}

		public String toString() {
			return write();
		}

		public final String write() {
			return hash + "\t" + docID;
		}

		public static final IndexItem read(String s) {
			String[] tok = s.split("\\t");
			return new IndexItem(tok[0], tok[1]);
		}
	}

	private IndexItem[] indexItems;

	public File getIndexFile() {
		return new File(Constants.BONNIE_HOME + "/.index" + num);
	}

	public void writeIndex() throws Exception {
		if (indexItems == null) {
			throw new Exception("Index not initialized");
		}
		File indexFile = new File(Constants.BONNIE_HOME + "/.index" + num);
		PrintWriter pw = new PrintWriter(indexFile);
		for (int i = 0; i < indexItems.length; i++) {
			pw.println(indexItems[i].write());
		}
		pw.close();
	}

	private static String getLocalHost() throws Exception {
		for (Enumeration<NetworkInterface> nis = NetworkInterface
				.getNetworkInterfaces(); nis.hasMoreElements();) {
			for (Enumeration<InetAddress> addrs = ((NetworkInterface) nis
					.nextElement()).getInetAddresses(); addrs.hasMoreElements();) {
				InetAddress addr = (InetAddress) addrs.nextElement();
				if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
					return addr.getHostAddress();
				}
			}
		}
		return null;
	}

	public void readIndex(boolean useCache) throws Exception {
		File indexFile = getIndexFile();
		if (!indexFile.exists() || !useCache) {
			Socket s = new Socket(masterIPAddr, masterPort);
			PrintWriter w = new PrintWriter(s.getOutputStream());
			BufferedReader r = new BufferedReader(new InputStreamReader(s
					.getInputStream()));
			w.println(num);
			w.flush();
			readIndex(r);
			w.println(getLocalHost() + ":" + port);
			w.flush();
			r.close();
			w.close();
			s.close();
			// For future use
			writeIndex();
		} else {
			BufferedReader r = new BufferedReader(new FileReader(indexFile));
			readIndex(r);
			r.close();
		}
	}

	private void readIndex(BufferedReader r) throws IOException {
		String item_str;
		List<IndexItem> list = new ArrayList<IndexItem>();
		while ((item_str = r.readLine()) != null && !item_str.equals("/")) {
			list.add(IndexItem.read(item_str));
			if (list.size() % 1000 == 0) {
				System.out.print(".");
			}
		}
		indexItems = list.toArray(new IndexItem[0]);
		System.out.println();
		Arrays.sort(indexItems, IndexItem.Cmp);
	}

	private static void printUsageAndExit() {
		System.err
				.println("Usage: java SearchSlave -mip <master-ip> [-mport <master-port>] -n <num> [-p <port>]");
		System.exit(0);
	}

	public static void main(String[] args) {
		String masterIPAddr = null;
		int masterPort = SearchConstants.DEFAULT_MASTER_PORT;
		int num = -1;
		int port = SearchConstants.DEFAULT_SLAVE_PORT;
		for (int i = 0; i < args.length; i += 2) {
			if (args[i].equals("-mip")) {
				masterIPAddr = args[i + 1];
			} else if (args[i].equals("-mport")) {
				masterPort = Integer.parseInt(args[i + 1]);
			} else if (args[i].equals("-n")) {
				num = Integer.parseInt(args[i + 1]);
			} else if (args[i].equals("-port")) {
				port = Integer.parseInt(args[i + 1]);
			}
		}
		if (masterIPAddr == null || num < 0) {
			printUsageAndExit();
		}
		System.out.println("Running Bonnie Search Slave.");
		new SearchSlave(masterIPAddr, masterPort, num, port).start();
	}
}

package edu.ucla.bonnie.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ucla.bonnie.common.AppData;
import edu.ucla.bonnie.common.Base;
import edu.ucla.bonnie.search_client.SearchClient;

public class QueryQueue extends Thread {
	private static QueryQueue instance;

	private SearchClient searchClient;
	private Map<String, String> docID2File;

	private List<QueryData> processQueue = new ArrayList<QueryData>();
	private Map<String, QueryData> doneMap = new HashMap<String, QueryData>();

	private QueryQueue(SearchClient searchClient, Map<String, String> docID2File) {
		this.searchClient = searchClient;
		this.docID2File = docID2File;
	}

	private static Map<String, String> createDocID2File() {
		try {
			Map<String, String> docID2File = new HashMap<String, String>();
			BufferedReader r = new BufferedReader(new FileReader(
					new AppData.Store("Index").getFile("map")));
			String line;
			while ((line = r.readLine()) != null) {
				if ((line = line.trim()).length() <= 0) {
					continue;
				}
				String[] tok = line.split("\\t");
				docID2File.put(tok[0], tok[1]);
			}
			System.err.println("created docid2file map");
			return docID2File;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static SearchClient createSearchClient() {
		try {
			File searchSlaves = new AppData.Store("Search").getFile("slaves");
			BufferedReader r = new BufferedReader(new FileReader(searchSlaves));
			List<String> servers = new ArrayList<String>();
			String line;
			while ((line = r.readLine()) != null) {
				servers.add(line);
			}
			SearchClient client = new SearchClient(servers
					.toArray(new String[0]));
			if (client.connect() < 0) {
				throw new Exception("Error establishing connection");
			}
			System.err.println("created search client");
			return client;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static QueryQueue get() {
		if (instance == null) {
			instance = new QueryQueue(createSearchClient(), createDocID2File());
			instance.start();
		}
		return instance;
	}

	@Override
	public void run() {
		while (true) {
			QueryData data;
			synchronized (this) {
				while (processQueue.isEmpty()) {
					try {
						wait();
					} catch (InterruptedException e) {
					}
				}
				data = processQueue.remove(0);
			}
			try {
				data.setState(QueryState.PROCESSING);

				String[] query = Base.getHashes(new File(data.filepath));
				String[] docIDs = searchClient.search(query);
				Map<String, Match> matchMap = new HashMap<String, Match>();
				Match match = null;
				for (String s : docIDs) {
					if (s == null) {
						continue;
					}
					if ((match = matchMap.get(s)) == null) {
						matchMap.put(s, new Match(docID2File.get(s), 1));
					} else {
						match.nmatches++;
					}
				}
				Match[] matches = matchMap.values().toArray(new Match[0]);
				Arrays.sort(matches, Match.NumMatchesDescending);
				int mostMatches = matches[0].nmatches;
				int leastMatches = matches[matches.length - 1].nmatches;
				int matchRange = mostMatches - leastMatches;
				if (matchRange <= 0) {
					matchRange = 1;
				}

				Result[] results = new Result[matches.length];
				for (int i = 0; i < results.length; i++) {
					Result r = new Result();
					match = matches[i];
					r.filepath = match.filepath;
					r.matchPercent = (match.nmatches - leastMatches) * 100
							/ matchRange;
					r.title = new File(r.filepath).getName();
					results[i] = r;
				}
				data.results = results;
				data.setState(QueryState.DONE);
				doneMap.put(data.id, data);
			} catch (Exception e) {
				data.setState(QueryState.ERROR);
				doneMap.put(data.id, data);
			}
		}
	}

	public synchronized void addQuery(QueryData data) {
		processQueue.add(data);
		data.setState(QueryState.QUEUED);
		notifyAll();
	}

	public synchronized QueryData getQuery(String id) {
		QueryData data = doneMap.get(id);
		if (data != null) {
			return data;
		}
		for (QueryData qd : processQueue) {
			if (qd.id == id) {
				return qd;
			}
		}
		return null;
	}

	public QueryData[] getProcessQueue() {
		return processQueue.toArray(new QueryData[0]);
	}

	public QueryData[] getDoneMap() {
		return doneMap.values().toArray(new QueryData[0]);
	}
}

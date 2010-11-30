package edu.ucla.bonnie.server;

public class QueryData {
	public final String id;
	public final String filepath;
	private QueryState state;
	Result[] results;
	private long accessedAt;

	public QueryData(String id, String filepath) {
		this.id = id;
		this.filepath = filepath;
		setState(QueryState.CREATED);
	}

	public QueryState getState() {
		return state;
	}

	public void setState(QueryState state) {
		this.state = state;
		if (state == QueryState.DONE || state == QueryState.ERROR) {
			accessedAt = System.currentTimeMillis();
		}
	}

	public Result[] getResults() {
		return results;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("id=" + id + "\n");
		sb.append("filepath=" + filepath + "\n");
		sb.append("state=" + state + "\n");
		if (results != null) {
			sb.append("results:\n");
			for (Result r : results) {
				sb.append(r + "\n");
			}
		}
		sb.append("accessedAt=" + accessedAt + "\n");
		return sb.toString();
	}
}
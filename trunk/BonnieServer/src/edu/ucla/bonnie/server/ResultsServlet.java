package edu.ucla.bonnie.server;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class ResultsServlet extends HttpServlet {
	private static final File serverTempDir = new AppData.Store("Server")
			.getDir("Temp");

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		System.err.println("Get request received");
		resp.setContentType("text/plain");
		PrintWriter pw = resp.getWriter();
		String id = req.getParameter("id");
		if (id == null) {
			pw.println(Common.ERROR);
			return;
		}
		String page_str = req.getParameter("p");
		int page;
		try {
			page = Integer.parseInt(page_str);
		} catch (Exception e) {
			page = 1;
		}
		QueryData data = QueryQueue.get().getQuery(id);
		if (data == null) {
			pw.println("Unknown or expired id: " + id);
		} else {
			switch (data.getState()) {
			case QUEUED:
				pw.println("Query request is in queue");
				break;
			case PROCESSING:
				pw.println("Query request is being processed");
				break;
			case ERROR:
				pw.println("An error occurred while processing query");
				break;
			case DONE:
				Result[] results = data.getResults();
				for (Result r : results) {
					pw.println(r.filepath + " (" + r.matchPercent + "%)");
				}
				break;
			}
		}
		pw.close();
	}
}

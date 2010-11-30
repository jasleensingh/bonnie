package edu.ucla.bonnie.server;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class DebugServlet extends HttpServlet {
	private static final File serverTempDir = new AppData.Store("Server")
			.getDir("Temp");

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setContentType("text/plain");
		PrintWriter pw = resp.getWriter();
		String t = req.getParameter("t");
		if (t == null) {
			pw.println(Common.ERROR);
			return;
		}
		if ("queue".equals(t)) {
			QueryData[] queue = QueryQueue.get().getProcessQueue();
			for (QueryData d : queue) {
				pw.println(d);
			}
		} else if ("done".equals(t)) {
			QueryData[] done = QueryQueue.get().getDoneMap();
			for (QueryData d : done) {
				pw.println(d);
			}
		}
		pw.close();
	}
}

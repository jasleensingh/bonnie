package edu.ucla.bonnie.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class QueryServlet extends HttpServlet {
	private static final File serverTempDir = new AppData.Store("Server")
			.getDir("Temp");

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		System.err.println("Get request received");
	}
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		System.err.println("Post request received");
		PrintWriter pw = resp.getWriter();
		String type = req.getHeader("type");
		if (type == null) {
			pw.println(Common.ERROR + ": Missing parameter 'type'");
			pw.close();
			return;
		}
		ServletContext ctx = getServletContext();
		String queryId = Utils.randomAlphaNum(20);
		File queryFile = new File(serverTempDir + "/" + queryId + "." + type);
		InputStream in = req.getInputStream();
		OutputStream out = new FileOutputStream(queryFile);
		Utils.readFully(in, out);
		System.out.println("done");
		in.close();
		out.close();
		pw.println(Common.OK);
		pw.println(queryId);
		pw.close();
		QueryData queryData = new QueryData(queryId, queryFile
				.getAbsolutePath());
		QueryQueue.get().addQuery(queryData);
	}
}

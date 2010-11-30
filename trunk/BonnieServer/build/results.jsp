<%@ page import="edu.ucla.bonnie.server.*" %>
<%
    String id = request.getParameter("id");
    String p_str = request.getParameter("p");
    int currPage = 1;
    try {
        currPage = Integer.parseInt(p_str);
    } catch (Exception e) {
    }
    int prevPage = currPage - 1;
    int nextPage = currPage + 1;
    int resultsPerPage = 10;
    QueryData data = QueryQueue.get().getQuery(id);
    Result[] results = data == null ? null : data.getResults();
%>
<html>
    <head>
        <title>Bonnie Longears - <%= results == null ? 0 : results.length %> Results (<%= id %>)</title>
		<script>
		function loadPlayerApplet(id, file) {
		    var play = document.getElementById(id);
		    if (play) {
		        play.innerHTML = '<applet code="edu.ucla.bonnie.player.Applet" archive="BonniePlayer.jar" width="32px" height="32px"><param name="file" value="' + file + '"></applet>';
		    }
		}
		</script>
        <style>
            body {
                width: 800px;
                margin: auto;
                padding-bottom: 50px;
                font-family: "Arial", sans-serif;
                font-size: 90%;
                line-height: 1.5em;
                border-left: #bbb 1px solid;
            }
            
            a {
                color: #ff6600;
            }
            
            hr {
                border: none;
                height: 1px;
                color: #bbb;
                background-color: #bbb;
            }
            
            td {
                padding: 0px;
            }
            
            img {
                padding: 0px;
                margin: 0px;
				border: none;
            }
            
            .result {
                padding: 10 0 10 30px;
            }
            
            .title {
                line-height: 2em;
                font-size: 1.5em;
                font-weight: bold;
            }
            
            .metadata {
            }
            
            .path {
                color: #777;
            }
            
            div.progress-container {
                border: 1px solid #ccc;
                width: 100px;
                margin: 2px 5px 2px 0;
                padding: 1px;
                background: white;
            }
            
            div.progress-container > div {
                background-color: #66ff33;
                height: 12px
            }
            
            #logo {
                padding: 20px;
            }
            
            #nav {
                margin: 30px;
                border-collapse: collapse;
            }
            
            #nav td {
                text-align: center;
                font-size: 1.2em;
                color: #999;
            }
            
            #nav td#selected {
                font-weight: bold;
                color: #000;
            }
            
            #footer li {
                display: inline;
                list-style-type: none;
                padding-right: 20px;
            }
        </style>
    </head>
</html>
<body width="800px">
    <a href="index.html"><img id="logo" src="bonnie_sm.png"/></a><hr/>
    <% if (results == null) { %>
        <div class="result">Sorry, no results</div>
    <% } else { %>
	    <% for (int i = resultsPerPage * (currPage - 1); i < Math.min(resultsPerPage * currPage, results.length); i++) { %>
			<div class="result">
			    <div class="title">
			        <span id="play<%= i %>">
			            <img src="player_play.png" onclick="loadPlayerApplet('play<%= i %>', '<%= results[i].filepath %>');" />
			        </span>
			        <% String reslink = "file:///" + results[i].filepath.replace('\\', '/'); %>
			        <a href="<%= reslink %>" target="_blank"><%= results[i].title %></a>
			        <div class="progress-container">
			            <div style="width:<%= results[i].matchPercent%>%">
			            </div>
			        </div>
			    </div>
			    <div class="metadata">
			        <b>Artist(s):</b>
			    </div>
			    <div class="path">
			        <%= results[i].filepath %>
			    </div>
			</div>
		<% } %>
	    <center>
	        <table id="nav">
	            <tr>
                    <% if (prevPage > 0) { %>
                    <td>
                        <a href="results.jsp?id=<%= id %>&p=<%= prevPage %>"><img src="prev.png"/></a>
                    </td>
                    <% } %>
	                <td>
	                    <img src="bo.png"/>
	                </td>
			        <% for (int i = 0; resultsPerPage * i < results.length; i++) { %>
	                <td>
	                    <img src="<%= currPage == (i + 1) ? "ns.png" : "nn.png" %>"/>
	                </td>
	                <% } %>
	                <td>
	                    <img src="ie.png"/>
	                </td>
                    <% if (resultsPerPage * (nextPage - 1) < results.length) { %>
	                <td>
	                    <a href="results.jsp?id=<%= id %>&p=<%= nextPage %>"><img src="next.png"/></a>
	                </td>
                    <% } %>
	            </tr>
	            <tr>
                    <% if (prevPage > 0) { %>
                    <td>
                        <a href="results.jsp?id=<%= id %>&p=<%= prevPage %>">Previous</a>
                    </td>
                    <% } %>
	                <td>
	                </td>
                    <% for (int i = 0; resultsPerPage * i < results.length; i++) { %>
	                <td <%= currPage == (i + 1) ? "id=\"selected\"" : "" %> >
	                    <a href="results.jsp?id=<%= id %>&p=<%= (i + 1) %>"><%= (i + 1) %></a>
	                </td>
	                <% } %>
	                <td>
	                </td>
	                <% if (resultsPerPage * (nextPage - 1) < results.length) { %>
	                <td>
	                    <a href="results.jsp?id=<%= id %>&p=<%= nextPage %>">Next</a>
	                </td>
	                <% } %>
	            </tr>
	        </table>
	    </center>
    <% } %>
    <center>
        <div id="footer">
            <ul>
                <li>
                    <a href="index.html">Bonnie Home</a>
                </li>
                <li>
                    <a href="privacy.html">Privacy</a>
                </li>
                <li>
                    <a href="about.html">About</a>
                </li>
            </ul>
        </div>
    </center>
</body>
</html>

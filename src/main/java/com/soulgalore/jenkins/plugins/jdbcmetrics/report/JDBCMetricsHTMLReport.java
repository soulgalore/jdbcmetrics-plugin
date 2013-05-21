/******************************************************
 * JDBCMetrics for Jenkins
 * 
 *
 * Copyright (C) 2013 by Peter Hedenskog (http://peterhedenskog.com)
 *
 ******************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License. You may obtain a copy of the License at
 * 
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is 
 * distributed  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   
 * See the License for the specific language governing permissions and limitations under the License.
 *
 *******************************************************
 */
package com.soulgalore.jenkins.plugins.jdbcmetrics.report;

import hudson.FilePath;
import hudson.model.AbstractBuild;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Set;

import com.soulgalore.crawler.core.CrawlerResult;
import com.soulgalore.crawler.core.HTMLPageResponse;
import com.soulgalore.crawler.util.StatusCode;
import com.soulgalore.jenkins.plugins.jdbcmetrics.JDBCMetricsBuilder;

/**
 * Create a simple HTML version of the fetched JDBC Metrics.
 * 
 */
public class JDBCMetricsHTMLReport {

	/**
	 * The name of the HTML file.
	 */
	public static final String FILENAME = "jdbcmetrics.html";

	private final PrintStream logger;

	private static final String CSS = "<style type='text/css'>body{font-family:verdana,arial,sans-serif;font-size:14px;}table{font-family:verdana,arial,sans-serif;font-size:11px;color:#333;border-width:1px;border-color:#666;border-collapse:collapse}th{border-width:1px;padding:8px;border-style:solid;border-color:#666;background-color:#dedede}td{border-width:1px;padding:8px;border-style:solid;border-color:#666;background-color:#fff}</style>";

	public JDBCMetricsHTMLReport(PrintStream theLogger) {
		logger = theLogger;
	}

	public void writeReport(Set<HTMLPageResponse> responses, FilePath workSpace,
			AbstractBuild build) {

		int nrOfPages = responses.size();
		logger.println("Start writing html report " + FILENAME
				+ " to workspace");
		StringBuilder html = new StringBuilder();
		html.append("<html><head>");
		html.append(CSS);
		html.append("</head><body><h1>JDBCMetrics</h1><p>Build time: ");
		html.append(build.getTime());
		html.append("</p>");
		html.append("<p>");
		html.append("Pages: ");
		html.append(nrOfPages);
		html.append(" Total reads: ");
		html.append(getTotal(JDBCMetricsBuilder.JDBC_READ_HEADER_NAME, responses));
		html.append(" Total writes: ");
		html.append(getTotal(JDBCMetricsBuilder.JDBC_WRITE_HEADER_NAME, responses));
		html.append("</p>");
		html.append("<p>");
		html.append("Reads per page: ");
		html.append( (float) (getTotal(JDBCMetricsBuilder.JDBC_READ_HEADER_NAME,
				responses) / responses.size()));
		html.append(" Writes per page: ");
		html.append( (float) (getTotal(JDBCMetricsBuilder.JDBC_WRITE_HEADER_NAME,
				responses) / responses.size()));
		html.append("</p>");
		html.append("<table>");
		html.append("<thead>");
		html.append("<tr>");
		html.append("<th>");
		html.append("URL");
		html.append("</th>");
		html.append("<th>");
		html.append("Reads");
		html.append("</th>");
		html.append("<th>");
		html.append("Writes");
		html.append("</th>");
		html.append("<th>");
		html.append("Read time (ms)");
		html.append("</th>");
		html.append("<th>");
		html.append("Write time (ms)");
		html.append("</th>");
		html.append("<th>");
		html.append("Time (ms)");
		html.append("</th>");
		html.append("<th>");
		html.append("Response");
		html.append("</th>");
		html.append("</tr>");
		html.append("</thead>");
		html.append("<tbody>");
		for (HTMLPageResponse resp : responses) {
			html.append(getResponseHTML(resp));
		}

		html.append("</tbody>");
		html.append("</table></body></html>");

		try {
			FilePath htmlFile = workSpace.child(FILENAME);
			htmlFile.write(html.toString(), "UTF-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String getResponseHTML(HTMLPageResponse resp) {
		StringBuilder html = new StringBuilder();
		html.append("<tr>");
		html.append("<td>");
		html.append(resp.getPageUrl().getUrl());
		html.append("</td>");
		html.append("<td>");
		html.append(resp
				.getHeaderValue(JDBCMetricsBuilder.JDBC_READ_HEADER_NAME));
		html.append("</td>");
		html.append("<td>");
		html.append(resp
				.getHeaderValue(JDBCMetricsBuilder.JDBC_WRITE_HEADER_NAME));
		html.append("</td>");
		html.append("<td>");
		html.append(resp
				.getHeaderValue(JDBCMetricsBuilder.JDBC_READ_TIME_HEADER_NAME));
		html.append("</td>");
		html.append("<td>");
		html.append(resp
				.getHeaderValue(JDBCMetricsBuilder.JDBC_WRITE_TIME_HEADER_NAME));
		html.append("</td>");
		
		html.append("<td>");
		html.append(resp.getFetchTime());
		html.append("</td>");
		html.append("<td>");
		html.append(StatusCode.toFriendlyName(resp.getResponseCode()));
		html.append("</td>");
		html.append("</tr>");
		return html.toString();
	}
	
	private int getTotal(String headerName,Set<HTMLPageResponse> result) {
		int total = 0;
		for (HTMLPageResponse resp : result) {
			String value = resp.getHeaderValue(headerName);
			if (value != null) {
				total += Integer.parseInt(value);
			}
		}
		return total;
	}
}

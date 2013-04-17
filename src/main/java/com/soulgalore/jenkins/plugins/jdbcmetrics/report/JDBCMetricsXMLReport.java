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

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Set;

import org.jdom2.CDATA;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.soulgalore.crawler.core.HTMLPageResponse;
import com.soulgalore.jenkins.plugins.jdbcmetrics.JDBCMetricsBuilder;

/**
 * Generate a generic XML report.
 *
 */
public class JDBCMetricsXMLReport {

	public static final String FILENAME = "jdbcmetrics.xml";
	private final int maxReads;
	private final int maxWrites;
	private final PrintStream logger;

	public JDBCMetricsXMLReport(int theMaxReads, int theMaxWrites,
			PrintStream theLogger) {
		maxReads = theMaxReads;
		maxWrites = theMaxWrites;
		logger = theLogger;
	}

	public void writeReport(Set<HTMLPageResponse> responses, FilePath workSpace) {

		Element root = new Element("jdbcmetrics");
		root.setAttribute("maxReads", "" + maxReads);
		root.setAttribute("maxWrites", "" + maxWrites);
		root.addContent(getResult(responses));
		Document doc = new Document(root);
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		try {
			FilePath junitXML = workSpace.child(FILENAME);
			outputter.output(doc, junitXML.write());
			logger.println("Wrote " + FILENAME + " to workspace dir");

		} catch (Exception e) {
			logger.println("Couldn't create XML file " + FILENAME + " "
					+ e.toString());
		}

	}

	private int getTotal(String headerName, Set<HTMLPageResponse> responses) {
		int total = 0;
		for (HTMLPageResponse resp : responses) {
			String value = resp.getHeaderValue(headerName);
			if (value != null) {
				total += Integer.parseInt(value);
			}
		}
		return total;
	}
	
	private int getMedian(String headerName, Set<HTMLPageResponse> responses) {
		
		int[] values = new int[responses.size()];
		int i =0;
		for (HTMLPageResponse resp : responses) {
			String value = resp.getHeaderValue(headerName);
			
			if (value != null) 
				values[i] = Integer.parseInt(value);
			else values[i] =0;
			i++;
		}
		Arrays.sort(values);
		
		int middle = values.length/2;
	    if (values.length%2 == 1) {
	        return values[middle];
	    } else {
	        return (values[middle-1] + values[middle]) / 2;
	    }
		
	}

	private Element getResult(Set<HTMLPageResponse> responses) {

		Element pages = new Element("pages");
		Element totalReads = new Element("totalReads");
		totalReads.addContent(""
				+ getTotal(JDBCMetricsBuilder.JDBC_READ_HEADER_NAME, responses));
		Element totalWrites = new Element("totalWrites");
		totalWrites.addContent(""
				+ getTotal(JDBCMetricsBuilder.JDBC_WRITE_HEADER_NAME, responses));
		pages.addContent(totalReads);
		pages.addContent(totalWrites);
		
		Element meanReadsPerPage = new Element("meanReadsPerPage");
		Element meanWritesPerPage = new Element("meanWritesPerPage");
		meanReadsPerPage.addContent(""
				+ (float) (getTotal(JDBCMetricsBuilder.JDBC_READ_HEADER_NAME,
						responses) /responses.size()));
		meanWritesPerPage.addContent(""
				+ (float) (getTotal(JDBCMetricsBuilder.JDBC_WRITE_HEADER_NAME,
						responses) / responses.size()));

		pages.addContent(meanReadsPerPage);
		pages.addContent(meanWritesPerPage);
			
		Element medianReadsPerPage = new Element("medianReadsPerPage");
		Element medianWritesPerPage = new Element("medianWritesPerPage");
		
		medianReadsPerPage.addContent(""
				+ getMedian(JDBCMetricsBuilder.JDBC_READ_HEADER_NAME,
						responses));
		medianWritesPerPage.addContent(""
				+ getMedian(JDBCMetricsBuilder.JDBC_WRITE_HEADER_NAME,
						responses));
		
		pages.addContent(medianReadsPerPage);
		pages.addContent(medianWritesPerPage);

		for (HTMLPageResponse resp : responses) {
			Element page = new Element("page");
			Element url = new Element("url");
			url.addContent(new CDATA(resp.getPageUrl().getUrl()));
			page.addContent(url);
			if (resp.getHeaderValue(JDBCMetricsBuilder.JDBC_READ_HEADER_NAME) != null) {
				Element reads = new Element("reads");
				reads.addContent(resp
						.getHeaderValue(JDBCMetricsBuilder.JDBC_READ_HEADER_NAME));
				page.addContent(reads);
			}
			if (resp.getHeaderValue(JDBCMetricsBuilder.JDBC_READ_HEADER_NAME) != null) {
				Element writes = new Element("writes");
				writes.addContent(resp
						.getHeaderValue(JDBCMetricsBuilder.JDBC_WRITE_HEADER_NAME));
				page.addContent(writes);

			}
			pages.addContent(page);

		}

		return pages;

	}
}

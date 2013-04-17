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

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
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

	private DescriptiveStatistics getStats(String headerName,
			Set<HTMLPageResponse> responses) {

		DescriptiveStatistics stats = new DescriptiveStatistics();

		for (HTMLPageResponse resp : responses) {
			String value = resp.getHeaderValue(headerName);
			if (value != null)
				stats.addValue(Integer.parseInt(value));
		}
		return stats;
	}

	private Element getResult(Set<HTMLPageResponse> responses) {

		Element pages = new Element("pages");
		Element totalReads = new Element("totalReads");
		Element totalWrites = new Element("totalWrites");
		Element meanReadsPerPage = new Element("meanReadsPerPage");
		Element meanWritesPerPage = new Element("meanWritesPerPage");
		Element medianReadsPerPage = new Element("medianReadsPerPage");
		Element medianWritesPerPage = new Element("medianWritesPerPage");
		Element maxReadsPerPage = new Element("maxReadsPerPage");
		Element maxWritesPerPage = new Element("maxWritesPerPage");
		Element minReadsPerPage = new Element("minReadsPerPage");
		Element minWritesPerPage = new Element("minWritesPerPage");
		Element percentilReadsPerPage = new Element("percentil90ReadsPerPage");
		Element percentilWritesPerPage = new Element("percentil90WritesPerPage");

		DescriptiveStatistics readStats = getStats(
				JDBCMetricsBuilder.JDBC_READ_HEADER_NAME, responses);
		DescriptiveStatistics writeStats = getStats(
				JDBCMetricsBuilder.JDBC_WRITE_HEADER_NAME, responses);

		totalReads.addContent("" + readStats.getSum());
		totalWrites.addContent("" + writeStats.getSum());
		pages.addContent(totalReads);
		pages.addContent(totalWrites);

		meanReadsPerPage.addContent("" + readStats.getMean());
		meanWritesPerPage.addContent("" + writeStats.getMean());

		pages.addContent(meanReadsPerPage);
		pages.addContent(meanWritesPerPage);

		medianReadsPerPage.addContent("" + readStats.getPercentile(50));
		medianWritesPerPage.addContent("" + writeStats.getPercentile(50));

		pages.addContent(medianReadsPerPage);
		pages.addContent(medianWritesPerPage);

		maxReadsPerPage.addContent("" + readStats.getMax());
		maxWritesPerPage.addContent("" + writeStats.getMax());

		pages.addContent(maxReadsPerPage);
		pages.addContent(maxWritesPerPage);

		minReadsPerPage.addContent("" + readStats.getMin());
		minWritesPerPage.addContent("" + writeStats.getMin());

		pages.addContent(minReadsPerPage);
		pages.addContent(minWritesPerPage);

		percentilReadsPerPage.addContent("" + readStats.getPercentile(90));
		percentilWritesPerPage.addContent("" + writeStats.getPercentile(90));

		pages.addContent(percentilReadsPerPage);
		pages.addContent(percentilWritesPerPage);

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

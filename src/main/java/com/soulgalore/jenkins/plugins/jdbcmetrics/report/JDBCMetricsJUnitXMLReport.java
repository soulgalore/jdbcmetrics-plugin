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
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.soulgalore.crawler.core.CrawlerResult;
import com.soulgalore.crawler.core.HTMLPageResponse;
import com.soulgalore.crawler.util.StatusCode;
import com.soulgalore.jenkins.plugins.jdbcmetrics.JDBCMetricsBuilder;

/**
 * Generate a JUnit XML report.
 *
 */
public class JDBCMetricsJUnitXMLReport {

	public static final String FILENAME = "jdbcmetrics-junit.xml";
	private final int maxReads;
	private final int maxWrites;
	private final String headerName;
	private final PrintStream logger;

	public JDBCMetricsJUnitXMLReport(int theMaxReads, int theMaxWrites,
			String theHeaderName, PrintStream theLogger) {
		maxReads = theMaxReads;
		maxWrites = theMaxWrites;
		headerName = theHeaderName;
		logger = theLogger;
	}

	public boolean verifyAndWriteReport(Set<HTMLPageResponse> responses,
			FilePath workSpace) {

		boolean isSuccess = true;

		Element root = new Element("testsuites");
		root.setAttribute("name", "the jdbcmetrics suites");
		root.addContent(getTestSuite(responses));

		if (getNumberOfFailures(responses) > 0)
			isSuccess = false;

		Document doc = new Document(root);
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());

		if (!isSuccess)
			logger.println(outputter.outputString(doc));

		try {
			FilePath junitXML = workSpace.child(FILENAME);
			outputter.output(doc, junitXML.write());
			logger.println("Wrote " + FILENAME + " to workspace dir");
			return isSuccess;

		} catch (Exception e) {
			logger.println("Couldn't create JunitXML file " + FILENAME + " "
					+ e.toString());
			return false;
		}

	}

	private Element getTestSuite(Set<HTMLPageResponse> responses) {

		Element testSuite = new Element("testsuite");
		testSuite.setAttribute("name", "Tested pages");

		testSuite.setAttribute("tests", ""
				+ (responses.size()));
		testSuite.setAttribute("failures", ""
				+ (getNumberOfFailures(responses)));

		long testSuiteTime = 0;
		for (HTMLPageResponse resp : responses)
			testSuiteTime += resp.getFetchTime();
		testSuite.setAttribute("time", "" + (testSuiteTime / 1000.0D));

		for (HTMLPageResponse resp : responses) {

			Element testCase = new Element("testcase");
			testCase.setAttribute("name", junitFriendlyUrlName(resp
					.getPageUrl().getUrl()));
			testCase.setAttribute("status", isMissingHeaders(resp) ? ""
					: getStatus(resp));
			testCase.setAttribute("time", "" + (resp.getFetchTime() / 1000.0D));
			if (isMissingHeaders(resp) || isFailure(resp)) {
				Element failure = new Element("failure");
				if (isMissingHeaders(resp))
					failure.setAttribute("message", missingHeadersMessage(resp));

				else
					failure.setAttribute("message", "The url "
							+ resp.getPageUrl().getUrl()
							+ " made too many database requests "
							+ getStatus(resp));

				testCase.addContent(failure);

			}
			testSuite.addContent(testCase);
		}
/*
		for (HTMLPageResponse resp : result.getNonWorkingUrls()) {
			Element testCase = new Element("testcase");
			testCase.setAttribute("name", junitFriendlyUrlName(resp
					.getPageUrl().getUrl()));
			testCase.setAttribute("status",
					StatusCode.toFriendlyName(resp.getResponseCode()));
			testCase.setAttribute("time", "" + (resp.getFetchTime() / 1000.0D));
			Element failure = new Element("failure");
			failure.setAttribute("message",
					"The url " + resp.getPageUrl().getUrl() + " got "
							+ StatusCode.toFriendlyName(resp.getResponseCode())
							+ " and is linked from "
							+ resp.getPageUrl().getReferer());
			testCase.addContent(failure);
			testSuite.addContent(testCase);
		}
*/
		return testSuite;

	}

	private int getNumberOfFailures(Set<HTMLPageResponse> responses) {
		int failures = 0;
		for (HTMLPageResponse resp : responses) {
			if (isMissingHeaders(resp) || isFailure(resp))
				failures++;
		}
	
		return failures;
	}

	private boolean isMissingHeaders(HTMLPageResponse response) {
		if (response.getHeaderValue(JDBCMetricsBuilder.JDBC_READ_HEADER_NAME) == null
				|| response
						.getHeaderValue(JDBCMetricsBuilder.JDBC_WRITE_HEADER_NAME) == null) {
			return true;
		}
		return false;
	}

	private boolean isFailure(HTMLPageResponse response) {

		int reads = Integer.parseInt(response
				.getHeaderValue(JDBCMetricsBuilder.JDBC_READ_HEADER_NAME));
		int writes = Integer.parseInt(response
				.getHeaderValue(JDBCMetricsBuilder.JDBC_WRITE_HEADER_NAME));

		if (reads > maxReads || writes > maxWrites)
			return true;
		return false;

	}

	private String getStatus(HTMLPageResponse response) {

		int reads = Integer.parseInt(response
				.getHeaderValue(JDBCMetricsBuilder.JDBC_READ_HEADER_NAME));
		int writes = Integer.parseInt(response
				.getHeaderValue(JDBCMetricsBuilder.JDBC_WRITE_HEADER_NAME));

		return "reads:" + reads + " writes:" + writes;
	}

	private String missingHeadersMessage(HTMLPageResponse response) {
		StringBuilder builder = new StringBuilder(
				"Missing JDBCMetrics information from the server. The server should listen on request header [");
		builder.append(headerName);
		builder.append("]. Check the console for more info. More information about JDBCMetrics here: https://github.com/soulgalore/jdbcmetrics");

		logger.println("Missing JDBCMetrics info from the server. Got the following headers (for page "
				+ response.getPageUrl().getUri() + " ):");

		logger.println("-------------");
		for (String key : response.getResponseHeaders().keySet()) {
			logger.println(key + " : " + response.getHeaderValue(key));
		}
		logger.println("-------------");
		return builder.toString();
	}

	private static String junitFriendlyUrlName(String url) {
		return url.replace("&", "_");
	}

}

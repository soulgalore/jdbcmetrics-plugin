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
package com.soulgalore.jenkins.plugins.jdbcmetrics;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;

import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.soulgalore.crawler.core.Crawler;
import com.soulgalore.crawler.core.CrawlerConfiguration;
import com.soulgalore.crawler.core.CrawlerResult;
import com.soulgalore.crawler.guice.CrawlModule;
import com.soulgalore.jenkins.plugins.jdbcmetrics.blocks.EnableAuthBlock;
import com.soulgalore.jenkins.plugins.jdbcmetrics.blocks.EnableCrawlerInternalsBlock;
import com.soulgalore.jenkins.plugins.jdbcmetrics.blocks.EnableCrawlerPathBlock;
import com.soulgalore.jenkins.plugins.jdbcmetrics.blocks.EnableHeaderNameBlock;
import com.soulgalore.jenkins.plugins.jdbcmetrics.report.JDBCMetricsHTMLReport;
import com.soulgalore.jenkins.plugins.jdbcmetrics.report.JDBCMetricsJUnitXMLReport;
import com.soulgalore.jenkins.plugins.jdbcmetrics.report.JDBCMetricsXMLReport;

/**
 * Plugin hat checks the amount of database reads & writes per page by crawling
 * your site, sends a specific request header and fetch response headers created
 * by https://github.com/soulgalore/jdbcmetrics. If the number of database reads
 * or writes are more than the configured limit, the plugin will fail.
 */
public class JDBCMetricsBuilder extends Builder {

	/**
	 * The start url of the crawl.
	 */
	private final String url;

	/**
	 * How deep you want to crawl.
	 */
	private final int level;

	/**
	 * The request header name, that will trigger JDBCMetrics to send the
	 * response headers.
	 */
	private final String headerName;

	/**
	 * The maximum numbers of database reads to break the build.
	 */
	private final int maxReads;

	/**
	 * The maximum numbers of database writes to break the build.
	 */
	private final int maxWrites;

	/**
	 * The login if you are using basic auth.
	 */
	private final String login;

	/**
	 * The password if you are using basic auth.
	 */
	private final String password;

	/**
	 * If the header name is checked or not.
	 */
	private final boolean checkHeader;

	/**
	 * If auth is checked or not.
	 */
	private final boolean checkAuth;

	/**
	 * If crawler internals is checked or not.
	 */
	private final boolean checkCrawler;

	/**
	 * If the crawler path specifics is checked or not.
	 */
	private final boolean checkCrawlerPath;

	/**
	 * The number of HTTP threads for the crawl client.
	 */
	private final String httpThreads;

	/**
	 * The number of threads in the pool that will parse the responses.
	 */
	private final String threadsPool;

	/**
	 * The socket timeout.
	 */
	private final String socketTimeout;

	/**
	 * The connection timeout.
	 */
	private final String connectionTimeout;

	/**
	 * Follow only this path in the crawl.
	 */
	private final String followPath;

	/**
	 * Do not include pages in this path in the crawl.
	 */
	private final String notFollowPath;

	/**
	 * If no request header name is configured, this will be sent to the server,
	 * with the values of <em>true</em> so that JDBCMetrics will send back the
	 * number of database reads & writes.
	 */
	public final static String DEFAULT_HEADER_NAME = "jdbcmetrics";

	/**
	 * The header name that holds the number of database reads for a page.
	 */
	public final static String JDBC_READ_HEADER_NAME = "nr-of-reads";

	/**
	 * The header name that holds the number of database writes for a page.
	 */
	public final static String JDBC_WRITE_HEADER_NAME = "nr-of-writes";

	@DataBoundConstructor
	public JDBCMetricsBuilder(String url, int level, int maxReads,
			int maxWrites, EnableAuthBlock checkAuth,
			EnableHeaderNameBlock checkHeader,
			EnableCrawlerInternalsBlock checkCrawler,
			EnableCrawlerPathBlock checkCrawlerPath) {

		this.url = url;
		this.level = level;
		this.maxReads = maxReads;
		this.maxWrites = maxWrites;

		this.headerName = checkHeader == null ? DEFAULT_HEADER_NAME
				: checkHeader.getHeaderName();
		this.checkHeader = checkHeader == null ? false : true;

		this.login = checkAuth == null ? "" : checkAuth.getLogin();
		this.password = checkAuth == null ? "" : checkAuth.getPassword();
		this.checkAuth = checkAuth == null ? false : true;

		this.httpThreads = checkCrawler == null ? "" : checkCrawler
				.getHttpThreads();
		this.threadsPool = checkCrawler == null ? "" : checkCrawler
				.getThreadsPool();
		this.socketTimeout = checkCrawler == null ? "" : checkCrawler
				.getSocketTimeout();
		this.connectionTimeout = checkCrawler == null ? "" : checkCrawler
				.getConnectionTimeout();
		this.checkCrawler = checkCrawler == null ? false : true;

		this.followPath = checkCrawlerPath == null ? "" : checkCrawlerPath
				.getFollowPath();
		this.notFollowPath = checkCrawlerPath == null ? "" : checkCrawlerPath
				.getNotFollowPath();
		this.checkCrawlerPath = checkCrawlerPath == null ? false : true;

	}

	public String getConnectionTimeout() {
		return connectionTimeout;
	}

	public String getFollowPath() {
		return followPath;
	}

	public String getHeaderName() {
		return headerName;
	}

	public String getHttpThreads() {
		return httpThreads;
	}

	public int getLevel() {
		return level;
	}

	public String getLogin() {
		return login;
	}

	public int getMaxReads() {
		return maxReads;
	}

	public int getMaxWrites() {
		return maxWrites;
	}

	public String getNotFollowPath() {
		return notFollowPath;
	}

	public String getPassword() {
		return password;
	}

	public String getSocketTimeout() {
		return socketTimeout;
	}

	public String getThreadsPool() {
		return threadsPool;
	}

	public String getUrl() {
		return url;
	}

	public boolean isCheckAuth() {
		return checkAuth;
	}

	public boolean isCheckCrawler() {
		return checkCrawler;
	}

	public boolean isCheckCrawlerPath() {
		return checkCrawlerPath;
	}

	public boolean isCheckHeader() {
		return checkHeader;
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	@Override
	public boolean perform(AbstractBuild build, Launcher launcher,
			BuildListener listener) {

		PrintStream logger = listener.getLogger();

		if (!setupAuth(logger))
			return false;

		setupCrawlerInternals();

		logger.println("Start crawling the URL:s, start with "
				+ url
				+ " sending request header:"
				+ headerName
				+ (socketTimeout != null ? " socketTimeout:" + socketTimeout
						: "")
				+ (connectionTimeout != null ? " connectionTimeout:"
						+ connectionTimeout : ""));	
		
		
		final CrawlerResult result = crawl();

		JDBCMetricsJUnitXMLReport reporter = new JDBCMetricsJUnitXMLReport(maxReads,
				maxWrites, headerName, logger);
		JDBCMetricsHTMLReport htmlReporter = new JDBCMetricsHTMLReport(logger);
		htmlReporter.writeReport(result, build.getWorkspace(), build);
		JDBCMetricsXMLReport xmlReporter = new JDBCMetricsXMLReport(maxReads,
				maxWrites, logger);
		xmlReporter.writeReport(result, build.getWorkspace());

		return  reporter.verifyAndWriteReport(result, build.getWorkspace());
	}

	private CrawlerResult crawl() {

		CrawlerConfiguration configuration = CrawlerConfiguration.builder()
				.setMaxLevels(level).setVerifyUrls(true)
				.setOnlyOnPath(followPath).setNotOnPath(notFollowPath)
				.setRequestHeaders(headerName + ":true").setStartUrl(url)
				.build();

		final Injector injector = Guice.createInjector(new CrawlModule());
		final Crawler crawler = injector.getInstance(Crawler.class);

		try {
			return crawler.getUrls(configuration);
		} finally {
			crawler.shutdown();
		}
	}

	private void setupCrawlerInternals() {
		if (!"".equals(httpThreads))
			System.setProperty(CrawlerConfiguration.MAX_THREADS_PROPERTY_NAME,
					httpThreads);
		else if (!"".equals(threadsPool))
			System.setProperty("com.soulgalore.crawler.threadsinworkingpool",
					threadsPool);
		else if (!"".equals(socketTimeout))
			System.setProperty(
					CrawlerConfiguration.SOCKET_TIMEOUT_PROPERTY_NAME,
					socketTimeout);
		else if (!"".equals(connectionTimeout))
			System.setProperty(
					CrawlerConfiguration.CONNECTION_TIMEOUT_PROPERTY_NAME,
					connectionTimeout);
	}

	private boolean setupAuth(PrintStream logger) {

		if (!"".equals(login) && !"".equals(password)) {

			try {
				URL u = new URL(url);
				String host = u.getHost()
						+ (u.getPort() != -1 ? ":" + u.getPort() : ":80");
				System.setProperty("com.soulgalore.crawler.auth", host + ":"
						+ login + ":" + password);
			} catch (MalformedURLException e) {
				logger.println(e.toString());
				return false;
			}

		}
		return true;

	}

	@Extension
	public static final class DescriptorImpl extends
			BuildStepDescriptor<Builder> {

		public FormValidation doCheckUrl(@QueryParameter String value)
				throws IOException, ServletException {
			if (value.length() == 0)
				return FormValidation.error("Please set a start url");
			if ((!value.startsWith("http://"))
					&& (!value.startsWith("https://")))
				return FormValidation
						.warning("The url must start with http:// or https:// !");
			return FormValidation.ok();
		}

		public String getDisplayName() {
			return "JDBCMetrics";
		}

		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			return true;
		}
	}
}

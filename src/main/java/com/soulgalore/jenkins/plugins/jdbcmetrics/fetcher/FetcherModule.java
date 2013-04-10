package com.soulgalore.jenkins.plugins.jdbcmetrics.fetcher;

import org.apache.http.client.HttpClient;

import com.soulgalore.crawler.core.HTMLPageResponseFetcher;

import com.soulgalore.crawler.core.impl.HTTPClientResponseFetcher;
import com.soulgalore.crawler.guice.AbstractPropertiesModule;
import com.soulgalore.crawler.guice.HttpClientProvider;

public class FetcherModule extends AbstractPropertiesModule {

	/**
	 * Bind the classes.
	 */
	@Override
	protected void configure() {
		super.configure();
		bind(HTMLPageResponseFetcher.class).to(HTTPClientResponseFetcher.class);
		bind(HttpClient.class).toProvider(HttpClientProvider.class);
		bind(Fetcher.class).to(DefaultFetcher.class);
	}

}

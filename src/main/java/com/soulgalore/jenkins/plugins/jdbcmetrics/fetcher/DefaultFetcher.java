package com.soulgalore.jenkins.plugins.jdbcmetrics.fetcher;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;
import com.soulgalore.crawler.core.HTMLPageResponse;
import com.soulgalore.crawler.core.HTMLPageResponseFetcher;
import com.soulgalore.crawler.core.PageURL;

public class DefaultFetcher implements Fetcher {

	private final HTMLPageResponseFetcher responseFetcher;
	
	@Inject
	public DefaultFetcher(HTMLPageResponseFetcher theResponseFetcher) {
		responseFetcher = theResponseFetcher;
	}
	
	public Set<HTMLPageResponse> get(Set<PageURL> urls, Map<String,String> requestHeaders) {
		
		Set<HTMLPageResponse> responses = new HashSet<HTMLPageResponse>();
		for (PageURL pageURL : urls) {
			responses.add(responseFetcher.get(pageURL, true, requestHeaders));
		}
		return responses;
	}

	public void shutdown() {
		responseFetcher.shutdown();		
	}
	
}

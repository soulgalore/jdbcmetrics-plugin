package com.soulgalore.jenkins.plugins.jdbcmetrics.fetcher;

import java.util.Map;
import java.util.Set;

import com.soulgalore.crawler.core.HTMLPageResponse;
import com.soulgalore.crawler.core.PageURL;

public interface Fetcher {

	public Set<HTMLPageResponse> get(Set<PageURL> urls, Map<String,String> requestHeaders);
	
	public void shutdown();
}
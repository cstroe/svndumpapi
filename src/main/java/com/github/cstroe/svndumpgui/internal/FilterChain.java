package com.github.cstroe.svndumpgui.internal;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.github.cstroe.svndumpgui.api.RepositoryFilter;
import com.github.cstroe.svndumpgui.api.Preamble;

public class FilterChain implements RepositoryFilter {

	private final Iterable<RepositoryFilter>  filters;
	private final int nThread;
	private final int nFilter;
	private final ExecutorService threadPool;

	public FilterChain(List<RepositoryFilter> filters, int nThread) {
		this.filters = filters;
		this.nThread = nThread;
		nFilter = filters.size();
		threadPool = Executors.newFixedThreadPool(nThread);
	}

	@Override
	public void consume(Preamble preamble) {
		final int batchSize = nFilter/nThread;
		for(int i = 0; i < nThread; i++) {
			final Lock lock = new ReentrantLock();
		}
	}
}

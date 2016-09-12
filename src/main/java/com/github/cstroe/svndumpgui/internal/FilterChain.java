package com.github.cstroe.svndumpgui.internal;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import com.github.cstroe.svndumpgui.api.RepositoryFilter;
import com.github.cstroe.svndumpgui.api.Preamble;
import com.github.cstroe.svndumpgui.api.Revision;
import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.ContentChunk;

public class FilterChain implements RepositoryFilter {

	private final RepositoryFilter[] filters;
	private final int nThread;
	private final int nFilter;
	private final ExecutorService threadPool;

	public FilterChain(List<RepositoryFilter> filters, int nThread) {
		this.filters = filters.toArray(new RepositoryFilter[filters.size()]);
		this.nThread = nThread;
		nFilter = this.filters.length;
		threadPool = Executors.newFixedThreadPool(nThread);
	}

	@Override
	public void consume(Preamble preamble) {
		final int batchSize = nFilter / nThread;
		int batchRemainder = nFilter % nThread;

		final ArrayList<BlockingQueue<Preamble>> pipes = new ArrayList<BlockingQueue<Preamble>>(nThread);
		for (int i = 0; i < pipes.size(); i++)
			pipes.set(i, new ArrayBlockingQueue<Preamble>(1));

		int start = 0;
		for (int i = 0; i < pipes.size(); i++) {
			int end = start + nFilter;
			if (batchRemainder-- > 0)
				end++;

			final int ti = i;
			final int tstart = start;
			final int tend = end;
			threadPool.submit(() -> {
				try {
					Preamble p = pipes.get(ti).take();

					for (int j = tstart; j < tend; j++)
						filters[j].consume(p);

					if(ti+1 < nThread)
						pipes.get(ti+1).put(p);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					return;
				}
			});
			start = end;
		}
	}

	@Override
    public void consume(Revision revision){}
	@Override
    public void endRevision(Revision revision){}
	@Override
    public void consume(Node node){}
	@Override
    public void endNode(Node node){}
	@Override
    public void consume(ContentChunk chunk){}
	@Override
    public void endChunks(){}
 	@Override
   public void finish(){}
}

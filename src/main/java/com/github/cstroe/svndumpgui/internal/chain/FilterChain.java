package com.github.cstroe.svndumpgui.internal.chain;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Consumer;
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
	private Pipeline<Preamble> preamblePipe;
	private Pipeline<Revision> revisionPipe;

	public FilterChain(List<RepositoryFilter> filters, int nThread) {
		this.filters = filters.toArray(new RepositoryFilter[filters.size()]);
		this.nThread = nThread;
		nFilter = this.filters.length;
		threadPool = Executors.newFixedThreadPool(nThread);
	}

	@Override
	public void consume(Preamble preamble) {
		final ArrayList<Consumer<Preamble>> ops = new ArrayList<Consumer<Preamble>>(nThread);
		for (int i = 0; i < ops.size(); i++) {
			final int ti = i;
			ops.set(i, p -> filters[ti].consume(p));
		}

		if(preamblePipe == null) {
			preamblePipe = new Pipeline<Preamble>(ops, threadPool, nThread);
			preamblePipe.run();
		}
		preamblePipe.start(preamble);
	}

	@Override
    public void consume(Revision revision){
		final ArrayList<Consumer<Revision>> ops = new ArrayList<Consumer<Revision>>(nThread);
		for (int i = 0; i < ops.size(); i++) {
			final int ti = i;
			ops.set(i, r -> filters[ti].consume(r));
		}

		if(revisionPipe == null) {
			revisionPipe = new Pipeline<Revision>(ops, threadPool, nThread);
			revisionPipe.run();
		}
		revisionPipe.start(revision);
	}

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

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
	private Pipeline<Node> nodePipe;
	private Pipeline<ContentChunk> chunkPipe;
	private Pipeline<Object> emptyPipe;

	public FilterChain(List<RepositoryFilter> filters, int nThread) {
		this.filters = filters.toArray(new RepositoryFilter[filters.size()]);
		nFilter = this.filters.length;
		this.nThread = nThread;
		threadPool = Executors.newFixedThreadPool(nThread);
	}

	@Override
	public void consume(Preamble preamble) {
		final ArrayList<Consumer<Preamble>> ops = new ArrayList<Consumer<Preamble>>(nFilter);
		for (int i = 0; i < ops.size(); i++) {
			final int ti = i;
			ops.set(i, p -> filters[ti].consume(p));
		}

		if(preamblePipe == null)
			preamblePipe = new Pipeline<Preamble>(ops, threadPool, nThread);

		preamblePipe.run();
		preamblePipe.start(preamble);
	}

	@Override
	public void consume(Revision revision){
		final ArrayList<Consumer<Revision>> ops = new ArrayList<Consumer<Revision>>(nFilter);
		for (int i = 0; i < ops.size(); i++) {
			final int ti = i;
			ops.set(i, r -> filters[ti].consume(r));
		}

		if(revisionPipe == null)
			revisionPipe = new Pipeline<Revision>(ops, threadPool, nThread);

		revisionPipe.run();
		revisionPipe.start(revision);
	}

	@Override
	public void endRevision(Revision revision){
		final ArrayList<Consumer<Revision>> ops = new ArrayList<Consumer<Revision>>(nFilter);
		for (int i = 0; i < ops.size(); i++) {
			final int ti = i;
			ops.set(i, r -> filters[ti].endRevision(r));
		}

		if(revisionPipe == null)
			revisionPipe = new Pipeline<Revision>(ops, threadPool, nThread);

		revisionPipe.run();
		revisionPipe.start(revision);
	}

	@Override
	public void consume(Node node){
		final ArrayList<Consumer<Node>> ops = new ArrayList<Consumer<Node>>(nFilter);
		for (int i = 0; i < ops.size(); i++) {
			final int ti = i;
			ops.set(i, n -> filters[ti].consume(n));
		}

		if(nodePipe == null)
			nodePipe = new Pipeline<Node>(ops, threadPool, nThread);

		nodePipe.run();
		nodePipe.start(node);
	}

	@Override
	public void endNode(Node node){
		final ArrayList<Consumer<Node>> ops = new ArrayList<Consumer<Node>>(nFilter);
		for (int i = 0; i < ops.size(); i++) {
			final int ti = i;
			ops.set(i, n -> filters[ti].endNode(n));
		}

		if(nodePipe == null)
			nodePipe = new Pipeline<Node>(ops, threadPool, nThread);

		nodePipe.run();
		nodePipe.start(node);
	}

	@Override
	public void consume(ContentChunk chunk){
		final ArrayList<Consumer<ContentChunk>> ops = new ArrayList<Consumer<ContentChunk>>(nFilter);
		for (int i = 0; i < ops.size(); i++) {
			final int ti = i;
			ops.set(i, c -> filters[ti].consume(c));
		}

		if(chunkPipe == null)
			chunkPipe = new Pipeline<ContentChunk>(ops, threadPool, nThread);

		chunkPipe.run();
		chunkPipe.start(chunk);
	}

	@Override
	public void endChunks(){
		final ArrayList<Consumer<Object>> ops = new ArrayList<Consumer<Object>>(nFilter);
		for (int i = 0; i < ops.size(); i++) {
			final int ti = i;
			ops.set(i, o -> filters[ti].endChunks());
		}

		if(emptyPipe == null)
			emptyPipe = new Pipeline<Object>(ops, threadPool, nThread);

		emptyPipe.run();
		emptyPipe.start(new Object());
	}

	@Override
	public void finish(){
		final ArrayList<Consumer<Object>> ops = new ArrayList<Consumer<Object>>(nFilter);
		for (int i = 0; i < ops.size(); i++) {
			final int ti = i;
			ops.set(i, o -> filters[ti].finish());
		}

		if(emptyPipe == null)
			emptyPipe = new Pipeline<Object>(ops, threadPool, nThread);

		emptyPipe.run();
		emptyPipe.start(new Object());
	}
}

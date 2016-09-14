package com.github.cstroe.svndumpgui.internal.chain;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.UnaryOperator;
import com.github.cstroe.svndumpgui.api.FilterChain;
import com.github.cstroe.svndumpgui.api.RepositoryFilter;
import com.github.cstroe.svndumpgui.api.Preamble;
import com.github.cstroe.svndumpgui.api.Revision;
import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.ContentChunk;

public class FilterChainImpl implements FilterChain {

	private final RepositoryFilter[] filters;
	private final Lock[] locks;
	private final int nThread;
	private final int nFilter;
	private final ExecutorService threadPool;
	private Pipeline<Preamble> preamblePipe;
	private Pipeline<Revision> revisionPipe;
	private Pipeline<Node> nodePipe;
	private Pipeline<ContentChunk> chunkPipe;
	private Pipeline<Object> emptyPipe;

	public FilterChainImpl(List<RepositoryFilter> filters, int nThread) {
		this.filters = filters.toArray(new RepositoryFilter[filters.size()]);
		nFilter = this.filters.length;
		locks = new Lock[nFilter];
		for (int i = 0; i < nFilter; i++)
			locks[i] = new ReentrantLock();
		this.nThread = nThread;
		threadPool = Executors.newFixedThreadPool(nThread);
	}

	@Override
	public void consume(Preamble preamble) {
		final ArrayList<UnaryOperator<Preamble>> ops = new ArrayList<UnaryOperator<Preamble>>(nFilter);
		for (int i = 0; i < ops.size(); i++) {
			final int ti = i;
			ops.set(i, p -> filters[ti].consume(p));
		}

		if(preamblePipe == null)
			preamblePipe = new Pipeline<Preamble>(ops, locks, threadPool, nThread);

		preamblePipe.run();
		preamblePipe.start(preamble);
	}

	@Override
	public void consume(Revision revision){
		final ArrayList<UnaryOperator<Revision>> ops = new ArrayList<UnaryOperator<Revision>>(nFilter);
		for (int i = 0; i < ops.size(); i++) {
			final int ti = i;
			ops.set(i, r -> filters[ti].consume(r));
		}

		if(revisionPipe == null)
			revisionPipe = new Pipeline<Revision>(ops, locks, threadPool, nThread);

		revisionPipe.run();
		revisionPipe.start(revision);
	}

	@Override
	public void endRevision(Revision revision){
		final ArrayList<UnaryOperator<Revision>> ops = new ArrayList<UnaryOperator<Revision>>(nFilter);
		for (int i = 0; i < ops.size(); i++) {
			final int ti = i;
			ops.set(i, r -> filters[ti].endRevision(r));
		}

		if(revisionPipe == null)
			revisionPipe = new Pipeline<Revision>(ops, locks, threadPool, nThread);

		revisionPipe.run();
		revisionPipe.start(revision);
	}

	@Override
	public void consume(Node node){
		final ArrayList<UnaryOperator<Node>> ops = new ArrayList<UnaryOperator<Node>>(nFilter);
		for (int i = 0; i < ops.size(); i++) {
			final int ti = i;
			ops.set(i, n -> filters[ti].consume(n));
		}

		if(nodePipe == null)
			nodePipe = new Pipeline<Node>(ops, locks, threadPool, nThread);

		nodePipe.run();
		nodePipe.start(node);
	}

	@Override
	public void endNode(Node node){
		final ArrayList<UnaryOperator<Node>> ops = new ArrayList<UnaryOperator<Node>>(nFilter);
		for (int i = 0; i < ops.size(); i++) {
			final int ti = i;
			ops.set(i, n -> filters[ti].endNode(n));
		}

		if(nodePipe == null)
			nodePipe = new Pipeline<Node>(ops, locks, threadPool, nThread);

		nodePipe.run();
		nodePipe.start(node);
	}

	@Override
	public void consume(ContentChunk chunk){
		final ArrayList<UnaryOperator<ContentChunk>> ops = new ArrayList<UnaryOperator<ContentChunk>>(nFilter);
		for (int i = 0; i < ops.size(); i++) {
			final int ti = i;
			ops.set(i, c -> filters[ti].consume(c));
		}

		if(chunkPipe == null)
			chunkPipe = new Pipeline<ContentChunk>(ops, locks, threadPool, nThread);

		chunkPipe.run();
		chunkPipe.start(chunk);
	}

	@Override
	public void endChunks(){
		final ArrayList<UnaryOperator<Object>> ops = new ArrayList<UnaryOperator<Object>>(nFilter);
		for (int i = 0; i < ops.size(); i++) {
			final int ti = i;
			ops.set(i, o -> filters[ti].endChunks());
		}

		if(emptyPipe == null)
			emptyPipe = new Pipeline<Object>(ops, locks, threadPool, nThread);

		emptyPipe.run();
		emptyPipe.start(new Object());
	}

	@Override
	public void finish(){
		final ArrayList<UnaryOperator<Object>> ops = new ArrayList<UnaryOperator<Object>>(nFilter);
		for (int i = 0; i < ops.size(); i++) {
			final int ti = i;
			ops.set(i, o -> filters[ti].finish());
		}

		if(emptyPipe == null)
			emptyPipe = new Pipeline<Object>(ops, locks, threadPool, nThread);

		emptyPipe.run();
		emptyPipe.start(new Object());
	}
}

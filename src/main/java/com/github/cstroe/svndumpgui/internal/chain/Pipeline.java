package com.github.cstroe.svndumpgui.internal.chain;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class Pipeline<T> {

	private final ArrayList<BlockingQueue<T>> pipes;
	private final ArrayList<Consumer<T>> filters;
	private final int nFilter;
	private final int nThread;
	private final ExecutorService threadPool;

	public Pipeline(ArrayList<Consumer<T>> filters, ExecutorService threadPool, int nThread) {
		nFilter = filters.size();
		pipes = new ArrayList<BlockingQueue<T>>(filters.size());
		for (int i = 0; i < pipes.size(); i++)
			pipes.set(i, new ArrayBlockingQueue<T>(1));
		this.filters = filters;
		this.threadPool = threadPool;
		this.nThread = nThread;
	}

	public void start(T svnChunk) {
		try {
			pipes.get(0).put(svnChunk);
		} catch (InterruptedException ie) {
			return;
		}
	}

	public void run() {
		final int batchSize = nFilter / nThread;
		int batchRemainder = nFilter % nThread;

		int start = 0;
		for (int i = 0; i < nThread; i++) {
			int end = start + nFilter;
			if (batchRemainder-- > 0)
				end++;

			final int ti = i;
			final int tstart = start;
			final int tend = end;
			threadPool.submit(() -> {
				try {
					T chunk = pipes.get(ti).take();

					for (int j = tstart; j < tend; j++)
						filters.get(j).accept(chunk);

					if(ti+1 < nThread)
						pipes.get(ti+1).put(chunk);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
				}
			});
			start = end;
		}
	}
}

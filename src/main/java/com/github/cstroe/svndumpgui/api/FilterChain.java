package com.github.cstroe.svndumpgui.api;

public interface FilterChain {
	void consume(Preamble preamble);
	void consume(Revision revision);
	void endRevision(Revision revision);
	void consume(Node node);
	void endNode(Node node);
	void consume(ContentChunk chunk);
	void endChunks();
	void finish();
}

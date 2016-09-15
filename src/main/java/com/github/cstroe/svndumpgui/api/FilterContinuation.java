package com.github.cstroe.svndumpgui.api;

import java.util.function.UnaryOperator;
import java.util.function.BooleanSupplier;

public interface FilterContinuation {
	void proceedConsume(Preamble preamble);
	void proceedConsume(Revision revision);
	void proceedConsume(Node node);
	void proceedConsume(ContentChunk chunk);
	void proceedEnd(Revision revision);
	void proceedEnd(Node node);
	void proceedEndChunks();
	void proceedFinish();

	void setConsumePreamble(UnaryOperator<Preamble> op);
	void setConsumeRevision(UnaryOperator<Revision> op);
	void setConsumeNode(UnaryOperator<Node> op);
	void setConsumeContentChunk(UnaryOperator<ContentChunk> op);
	void setEndRevision(UnaryOperator<Revision> op);
	void setEndNode(UnaryOperator<Node> op);
	void setEndChunks(BooleanSupplier op);
	void setFinish(BooleanSupplier op);
}

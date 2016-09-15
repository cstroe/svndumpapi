package com.github.cstroe.svndumpgui.internal.chain;

public class NewFilterChainImpl implements NewFilterChain {

	private FilterContinuation startCont;
	private FilterContinuationImpl currentCont;

	public FilterContinuation newFilter() {
		FilterContinuationImpl cont =  new FilterContinuationImpl();
		if(currentCont != null)
			currentCont.setNext(cont);
		else
			startCont = cont;
		currentCont = cont;
		return cont;
	}
}

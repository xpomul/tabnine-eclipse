package net.winklerweb.tabnine.core;

import java.util.List;

/**
 * Interface for the Proposal Completion Cache.
 * 
 * The Proposal Completion Cache stores the currently proposed completions. The
 * purpose is that when the user wants to actually apply a proposal, we need to
 * look up which proposal the user actually wants to apply. So, whenever we
 * request proposals, we store the proposal results in the cache; and whenever
 * we issue another request, we invalidate the cache again, until the new
 * results are received and cached once again.
 * 
 * @author Stefan Winkler
 */
public interface ITabnineCompletionCache {

	/**
	 * Invalidates the cache.
	 */
	public void invalidateCompletions();

	/**
	 * Stores the given completions in the cache.
	 * 
	 * @param completions the completions to store in the cache.
	 */
	public void cacheCompletions(List<CompletionProposal> completions);

	/**
	 * Returns the completions stored in the cache.
	 * 
	 * @return the completions currently stored in the cache.
	 */
	public List<CompletionProposal> getCachedCompletions();
}

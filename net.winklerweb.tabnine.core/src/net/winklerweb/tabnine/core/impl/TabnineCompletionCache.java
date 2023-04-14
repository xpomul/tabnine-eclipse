package net.winklerweb.tabnine.core.impl;

import java.util.Collections;
import java.util.List;

import org.osgi.service.component.annotations.Component;

import com.tabnine.Log;

import net.winklerweb.tabnine.core.ITabnineCompletionCache;
import net.winklerweb.tabnine.core.CompletionProposal;

/**
 * Completion Proposal Cache implementation
 * 
 * @author Stefan Winkler 
 */
@Component
public class TabnineCompletionCache implements ITabnineCompletionCache {

	/**
	 * The list that actually caches the current completions.
	 */
	private List<CompletionProposal> cachedCompletions = Collections.emptyList();

	@Override
	public void invalidateCompletions() {
		Log.debug("Cache invalidated");
		cachedCompletions = Collections.emptyList();
	}

	@Override
	public void cacheCompletions(List<CompletionProposal> completions) {
		cachedCompletions = completions;
	}

	@Override
	public List<CompletionProposal> getCachedCompletions() {
		return cachedCompletions;
	}
}

package net.winklerweb.tabnine.core;

import java.util.List;

import org.eclipse.jface.text.ITextViewer;

/**
 * Interface for the TabNine completion service.
 *
 * This service is responsible for providing completion proposals for the given text viewer state and position.
 * 
 * @author Stefan Winkler
 */
public interface ITabnineCompletionService {
	
	/**
	 * Send a completion request to the TabNine binary and wrap the result in a list of completion proposals. 
	 * 
	 * @param textViewer the target text viewer.
	 * @param offset the current cursor position in the viewer.
	 * @param path the complete name of the file which is opened in the textViewer.
	 * @return the resulting list of completion proposals.
	 */
	List<CompletionProposal> complete(ITextViewer textViewer, int offset, String path);
}

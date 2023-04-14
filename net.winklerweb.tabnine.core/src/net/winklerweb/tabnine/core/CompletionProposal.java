package net.winklerweb.tabnine.core;

import org.eclipse.jface.text.ITextViewer;

/**
 * Model class for a Tabnine completion proposal.
 * 
 * A completion proposal is tied to an exact {@link ITextViewer} and cursor
 * position, and contains the proposals acquired by the TabNine binary.
 * 
 * @author Stefan Winkler
 */
public class CompletionProposal {

	/**
	 * The target viewer to which the proposal should be applied.
	 */
	private final ITextViewer targetViewer;

	/**
	 * The cursor position to which the proposal is tied.
	 */
	private final int cursorPosition;

	/**
	 * The number of characters before the current cursor position that are included
	 * in the completion proposal (basically old_prefix).
	 */
	private final int prefixOverlap;

	/**
	 * The number of characters by which the current cursor position is shifted
	 * after the proposal is applied.
	 */
	private final int cursorOffset;

	/**
	 * The total number of characters which are replaced in the document (basically
	 * old_prefix + old_suffix)
	 */
	private final int replacementLength;

	/**
	 * The actual completion proposal string which should be applied to the
	 * document.
	 */
	private final String replacementString;

	/**
	 * Create a new completion proposal object.
	 * 
	 * @param targetViewer      the target viewer
	 * @param cursorPosition    the current cursor position in the target viewer
	 * @param replacementString the replacement string (the actual proposal)
	 * @param prefixOverlap     the number of characters before the cursor position
	 *                          that are included in the replacement string
	 * @param replacementLength the total number of characters which are replaced in
	 *                          the document
	 * @param cursorOffset      the number of characters by which the cursor
	 *                          position is shifted after the proposal is applied
	 */
	public CompletionProposal(ITextViewer targetViewer, int cursorPosition, String replacementString, int prefixOverlap,
			int replacementLength, int cursorOffset) {
		this.targetViewer = targetViewer;
		this.prefixOverlap = prefixOverlap;
		this.cursorPosition = cursorPosition;
		this.cursorOffset = cursorOffset;
		this.replacementLength = replacementLength;
		this.replacementString = replacementString;
	}

	/**
	 * Get the target viewer.
	 * 
	 * @return the target viewer
	 */
	public ITextViewer getTargetViewer() {
		return targetViewer;
	}

	/**
	 * Get the current cursor position in the target viewer.
	 * 
	 * @return the current cursor position in the target viewer
	 */
	public int getCursorPosition() {
		return cursorPosition;
	}

	/**
	 * Get the number of characters before the current cursor position that are
	 * included in the completion proposal.
	 * 
	 * @return the number of characters before the current cursor position that are
	 *         included in the completion proposal
	 */
	public int getPrefixOverlap() {
		return prefixOverlap;
	}

	/**
	 * Get the number of characters by which the current cursor position is shifted
	 * after the proposal is applied.
	 * 
	 * @return the number of characters by which the current cursor position is
	 *         shifted
	 */
	public int getCursorOffset() {
		return cursorOffset;
	}

	/**
	 * Get the total number of characters which are replaced in the document.
	 * 
	 * @return the total number of characters which are replaced in the document
	 */
	public int getReplacementLength() {
		return replacementLength;
	}

	/**
	 * Get the actual completion proposal string which should be applied to the
	 * document.
	 * 
	 * @return the completion proposal string
	 */
	public String getReplacementString() {
		return replacementString;
	}
	
	@Override
	public String toString() {
		return getReplacementString() + " @" + getCursorPosition();
	}
}

package net.winklerweb.tabnine.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * Tracks the TabNine activation per editor.
 * 
 * When TabNine completion proposals are activated for an editor, we add a post
 * selection changed listener that updates the completions when the cursor
 * position changes.
 * 
 * @author Stefan Winkler
 */
public class TabnineCompletionActivationManager {

	/**
	 * The singleton instance
	 */
	private static final TabnineCompletionActivationManager INSTANCE = new TabnineCompletionActivationManager();

	/**
	 * A map of installed listeners per editor. TabNine completion is active for an
	 * editor, if it is contained in this map's keySet.
	 */
	private Map<IEditorPart, TabnineTextViewerCompletionListener> listeners = new HashMap<>();

	/**
	 * Private constructor, as this is a singleton.
	 */
	private TabnineCompletionActivationManager() {
		ensureColorsInitialized();
		InstanceScope.INSTANCE.getNode(Constants.PREFERENCE_SCOPE)
				.addPreferenceChangeListener(event -> ensureColorsInitialized());
	}

	/**
	 * Get the singleton instance.
	 * 
	 * @return the singleton instance
	 */
	public static final TabnineCompletionActivationManager getInstance() {
		return INSTANCE;
	}

	private static void ensureColorsInitialized() {
		var preferences = new ScopedPreferenceStore(InstanceScope.INSTANCE, Constants.PREFERENCE_SCOPE);
		var backgroundColor = PreferenceConverter.getColor(preferences, Constants.PREF_KEY_BACKGROUND_COLOR);
		if (backgroundColor.equals(PreferenceConverter.COLOR_DEFAULT_DEFAULT)) {
			backgroundColor = new RGB(0, 0, 0);
		}
		JFaceResources.getColorRegistry().put(Constants.BACKGROUND_COLOR_KEY, backgroundColor);

		var foregroundColor = PreferenceConverter.getColor(preferences, Constants.PREF_KEY_TEXT_COLOR);
		if (foregroundColor.equals(PreferenceConverter.COLOR_DEFAULT_DEFAULT)) {
			foregroundColor = new RGB(128, 128, 128);
		}
		JFaceResources.getColorRegistry().put(Constants.FOREGROUND_COLOR_KEY, foregroundColor);
	}

	/**
	 * Check if TabNine completion is active for an editor.
	 * 
	 * @param editorPart the editor to check
	 * @return <code>true</code> if TabNine completion is active for the editor,
	 *         <code>false</code> else.
	 */
	public boolean isTabnineCompletionActive(IEditorPart editorPart) {
		return listeners.containsKey(editorPart);
	}

	/**
	 * Activate TabNine completion for an editor.
	 * 
	 * @param editorPart the editor to activate the TabNine completion for
	 */
	public void activateTabnineCompletion(IEditorPart editorPart) {
		// don't bother if the editor is already active
		if (isTabnineCompletionActive(editorPart)) {
			return;
		}

		String pathOfEditor = null;
		var editorInput = editorPart.getEditorInput();
		if (editorInput instanceof IPathEditorInput) {
			pathOfEditor = ((IPathEditorInput)editorInput).getPath().toOSString();
		}
		
		// get the source viewer for the editor
		var possiblyTextViewer = editorPart.getAdapter(ITextOperationTarget.class);
		if (possiblyTextViewer instanceof ITextViewer) {
			var textViewer = (ITextViewer) possiblyTextViewer;
			// create a listener this editor
			var listener = new TabnineTextViewerCompletionListener(textViewer, pathOfEditor);
			
			// add the listener and remember it in the listeners map
			listeners.put(editorPart, listener);
			
			// add a dispose listener to remove the listener when the editor is closed
			textViewer.getTextWidget().addDisposeListener(e -> {
				var activeListener = listeners.remove(editorPart);
				if (activeListener != null) {
					activeListener.dispose();
				}
			});
		}
	}

	public void deactivateTabnineCompletion(IEditorPart editorPart) {
		listeners.remove(editorPart).dispose();
	}
}

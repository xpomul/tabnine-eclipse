package net.winklerweb.tabnine.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.ServiceCaller;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.texteditor.ITextEditor;

import com.tabnine.Log;

import net.winklerweb.tabnine.core.ITabnineCompletionCache;
import net.winklerweb.tabnine.ui.Constants;
import net.winklerweb.tabnine.ui.TabnineCompletionActivationManager;

/**
 * Command handler to handle the application of a tabnine completion to the active editor.
 * 
 * @author Stefan Winkler
 */
public class InsertTabnineCompletionHandler extends AbstractHandler {

	/** 
	 * The cache of tabnine completions.
	 */
	private ServiceCaller<ITabnineCompletionCache> completionCache = new ServiceCaller<>(
			InsertTabnineCompletionHandler.class, ITabnineCompletionCache.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// get the active editor from the event
		var activeEditor = HandlerUtil.getActiveEditorChecked(event);
		if (!(activeEditor instanceof ITextEditor)) {
			return null;
		} else {
			// check if tabnine completion is active and if not, activate it
			var activationManager = TabnineCompletionActivationManager.getInstance();
			if (!activationManager.isTabnineCompletionActive(activeEditor)) {
				var prefs = new ScopedPreferenceStore(InstanceScope.INSTANCE, Constants.PREFERENCE_SCOPE);
				var mode = prefs.getString(Constants.PREF_KEY_MODE);
				if (Constants.MODE_ALWAYS_OFF.equals(mode)) 
				{
					// never activate in this case
					return null;
				}

				activationManager.activateTabnineCompletion(activeEditor);
				return null;
			}
			
			// check which completion to insert
			var index = getIndexFromEvent(event);
			completionCache.call(cache -> {
				var completions = cache.getCachedCompletions();
				if (index < completions.size()) {
					// get the selected completion
					var selectedCompletion = completions.get(index);
					try {
						ITextViewer viewer = selectedCompletion.getTargetViewer();
						IDocument document = viewer.getDocument();

						// find the cursor position
						var cursorPosition = selectedCompletion.getCursorPosition();
						var replacementOverlap = selectedCompletion.getPrefixOverlap();
						var replacementOffset = cursorPosition - replacementOverlap;

						// and insert the completion
						document.replace(replacementOffset, selectedCompletion.getReplacementLength(),
								selectedCompletion.getReplacementString());
						viewer.setSelectedRange(cursorPosition + selectedCompletion.getCursorOffset(), 0);
					} catch (BadLocationException e) {
						Log.error("Unexpected exception", e);
					}
				}
			});
		}
		return null;
	}

	private int getIndexFromEvent(ExecutionEvent event) {
		try {
			var indexString = event.getParameter("index");
			if (indexString == null) {
				return 0;
			}
			var index = Integer.parseInt(indexString);
			return index;
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	@Override
	public void dispose() {
		completionCache.unget();
		super.dispose();
	}
}

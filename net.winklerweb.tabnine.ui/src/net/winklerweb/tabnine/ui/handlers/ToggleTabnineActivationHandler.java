package net.winklerweb.tabnine.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.texteditor.ITextEditor;

import net.winklerweb.tabnine.ui.Constants;
import net.winklerweb.tabnine.ui.TabnineCompletionActivationManager;

public class ToggleTabnineActivationHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		var prefs = new ScopedPreferenceStore(InstanceScope.INSTANCE, Constants.PREFERENCE_SCOPE);
		var mode = prefs.getString(Constants.PREF_KEY_MODE);
		if (Constants.MODE_ALWAYS_OFF.equals(mode)) 
		{
			// never activate in this case
			return null;
		}
		
		// get the active editor from the event
		var activeEditor = HandlerUtil.getActiveEditorChecked(event);
		if (!(activeEditor instanceof ITextEditor)) {
			return null;
		}
		
		// check if tabnine completion is active and if not, activate it
		var activationManager = TabnineCompletionActivationManager.getInstance();
		if (!activationManager.isTabnineCompletionActive(activeEditor)) {
			activationManager.activateTabnineCompletion(activeEditor);
		} else {
			activationManager.deactivateTabnineCompletion(activeEditor);
		}
		return null;
	}
}
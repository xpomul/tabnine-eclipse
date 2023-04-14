package net.winklerweb.tabnine.ui;

import java.util.Optional;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.internal.e4.compatibility.CompatibilityEditor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

@Component(property = EventConstants.EVENT_TOPIC + ":String=" + UIEvents.UILifeCycle.ACTIVATE)
public class TextEditorPartCreationListener implements EventHandler {

	private boolean listenerActive = false;

	@Activate
	public void activate() {
		var initialMode = Platform.getPreferencesService().getString(Constants.PREFERENCE_SCOPE, Constants.PREF_KEY_MODE, Constants.MODE_ALWAYS_OFF, null);
		listenerActive = Constants.MODE_INITIAL_ON.equals(initialMode);

		
		var prefs = InstanceScope.INSTANCE.getNode(Constants.PREFERENCE_SCOPE);
		prefs.addPreferenceChangeListener((PreferenceChangeEvent event) -> {
			if (event.getKey().equals(Constants.PREF_KEY_MODE)) {
				listenerActive = Constants.MODE_INITIAL_ON.equals(event.getNewValue());
			}
		});
	}

	@Override
	public void handleEvent(Event event) {
		if (listenerActive) {
			var activatedPart = event.getProperty(UIEvents.EventTags.ELEMENT);
			if (activatedPart instanceof MPart) {
				var editor = getEditor((MPart) activatedPart);
				editor.ifPresent(e -> TabnineCompletionActivationManager.getInstance().activateTabnineCompletion(e));
			}
		}
	}
	
	private Optional<IEditorPart> getEditor(MPart part) {
		if (part != null) {
			Object clientObject = part.getObject();
			if (clientObject instanceof CompatibilityEditor) {
				return Optional.of(((CompatibilityEditor) clientObject).getEditor());
			}
		}
		return Optional.empty();
	}
}

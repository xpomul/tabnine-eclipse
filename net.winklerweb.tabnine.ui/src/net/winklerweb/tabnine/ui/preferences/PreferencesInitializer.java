package net.winklerweb.tabnine.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import net.winklerweb.tabnine.ui.Constants;

public class PreferencesInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		var preferences = new ScopedPreferenceStore(InstanceScope.INSTANCE, Constants.PREFERENCE_SCOPE);
		preferences.setDefault(Constants.PREF_KEY_MODE, Constants.MODE_INITIAL_OFF);
		PreferenceConverter.setDefault(preferences, Constants.PREF_KEY_BACKGROUND_COLOR, new RGB(0, 0, 0));
		PreferenceConverter.setDefault(preferences, Constants.PREF_KEY_TEXT_COLOR, new RGB(128,128,128));
	}
}

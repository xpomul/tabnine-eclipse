package net.winklerweb.tabnine.ui.preferences;

import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import net.winklerweb.tabnine.ui.Constants;

public class TabninePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public TabninePreferencePage() {
		super("Tabnine Code Completion", GRID);
		var store = new ScopedPreferenceStore(InstanceScope.INSTANCE, Constants.PREFERENCE_SCOPE);
		setPreferenceStore(store);
	}

	@Override
	protected void createFieldEditors() {
		RadioGroupFieldEditor radio = new RadioGroupFieldEditor(Constants.PREF_KEY_MODE, "Completion Mode", 3, new String[][] {
				new String []{ "Always Off (never activate)", Constants.MODE_ALWAYS_OFF }, 
				new String []{ "Initially Off (activate on demand)", Constants.MODE_INITIAL_OFF }, 
				new String []{ "Initially On (activate automatically)", Constants.MODE_INITIAL_ON } },
				getFieldEditorParent());
		addField(radio);
		
		var completionBackground = new ColorFieldEditor(Constants.PREF_KEY_BACKGROUND_COLOR, "Completion Background Color:",
				getFieldEditorParent());
		addField(completionBackground);
		var completionText = new ColorFieldEditor(Constants.PREF_KEY_TEXT_COLOR, "Completion Text Color:", getFieldEditorParent());
		addField(completionText);

		var store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "com.tabnine.binary");
		var additionalArgs = new AdditionalArgumentsFieldEditor("additional_arguments",
				"Additional arguments to be passed to the TabNine binary executable", getFieldEditorParent());
		additionalArgs.setPreferenceStore(store);
		addField(additionalArgs);

		var additionalMetadata = new AdditionalMetadataFieldEditor("additional_metadata",
				"Additional client metadata to be passed to the TabNine binary executable", getFieldEditorParent());
		additionalMetadata.setPreferenceStore(store);
		addField(additionalMetadata);
	}

	private static class AdditionalArgumentsFieldEditor extends ListEditor {
		public AdditionalArgumentsFieldEditor(String name, String title, Composite parent) {
			super(name, title, parent);
		}

		@Override
		protected String createList(String[] items) {
			return String.join("$$$", items);
		}

		@Override
		protected String getNewInputObject() {
			var dlg = new InputDialog(getShell(), "New Argument", "Enter the new argument to be added to the list", "",
					s -> s.isBlank() ? "Please enter a valid string" : null);
			dlg.setBlockOnOpen(true);
			if (dlg.open() == IDialogConstants.OK_ID) {
				return dlg.getValue();
			} else {
				return null;
			}
		}

		@Override
		protected String[] parseString(String stringList) {
			var result = Stream.of(stringList.split("\\$\\$\\$")).filter(Predicate.not(String::isBlank))
					.toArray(String[]::new);
			return result;
		}
	}

	private static class AdditionalMetadataFieldEditor extends ListEditor {

		public AdditionalMetadataFieldEditor(String name, String title, Composite parent) {
			super(name, title, parent);
		}

		@Override
		protected String createList(String[] items) {
			return String.join("$$$", items);
		}

		@Override
		protected String getNewInputObject() {
			var dlg = new InputDialog(getShell(), "New Metadata Value",
					"Enter the new metadata value in the format key=value to be added to the list", "",
					s -> s.isBlank() ? "Please enter a valid string" : null);
			dlg.setBlockOnOpen(true);
			if (dlg.open() == IDialogConstants.OK_ID) {
				return dlg.getValue();
			} else {
				return null;
			}
		}

		@Override
		protected String[] parseString(String stringList) {
			var result = Stream.of(stringList.split("\\$\\$\\$")).filter(Predicate.not(String::isBlank))
					.toArray(String[]::new);
			return result;
		}
	}

	@Override
	public void init(IWorkbench workbench) {
	}
}
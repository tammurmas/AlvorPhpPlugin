package org.eclipse.alvor.php;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class AlvorPhpPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {


	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(AlvorPhpPlugin.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		StringFieldEditor fName = new StringFieldEditor("function_name", "Function name", getFieldEditorParent());
		fName.setEmptyStringAllowed(false);
		addField(fName);
	}
}

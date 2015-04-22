package org.eclipse.alvor.php.gui;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
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
		
		String[][] entryNamesAndValues = {{"1","1"},{"2","2"}};
		ComboFieldEditor paramIndex = new ComboFieldEditor("param_index", "Parameter index", entryNamesAndValues, getFieldEditorParent()); 
		addField(paramIndex);
	}
}

package org.eclipse.alvor.php;

import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

public class EditorUtility {
	private EditorUtility() {
		super();
	}

	public static IEditorPart getActiveEditor() {
		IWorkbenchWindow window= AlvorPhpPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page= window.getActivePage();
			if (page != null) {
				return page.getActiveEditor();
			}
		}
		return null;
	}
	
	public static ISourceModule getPhpInput(IEditorPart part) {
		IEditorInput editorInput= part.getEditorInput();
		if (editorInput != null) {
			ISourceModule input= (ISourceModule) DLTKUIPlugin.getEditorInputModelElement(editorInput);
			return input;
		}
		return null;	
	}

}

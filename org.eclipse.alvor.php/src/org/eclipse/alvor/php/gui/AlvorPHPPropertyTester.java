package org.eclipse.alvor.php.gui;

import java.util.List;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.dltk.internal.core.SourceModule;

@SuppressWarnings("restriction")
public class AlvorPHPPropertyTester extends PropertyTester {

	@SuppressWarnings("rawtypes")
	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (property.equals("isPHPProject")) {
			Object element = receiver;
			
			// should work both when receiver is list or smth else
			if (receiver instanceof List) {
				if (((List) receiver).size() == 1) {
					element = ((List)receiver).get(0);
				}
				else {
					return false;
				}
			}
			
			if (!(element instanceof IAdaptable)) {
				return false;
			}
			IProject project = (IProject)((IAdaptable) element).getAdapter(IProject.class);
			
			if (project == null || !project.isOpen()) {
				return false;
			}
			
			try {
				return project.getNature("org.eclipse.php.core.PHPNature") != null;
			} catch (CoreException e) {
				return false;
			}
		}
		else if (property.equals("isPHPFile")) {
			Object element = receiver;
			
			// should work both when receiver is list or smth else
			if (receiver instanceof List) {
				if (((List) receiver).size() == 1) {
					element = ((List)receiver).get(0);
				}
				else {
					return false;
				}
			}
			
			if (!(element instanceof IAdaptable)) {
				return false;
			}
			SourceModule source = (SourceModule)((IAdaptable) element).getAdapter(SourceModule.class);
			
			if(source == null)
			{
				return false;
			}
			
			IFile file = (IFile)source.getResource();
			return file.getFileExtension().equalsIgnoreCase("php");
		}
		else {
			return false;
		}
	}

}

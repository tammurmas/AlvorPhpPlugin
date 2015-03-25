package org.eclipse.alvor.php.handlers;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.alvor.php.AlvorPhpPlugin;
import org.eclipse.alvor.php.crawler.StringCollector;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;

import com.googlecode.alvor.common.HotspotDescriptor;

public class ProjectInvocationHandler extends AbstractHandler {
	
	private static final String phpNature = "org.eclipse.php.core.PHPNature";
	
	private final String funcName = AlvorPhpPlugin.getDefault()
			.getPreferenceStore().getString("function_name");
	
	public Object execute(ExecutionEvent event) {
		//TODO: get the selected project's nature
		IProject project = getSelectedProject();
		
		if(isPHPProject(project))
		{
			System.out.println("..........Start of crawling for "+project.getName()+" .....");
			
			StringCollector collector = new StringCollector(funcName, project);

			try {

				collector.searchProject();
				Collection<HotspotDescriptor> hotspots = collector.getHotspots();

				for (HotspotDescriptor hotspot : hotspots) {
					System.out.println(hotspot);
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println("..........End of crawling for "+project.getName()+" .....");
		}
		else
		{
			MessageDialog.openError(null, "Error!", "The selected project is not a valid PHP-project!");
		}
		return null;
	}
	
	public static IProject getSelectedProject() {
		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		if (selection instanceof StructuredSelection) {
			StructuredSelection structSel = (StructuredSelection) selection;
			if (structSel.size() != 1) {
				return null;
			}
			else {
				Object sel = structSel.iterator().next();
				if (sel instanceof IAdaptable) {
					
					IResource res = (IResource) ((IAdaptable) sel).getAdapter(IResource.class);
					if(res instanceof IProject)
					{
						return (IProject)res;
					}
					else
					{
						return null;
					}
				}
				else {
					return null;
				}
			}
		}
		
		return null;
	}
	
	public static boolean isPHPProject(IProject project)
	{
		if(project != null)
		{
			try {
				String[] natures = project.getDescription().getNatureIds();
				if(natures != null && Arrays.asList(natures).contains(phpNature))
				{
					return true;
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
}
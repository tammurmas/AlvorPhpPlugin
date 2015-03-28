package org.eclipse.alvor.php.handlers;

import java.util.Collection;

import org.eclipse.alvor.php.AlvorPhpPlugin;
import org.eclipse.alvor.php.crawler.StringCollector;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;

import com.googlecode.alvor.common.HotspotDescriptor;

public class ProjectCrawlerHandler extends AbstractHandler {
	
	private final String funcName = AlvorPhpPlugin.getDefault()
			.getPreferenceStore().getString("function_name");
	
	public Object execute(ExecutionEvent event) {
		IProject project = getSelectedProject();
		
		System.out.println("..........Start of crawling for "+ project.getName() + " .....");

		StringCollector collector = new StringCollector(funcName, project);

		try {
			collector.searchProject();
			Collection<HotspotDescriptor> hotspots = collector.getHotspots();

			for (HotspotDescriptor hotspot : hotspots) {
				System.out.println(hotspot);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("..........End of crawling for " + project.getName() + " .....");
		
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
	
}
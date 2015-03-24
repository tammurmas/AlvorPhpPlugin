package org.eclipse.alvor.php.handlers;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.alvor.php.AlvorPhpPlugin;
import org.eclipse.alvor.php.crawler.StringCollector;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.php.internal.core.ast.nodes.ASTParser;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;

import com.googlecode.alvor.common.HotspotDescriptor;

@SuppressWarnings("restriction")
public class ProjectInvocationHandler extends AbstractHandler {
	
	private final String funcName = AlvorPhpPlugin.getDefault()
			.getPreferenceStore().getString("function_name");
	
	public Object execute(ExecutionEvent event) {
		//TODO: get the selected project's nature
		IProject project = getSelectedPHPProject();
		String[] natures = getProjectNatures(project);
		String phpNature = "org.eclipse.php.core.PHPNature";
		
		if(natures != null && Arrays.asList(natures).contains(phpNature))
		{
			System.out.println("..........Start of crawling for "+project.getName()+" .....");
			try {
				IResource[] resources = project.members();
				for(IResource res: resources)
				{
					if(isPHPFile(res))
					{
						IFile file = (IFile)res;
						ISourceModule sourceModule = DLTKCore.createSourceModuleFrom(file);

						if(sourceModule != null)
						{
							StringCollector collector = new StringCollector(funcName, ASTParser
									.newParser(sourceModule).createAST(null));

							collector.performSearch();
							Collection<HotspotDescriptor> hotspots = collector.getHotspots();

							for (HotspotDescriptor hotspot : hotspots) {
								System.out.println(hotspot);
							}
						}
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("..........End of crawling for "+project.getName()+" .....");
		}
		else
		{
			MessageDialog.openInformation(null, "Achtung!", "Not a PHP-project!");
		}
		return null;
	}
	
	public static boolean isPHPFile(IResource resource)
	{
		if(resource instanceof IFile)
		{
			IFile file = (IFile)resource;
			if(file.getFileExtension().equalsIgnoreCase("php"))
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	public static IProject getSelectedPHPProject() {
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
					//return res.getProject();
				}
				else {
					return null;
				}
			}
		}
		
		return null;
	}
	
	public static String[] getProjectNatures(IProject project)
	{
		if(project != null)
		{
			try {
				return project.getDescription().getNatureIds();
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	protected String sampleGetSelectedProject() {
        ISelectionService ss=AlvorPhpPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getSelectionService();
        String projExpID = "org.eclipse.ui.navigator.ProjectExplorer";
        ISelection sel = ss.getSelection(projExpID);
        Object selectedObject=sel;
        if(sel instanceof IStructuredSelection) {
              selectedObject= ((IStructuredSelection)sel).getFirstElement();
        }
        if (selectedObject instanceof IAdaptable) {
              IResource res = (IResource) ((IAdaptable) selectedObject)
                          .getAdapter(IResource.class);
              IProject project = res.getProject();
              return "Project found: "+project.getName();
        }
        
        return null;
  }
}
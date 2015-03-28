package org.eclipse.alvor.php.handlers;

import java.util.Collection;

import org.eclipse.alvor.php.AlvorPhpPlugin;
import org.eclipse.alvor.php.crawler.StringCollector;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.dltk.internal.core.SourceModule;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.googlecode.alvor.common.HotspotDescriptor;

@SuppressWarnings("restriction")
public class FileCrawlerHandler extends AbstractHandler {

	private final String funcName = AlvorPhpPlugin.getDefault()
			.getPreferenceStore().getString("function_name");
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		SourceModule source = getSelectedPHPFile();
		System.out.println("\n..........Start of crawling for "+ source.getFileName() + " .....");
		
		try {
			StringCollector collector = new StringCollector(funcName);

			collector.searchSource(source);
			Collection<HotspotDescriptor> hotspots = collector.getHotspots();

			for (HotspotDescriptor hotspot : hotspots) {
				System.out.println(hotspot);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("..........End of crawling for " + source.getFileName() + " .....\n");

		return null;
	}
	
	public static SourceModule getSelectedPHPFile() {
		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		if (selection instanceof StructuredSelection) {
			StructuredSelection structSel = (StructuredSelection) selection;
			if (structSel.size() != 1) {
				return null;
			}
			else {
				Object sel = structSel.iterator().next();
				if (sel instanceof SourceModule) {
					return (SourceModule)sel;
				}
				else {
					return null;
				}
			}
		}
		
		return null;
	}

}

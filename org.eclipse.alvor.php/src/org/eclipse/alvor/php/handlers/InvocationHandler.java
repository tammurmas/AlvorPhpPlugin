package org.eclipse.alvor.php.handlers;

import java.util.Collection;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.php.internal.core.ast.nodes.ASTParser;
import org.eclipse.php.internal.core.ast.nodes.Program;
import org.eclipse.ui.PlatformUI;
import org.eclipse.alvor.php.EditorUtility;
import org.eclipse.alvor.php.AlvorPhpPlugin;
import org.eclipse.alvor.php.crawler.StringCollector;

import com.googlecode.alvor.common.HotspotDescriptor;
import com.googlecode.alvor.common.PositionUtil;

@SuppressWarnings("restriction")
public class InvocationHandler extends AbstractHandler {

	private final String funcName = AlvorPhpPlugin.getDefault()
			.getPreferenceStore().getString("function_name");

	private final String fileName = PositionUtil
			.getFileString((IResource) PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.getActiveEditor().getEditorInput()
					.getAdapter(IResource.class));
	
	private final ISourceModule sourceModule = EditorUtility
			.getPhpInput(EditorUtility.getActiveEditor());

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		System.out
				.println(".......................Handler invocation...........");

		try {
			StringCollector collector = new StringCollector(funcName, ASTParser
					.newParser(sourceModule).createAST(null), fileName);

			collector.performSearch();
			Collection<HotspotDescriptor> hotspots = collector.getHotspots();

			for (HotspotDescriptor hotspot : hotspots) {
				System.out.println(hotspot);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out
				.println(".......................End of handler invocation.....");

		return null;
	}
	
	
}

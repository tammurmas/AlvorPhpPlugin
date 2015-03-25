package org.eclipse.alvor.php.handlers;

import java.util.Collection;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.alvor.php.EditorUtility;
import org.eclipse.alvor.php.AlvorPhpPlugin;
import org.eclipse.alvor.php.crawler.StringCollector;

import com.googlecode.alvor.common.HotspotDescriptor;

public class InvocationHandler extends AbstractHandler {

	private final String funcName = AlvorPhpPlugin.getDefault()
			.getPreferenceStore().getString("function_name");
	
	private final ISourceModule sourceModule = EditorUtility
			.getPhpInput(EditorUtility.getActiveEditor());

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		System.out.println(".....Handler invocation...........");
		
		try {
			StringCollector collector = new StringCollector(funcName);

			collector.searchSource(sourceModule);
			Collection<HotspotDescriptor> hotspots = collector.getHotspots();

			for (HotspotDescriptor hotspot : hotspots) {
				System.out.println(hotspot);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("......End of handler invocation.....");

		return null;
	}
	
	
}

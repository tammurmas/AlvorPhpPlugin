package org.eclipse.alvor.php.crawler;

import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.php.internal.core.ast.nodes.Expression;
import org.eclipse.php.internal.core.ast.nodes.FunctionInvocation;
import org.eclipse.php.internal.core.ast.nodes.FunctionName;
import org.eclipse.php.internal.core.ast.nodes.NamespaceName;
import org.eclipse.php.internal.core.ast.nodes.Program;
import org.eclipse.php.internal.core.ast.visitor.AbstractVisitor;

import com.googlecode.alvor.common.HotspotDescriptor;
import com.googlecode.alvor.string.Position;

@SuppressWarnings("restriction")
public class StringCollector {
	
	private Collection<HotspotDescriptor> hotspots = new ArrayList<HotspotDescriptor>();

	private String funcName;
	private Program ast;
	private String fileName;
	
	public StringCollector(String funcName, Program ast, String fileName)
	{
		this.funcName = funcName;
		this.ast = ast;
		this.fileName = fileName;
	}
	
	/*
	 * Inspects the AST and collects all occurrences of function invocations
	 * Evaluator then generates hotspotDescriptors based on the type of the argument
	 */
	public void performSearch() {
		try {
			ast.accept(new AbstractVisitor() {
				@Override
				public boolean visit(FunctionInvocation functionInvocation) {
					StringExpressionEvaluator evaluator = new StringExpressionEvaluator();
					
					FunctionName functionName = functionInvocation.getFunctionName();
					Expression expr = functionName.getName();

					if (expr instanceof NamespaceName && ((NamespaceName) expr).getName().equals(funcName)) {
						Position pos = new Position(fileName, functionInvocation.getStart(), functionInvocation.getLength());
						hotspots.add(evaluator.evaluate(functionInvocation,pos));
					}
					return true;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Collection<HotspotDescriptor> getHotspots()
	{
		return hotspots;
	}
	
}

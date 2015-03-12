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

@SuppressWarnings("restriction")
public class StringCollector {
	
	private Collection<HotspotDescriptor> hotspots = new ArrayList<HotspotDescriptor>();

	private String funcName;
	private Program ast;
	
	public StringCollector(String funcName, Program ast)
	{
		this.funcName = funcName;
		this.ast = ast;
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
						Expression arg = (Expression)(functionInvocation.parameters().get(0));//TODO: check if there are no parameters
						hotspots.add(evaluator.evaluate(arg));
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

package org.eclipse.alvor.php.crawler;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.php.internal.core.ast.nodes.ASTParser;
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
	private IProject project;
	
	public StringCollector(String funcName, IProject project)
	{
		this.funcName = funcName;
		this.project = project;
	}
	
	public StringCollector(String funcName)
	{
		this.funcName = funcName;
	}
	
	public void searchProject() throws Exception
	{
		searchProjectResources(project.members());
	}
	
	/*
	 * Search recursively for db-api calls inside project's resources
	 */
	public void searchProjectResources(IResource[] resources) throws Exception {

		for (IResource res : resources) {
			if (isPHPFile(res)) {
				IFile file = (IFile) res;
				ISourceModule sourceModule = DLTKCore.createSourceModuleFrom(file);

				if (sourceModule != null) {
					searchSource(sourceModule);
				}
			} else if (res instanceof IFolder) {
				searchProjectResources(((IFolder) res).members());//recursively search inside folders
			}
		}

	}
	
	/*
	 * Inspects the AST and collects all occurrences of function invocations
	 */
	public void searchSource(ISourceModule sourceModule) throws Exception
	{
		Program ast = ASTParser.newParser(sourceModule).createAST(null);
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
	}
	
	public Collection<HotspotDescriptor> getHotspots()
	{
		return hotspots;
	}
	
	public boolean isPHPFile(IResource resource)
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
	
}

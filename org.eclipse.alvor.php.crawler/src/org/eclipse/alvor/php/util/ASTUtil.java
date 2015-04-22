package org.eclipse.alvor.php.util;

import org.eclipse.core.resources.IResource;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.php.internal.core.ast.nodes.ASTNode;
import org.eclipse.php.internal.core.ast.nodes.DoStatement;
import org.eclipse.php.internal.core.ast.nodes.Expression;
import org.eclipse.php.internal.core.ast.nodes.ForStatement;
import org.eclipse.php.internal.core.ast.nodes.IBinding;
import org.eclipse.php.internal.core.ast.nodes.Identifier;
import org.eclipse.php.internal.core.ast.nodes.NamespaceName;
import org.eclipse.php.internal.core.ast.nodes.Statement;
import org.eclipse.php.internal.core.ast.nodes.Variable;
import org.eclipse.php.internal.core.ast.nodes.WhileStatement;

import com.googlecode.alvor.common.PositionUtil;
import com.googlecode.alvor.string.IPosition;
import com.googlecode.alvor.string.Position;


@SuppressWarnings("restriction")
/**
 * Helper class to collect info about resources and Abstract Syntax Tree
 * @author Urmas
 *
 */
public class ASTUtil {
	
	public static IPosition getPosition(ASTNode node)
	{
		return new Position(ASTUtil.getFileString(node), node.getStart(), node.getLength());
	}

	private static String getFileString(ASTNode node) {
		IResource file = getFile(node);
		return PositionUtil.getFileString(file);
	}

	private static IResource getFile(ASTNode node) {
		try {
			ISourceModule sourceModule = node.getProgramRoot().getSourceModule();
			IResource resource = sourceModule.getCorrespondingResource();
			return resource;
		} catch (ModelException e) {
			throw new IllegalStateException(e);
		}
	}
	
	public static String getVariableName(Variable var)
	{
		Identifier id = (Identifier) var.getName();
		return id.getName();
	}
	
	/**
	 * Check the bindings of an Expression object and a Variable binding 
	 */
	public static boolean sameBinding(Expression exp, IBinding var) {
		if(exp instanceof Variable)
		{
			Variable v = (Variable)exp; 
			//a dirty hack! we just compare the names of the variables if all else fails
			return v.resolveVariableBinding().equals(var) || v.resolveVariableBinding().getName().equals(var.getName());
		}
		else
		{
			return false;
		}
	}
	
	public static boolean isLoopStatement(ASTNode node) {
		return node instanceof WhileStatement
			|| node instanceof ForStatement
			|| node instanceof DoStatement; 
	}
	
	public static Statement getLoopBody(ASTNode loop) {
		if (loop instanceof ForStatement) {
			return ((ForStatement)loop).getBody();
		}
		else if (loop instanceof WhileStatement) {
			return ((WhileStatement)loop).getBody();
		}
		else if (loop instanceof DoStatement) {
			return ((DoStatement)loop).getBody();
		}
		else {
			throw new IllegalArgumentException();
		}
	}
	
	public static String getFunctionOrMethodName(Expression expr)
	{
		if (expr instanceof NamespaceName) {
			return ((NamespaceName) expr).getName();
		}
		
		if(expr instanceof Variable && ((Variable)expr).getName() instanceof Identifier)
		{
			Identifier id = (Identifier)((Variable)expr).getName();
			return id.getName();
				 
		}
		
		return null;
	}
	
}

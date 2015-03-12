package org.eclipse.alvor.php.util;

import org.eclipse.core.resources.IResource;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.php.internal.core.ast.nodes.ASTNode;
import org.eclipse.php.internal.core.ast.nodes.Expression;
import org.eclipse.php.internal.core.ast.nodes.IBinding;
import org.eclipse.php.internal.core.ast.nodes.Identifier;
import org.eclipse.php.internal.core.ast.nodes.Variable;

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
	 * @param exp
	 * @param var
	 * @return
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
}

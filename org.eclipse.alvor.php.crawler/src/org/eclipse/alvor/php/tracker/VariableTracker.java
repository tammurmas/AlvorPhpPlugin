package org.eclipse.alvor.php.tracker;


import org.eclipse.alvor.php.util.ASTUtil;
import org.eclipse.alvor.php.util.UnsupportedStringOpExAtNode;
import org.eclipse.php.internal.core.ast.nodes.ASTNode;
import org.eclipse.php.internal.core.ast.nodes.Assignment;
import org.eclipse.php.internal.core.ast.nodes.Block;
import org.eclipse.php.internal.core.ast.nodes.Expression;
import org.eclipse.php.internal.core.ast.nodes.ExpressionStatement;
import org.eclipse.php.internal.core.ast.nodes.FunctionInvocation;
import org.eclipse.php.internal.core.ast.nodes.IVariableBinding;
import org.eclipse.php.internal.core.ast.nodes.Scalar;
import org.eclipse.php.internal.core.ast.nodes.Statement;
import org.eclipse.php.internal.core.ast.nodes.Variable;


@SuppressWarnings("restriction")
/**
 * Follows the alternation of the db-query parameter upward starting from the db-query call
 * @author Urmas
 *
 */
public class VariableTracker {

	public static NameUsage getLastReachingMod(IVariableBinding var, ASTNode target) {
		assert target != null;
		
		ASTNode parent = target.getParent();
		NameUsage usage = getLastReachingModIn(var, target, parent);
		
		if (usage != null) {
			return usage;
		}
		else {
			// nothing found inside parent, so search in things preceding the parent
			NameUsage precedingUsage = getLastReachingMod(var, parent);
			return precedingUsage;
		}
	}

	/**
	 * Find previous modification of target node with the given binding inside the scope
	 * @param var
	 * @param target
	 * @param scope
	 * @return
	 */
	private static NameUsage getLastReachingModIn(IVariableBinding var, ASTNode target, ASTNode scope) {
		if (scope instanceof FunctionInvocation) {
			return getLastReachingModInInv(var, target, (FunctionInvocation)scope);
		}
		else if (scope instanceof ExpressionStatement) {
			if (target == null) {
				return getLastModIn(var, ((ExpressionStatement)scope).getExpression());
			} 
			else {
				assert (target == ((ExpressionStatement)scope).getExpression());
				return null;
			}
		}
		else if (scope instanceof Block) {
			return getLastReachingModInBlock(var, target, (Block)scope);
		}
		else if (scope instanceof Assignment) {
	    	return getLastReachingModInVDeclStmt(var, target, (Assignment)scope);
	    }
		// a special case of an unassigned variable
		else if (scope instanceof Variable) {
			throw new UnsupportedStringOpExAtNode("getLastReachingModIn " + scope.getClass(), scope);
	    }
		else
		{
			throw new UnsupportedStringOpExAtNode("getLastReachingModIn " + scope.getClass(), scope);
		}
	}
	
	/**
	 * Helper method to search for variable modification in the next scope
	 * @param var
	 * @param scope
	 * @return
	 */
	public static NameUsage getLastModIn(IVariableBinding var, ASTNode scope) {
		if (var != null && var.isField()) {
			//return getFieldDefinition(var);
			//TODO: handle the field parameters
		}
		return getLastReachingModIn(var, null, scope);
	}
	
	/**
	 * Get last modification of target node(with the given binding) inside the method invocation
	 * @param var
	 * @param target
	 * @param inv
	 * @return
	 */
	private static NameUsage getLastReachingModInInv(IVariableBinding var,
			ASTNode target, FunctionInvocation inv) {
		//TODO check in expression position
		
		return null;
	}
	
	private static NameUsage getLastReachingModInBlock(IVariableBinding var,
			ASTNode target, Block block) {
		int stmtIdx; // last statement that can affect target
		
		if (target == null) { // search whole block
			stmtIdx = block.statements().size()-1;
		}
		else { 
			// should be called only when block really is target's direct parent
			stmtIdx = block.statements().indexOf(target)-1;
		}
		
		// go backwards in statements
		for (int i = stmtIdx; i >= 0; i--) {
			Statement stmt = (Statement)block.statements().get(i);
			
			NameUsage usage = getLastModIn(var, stmt);
			if (usage != null) {
				return usage;
			}
		}
		
		// no (preceding) statement modifies var, e.g. this block doesn't affect target
		return null;
	}
	
	/**
	 * Check whether the left-side of the assignment(Variable) has the same binding as the right-side of the assignment(Expression)
	 * @param var
	 * @param target
	 * @param vDeclStmt
	 * @return
	 */
	private static NameUsage getLastReachingModInVDeclStmt(IVariableBinding var, 
			ASTNode target, Assignment vDeclStmt) {
		
		if(ASTUtil.sameBinding((Expression)vDeclStmt.getLeftHandSide(), var))
		{
			return new NameAssignment(vDeclStmt);
		}
		else
		{
			return  getLastModIn(var, (Expression)vDeclStmt.getRightHandSide());
		}
			
	}

}

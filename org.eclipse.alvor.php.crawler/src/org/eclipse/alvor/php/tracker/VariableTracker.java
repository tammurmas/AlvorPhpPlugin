package org.eclipse.alvor.php.tracker;


import org.eclipse.alvor.php.util.ASTUtil;
import org.eclipse.alvor.php.util.UnsupportedStringOpExAtNode;
import org.eclipse.php.internal.core.ast.nodes.ASTNode;
import org.eclipse.php.internal.core.ast.nodes.Assignment;
import org.eclipse.php.internal.core.ast.nodes.Block;
import org.eclipse.php.internal.core.ast.nodes.ExpressionStatement;
import org.eclipse.php.internal.core.ast.nodes.FunctionInvocation;
import org.eclipse.php.internal.core.ast.nodes.IVariableBinding;
import org.eclipse.php.internal.core.ast.nodes.InfixExpression;
import org.eclipse.php.internal.core.ast.nodes.Statement;
import org.eclipse.php.internal.core.ast.nodes.Variable;

import com.googlecode.alvor.common.UnsupportedStringOpEx;
import com.googlecode.alvor.string.IPosition;


@SuppressWarnings("restriction")
/**
 * Follows the alternation of the db-query parameter upward starting from the db-query call
 * @author Urmas
 *
 */
public class VariableTracker {

	public static NameUsage getLastReachingMod(IVariableBinding var, ASTNode target) {
		assert target != null;
		/*if(var == null)
			throw new UnsupportedStringOpExAtNode("getLastReachingModIn " + target.getClass(), target);*/
			
		if (var != null && var.isField()) {
			return getFieldDefinition(var);
		}
		
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
			//return getLastReachingModInVDeclStmt(var, target, (Assignment)scope);
			return getLastReachingModInAss(var, target, (Assignment)scope);
	    	
	    }
		else if(scope instanceof InfixExpression)
		{
			return getLastReachingModInInfix(var, target, (InfixExpression)scope);
		}
		else if(scope instanceof Variable)
		{
			//we've reached an empty variable declaration, carry on
			return null;
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
			return getFieldDefinition(var);
			//TODO: handle the field parameters
		}
		return getLastReachingModIn(var, null, scope);
	}
	
	/**
	 * Check for a field definition
	 * @param var
	 * @return
	 */
	private static NameUsage getFieldDefinition(IVariableBinding var) {
		// for now, (final)fields should be handled by client 
		throw new UnsupportedStringOpEx("Fields are not supported in tracker", (IPosition)null);
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
	
	/**
	 * Get last modaification of target inside a block statement
	 * @param var
	 * @param target
	 * @param block
	 * @return
	 */
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
	 * Get last modification inside an assignment
	 * @param var
	 * @param target
	 * @param ass
	 * @return
	 */
	private static NameUsage getLastReachingModInAss(IVariableBinding var, 
			ASTNode target, Assignment ass) {
		
		if (target == null && ASTUtil.sameBinding(ass.getLeftHandSide(), var)) {
			return new NameAssignment(ass);
		}
		
		return null; 
	}
	
	/**
	 * Track variable modifications inside an infix expression, i.e concatenation of strings
	 * @param var
	 * @param target
	 * @param inf
	 * @return
	 */
	private static NameUsage getLastReachingModInInfix(IVariableBinding var,
			ASTNode target, InfixExpression inf) {
		
		//the right side is not an infix expression in PDT, so we have to search for its value from the level above
		if(target != null && target.equals(inf.getRight()))
		{
			NameUsage usage = getLastModIn(var, inf);
			if(usage != null)
			{
				return usage;
			}
		}
		
		//if the left-side is also an infix expression, then dig deeper into it 
		if(target != null && target.equals(inf.getLeft()) && inf.getLeft() instanceof InfixExpression)
		{
			NameUsage usage = getLastModIn(var, target);
			if(usage != null)
			{
				return usage;
			}
		}
		
		//if the left-hand side is not an infix expression anymore, then we can finally evaluate its value
		if(target != null && target.equals(inf.getLeft()) && !(inf.getLeft() instanceof InfixExpression))
		{
			NameUsage usage = getLastModIn(var, inf);
			if(usage != null)
			{
				return usage;
			}
		}
		return null;
		
	}
	

}

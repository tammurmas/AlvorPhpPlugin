package org.eclipse.alvor.php.crawler;

import org.eclipse.alvor.php.tracker.NameAssignment;
import org.eclipse.alvor.php.tracker.NameUsage;
import org.eclipse.alvor.php.tracker.VariableTracker;
import org.eclipse.alvor.php.util.ASTUtil;
import org.eclipse.alvor.php.util.UnsupportedStringOpExAtNode;
import org.eclipse.php.internal.core.ast.nodes.ASTNode;
import org.eclipse.php.internal.core.ast.nodes.Assignment;
import org.eclipse.php.internal.core.ast.nodes.Expression;
import org.eclipse.php.internal.core.ast.nodes.ITypeBinding;
import org.eclipse.php.internal.core.ast.nodes.IVariableBinding;
import org.eclipse.php.internal.core.ast.nodes.Scalar;
import org.eclipse.php.internal.core.ast.nodes.Variable;

import com.googlecode.alvor.common.HotspotDescriptor;
import com.googlecode.alvor.common.StringHotspotDescriptor;
import com.googlecode.alvor.common.UnsupportedHotspotDescriptor;
import com.googlecode.alvor.common.UnsupportedStringOpEx;
import com.googlecode.alvor.string.IAbstractString;
import com.googlecode.alvor.string.StringConstant;
import com.googlecode.alvor.string.StringSequence;

@SuppressWarnings("restriction")
public class StringExpressionEvaluator {
	
	/**
	 * Creates and returns a new HotspotDescriptor at the given position
	 * @param node
	 * @param pos
	 * @return
	 */
	public HotspotDescriptor evaluate(Expression node)
	{
		try
		{
			IAbstractString str = eval(node);
			return new StringHotspotDescriptor(ASTUtil.getPosition(node), str);
		}
		catch(UnsupportedStringOpEx e)
		{
			return new UnsupportedHotspotDescriptor(ASTUtil.getPosition(node), e.getMessage(), e.getPosition());
		}
	}
	
	/**
	 * Evaluates recursively the ASTNode and extracts the needed IAbstractStrings
	 * @param node
	 * @param pos
	 * @return
	 * @throws UnsupportedStringOpEx - gets thrown if the node is not supported
	 */
	private IAbstractString eval(Expression node) throws UnsupportedStringOpEx
	{
		if (node instanceof Scalar)
		{
			return new StringConstant(ASTUtil.getPosition(node), ((Scalar)node).getStringValue(), null);
		}
		else if (node instanceof Variable)
		{
			return evalVariable((Variable)node);
		}
		else
		{
			throw new UnsupportedStringOpExAtNode("getValOf(" + node.getClass().getName() + ")", node);
		}
	}

	private IAbstractString evalVariable(Variable node) {
		//TODO: Check whether the type binding of the node is a String object
		return evalVarBefore((IVariableBinding)node.resolveVariableBinding(), node);
	}

	/**
	 * Get the usage context of the variable
	 * @param var
	 * @param target
	 * @return
	 */
	private IAbstractString evalVarBefore(IVariableBinding var, ASTNode target) {
		NameUsage usage = VariableTracker.getLastReachingMod(var, target);
		if (usage == null) {
			throw new UnsupportedStringOpEx("internal error: Can't find definition for '" + var + "'", null);
		}
		
		return evalVarAfter(var, usage);
	}

	/**
	 * Evaluate the variable usage context and return the corresponding String object  
	 * @param var
	 * @param usage
	 * @return
	 */
	private IAbstractString evalVarAfter(IVariableBinding var, NameUsage usage) {
		if(usage instanceof NameAssignment)
		{
			return evalVarAfterAssignment(var, (NameAssignment)usage);
		}
		else {
			throw new IllegalArgumentException();
		}
	}
	
	/**
	 * Determine whether we assign or concatenate and assign
	 * @param var
	 * @param usage
	 * @return
	 */
	private IAbstractString evalVarAfterAssignment(IVariableBinding var, NameAssignment usage)
	{
		if (usage.getOperator() == Assignment.OP_EQUAL) {
			return eval(usage.getRightHandSide());
		}
		else if (usage.getOperator() == Assignment.OP_CONCAT_EQUAL) {
			return new StringSequence(ASTUtil.getPosition(usage
					.getAssignmentOrDeclaration()),
					eval(usage.getLeftHandSide()),
					eval(usage.getRightHandSide()));
		}
		else
		{
			throw new UnsupportedStringOpExAtNode("Unknown assignment operator: " + usage.getOperator(), usage.getAssignmentOrDeclaration());
		}
	}
	

	
}

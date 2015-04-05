package org.eclipse.alvor.php.crawler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.alvor.php.tracker.NameAssignment;
import org.eclipse.alvor.php.tracker.NameInParameter;
import org.eclipse.alvor.php.tracker.NameUsage;
import org.eclipse.alvor.php.tracker.NameUsageChoice;
import org.eclipse.alvor.php.tracker.VariableTracker;
import org.eclipse.alvor.php.util.ASTUtil;
import org.eclipse.alvor.php.util.UnsupportedStringOpExAtNode;
import org.eclipse.php.internal.core.ast.nodes.ASTNode;
import org.eclipse.php.internal.core.ast.nodes.ArrayAccess;
import org.eclipse.php.internal.core.ast.nodes.Assignment;
import org.eclipse.php.internal.core.ast.nodes.ConditionalExpression;
import org.eclipse.php.internal.core.ast.nodes.Expression;
import org.eclipse.php.internal.core.ast.nodes.IVariableBinding;
import org.eclipse.php.internal.core.ast.nodes.InfixExpression;
import org.eclipse.php.internal.core.ast.nodes.ParenthesisExpression;
import org.eclipse.php.internal.core.ast.nodes.Scalar;
import org.eclipse.php.internal.core.ast.nodes.Variable;

import com.googlecode.alvor.common.HotspotDescriptor;
import com.googlecode.alvor.common.StringHotspotDescriptor;
import com.googlecode.alvor.common.UnsupportedHotspotDescriptor;
import com.googlecode.alvor.common.UnsupportedStringOpEx;
import com.googlecode.alvor.string.IAbstractString;
import com.googlecode.alvor.string.IPosition;
import com.googlecode.alvor.string.StringChoice;
import com.googlecode.alvor.string.StringConstant;
import com.googlecode.alvor.string.StringParameter;
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
		else if (node instanceof ArrayAccess)
		{
			//NOTE: some-why an array access gets evaluated to variable, so we have to check it beforehand
			throw new UnsupportedStringOpExAtNode("getValOf(" + node.getClass().getName() + ")", node);
		}
		else if (node instanceof Variable)
		{
			return evalVariable((Variable)node);
		}
		else if (node instanceof InfixExpression)
		{
			return evalInfix((InfixExpression)node);
		}
		else if (node instanceof ConditionalExpression) {
			return new StringChoice(ASTUtil.getPosition(node),
					eval(((ConditionalExpression)node).getIfTrue()),
					eval(((ConditionalExpression)node).getIfFalse()));
		}
		else if (node instanceof ParenthesisExpression) {
			return eval(((ParenthesisExpression)node).getExpression());
		}
		else
		{
			throw new UnsupportedStringOpExAtNode("getValOf(" + node.getClass().getName() + ")", node);
		}
		//TODO: ClassInstanceCreation
		//TODO: MethodInvocation
		//TODO: CastExpression
	}

	private IAbstractString evalVariable(Variable node) {
		//if binding is null it means the node is not available in the AST
		IVariableBinding var = node.resolveVariableBinding();
		if(var == null)
		{
			throw new UnsupportedStringOpEx("internal error: Can't find definition for '" + ASTUtil.getVariableName(node) + "'", null);
		}
		
		return evalVarBefore(var, node);
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
		else if (usage instanceof NameUsageChoice) {
			return evalVarAfterUsageChoice(var, (NameUsageChoice)usage);
		}
		else if (usage instanceof NameInParameter)
		{
			return evalVarInParameter(var, (NameInParameter)usage);
		}
		else {
			throw new IllegalArgumentException();
		}
	}
	
	private IAbstractString evalVarInParameter(IVariableBinding var,
			NameInParameter usage) {
		IPosition pos = ASTUtil.getPosition(usage.getParameterNode());
		
		return new StringParameter(pos, usage.getParameterNo());
	}

	/**
	 * Determine whether we assign or concatenate and then assign
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
	
	/**
	 * Evaluate the variable after an if-else clause
	 * @param var
	 * @param uc
	 * @return
	 */
	private IAbstractString evalVarAfterUsageChoice(IVariableBinding var, NameUsageChoice uc)
	{
		IAbstractString thenStr;
		if (uc.getThenUsage() == null) {
			thenStr = this.evalVarBefore(var, uc.getCommonParentNode()); // eval before if statement
		}
		else {
			 thenStr = this.evalVarAfter(var, uc.getThenUsage());
		}
		
		IAbstractString elseStr;
		if (uc.getElseUsage() == null) {
			elseStr = this.evalVarBefore(var, uc.getCommonParentNode()); // eval before if statement
		}
		else {
			elseStr = this.evalVarAfter(var, uc.getElseUsage());
		}
		
		IPosition pos = null;
		if (uc.getCommonParentNode() != null) {
			pos = ASTUtil.getPosition(uc.getCommonParentNode());
		}
		return new StringChoice(pos, thenStr, elseStr);
		
		//TODO: optimizeChoice?
	}
	
	private IAbstractString evalInfix(InfixExpression expr)
	{
		if(expr.getOperator() == InfixExpression.OP_CONCAT)
		{
			List<IAbstractString> ops = new ArrayList<IAbstractString>();
			ops.add(eval(expr.getLeft()));
			ops.add(eval(expr.getRight()));
			return new StringSequence(ASTUtil.getPosition(expr), ops);
		}
		else
		{
			throw new UnsupportedStringOpExAtNode("getValOf( infix op = " + expr.getOperator() + ")", expr);
		}
	}
	

	
}

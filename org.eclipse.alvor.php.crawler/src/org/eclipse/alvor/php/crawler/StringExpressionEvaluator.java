package org.eclipse.alvor.php.crawler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.php.internal.core.ast.nodes.ASTNode;
import org.eclipse.php.internal.core.ast.nodes.Assignment;
import org.eclipse.php.internal.core.ast.nodes.Expression;
import org.eclipse.php.internal.core.ast.nodes.FunctionInvocation;
import org.eclipse.php.internal.core.ast.nodes.InfixExpression;
import org.eclipse.php.internal.core.ast.nodes.Program;
import org.eclipse.php.internal.core.ast.nodes.Scalar;
import org.eclipse.php.internal.core.ast.nodes.Variable;

import com.googlecode.alvor.common.HotspotDescriptor;
import com.googlecode.alvor.common.StringHotspotDescriptor;
import com.googlecode.alvor.common.UnsupportedHotspotDescriptor;
import com.googlecode.alvor.common.UnsupportedStringOpEx;
import com.googlecode.alvor.string.IAbstractString;
import com.googlecode.alvor.string.Position;
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
	public HotspotDescriptor evaluate(ASTNode node, Position pos)
	{
		try
		{
			IAbstractString str = eval(node, pos);
			return new StringHotspotDescriptor(pos, str);
		}
		catch(UnsupportedStringOpEx e)
		{
			return new UnsupportedHotspotDescriptor(pos, e.getMessage(), e.getPosition());
		}
	}
	
	/**
	 * Evaluates the provided ASTNode to extract IAbstractStrings
	 * @param node
	 * @param pos
	 * @return
	 * @throws UnsupportedStringOpEx - gets thrown if the node is not supported
	 */
	private IAbstractString eval(ASTNode node, Position pos) throws UnsupportedStringOpEx
	{
		if(node instanceof FunctionInvocation)
		{
			return evalInvocationResult((FunctionInvocation)node, pos);
		}
		else if(node instanceof Variable)
		{
			return evalVariable((Variable)node, pos);
		}
		else if(node instanceof InfixExpression)
		{
			return evalInfixExpression((InfixExpression)node, pos);
		}
		else if (node instanceof Scalar && ((Scalar)node).getScalarType() == Scalar.TYPE_STRING)
		{
			return new StringConstant(new Position(pos.getPath(), node.getStart(), node.getLength()), ((Scalar)node).getStringValue(), null);
		}
		else
		{
			throw new UnsupportedStringOpEx("Requested operation is not supported!", pos);
		}
	}
	
	/**
	 * Evaluate the result of a db-query call
	 * @param inv
	 * @param pos
	 * @return
	 * @throws UnsupportedStringOpEx
	 */
	private IAbstractString evalInvocationResult(FunctionInvocation inv, Position pos) throws UnsupportedStringOpEx
	{
		List<Expression> params = inv.parameters();
		Expression param = params.get(0);
		
		if (param instanceof Scalar && ((Scalar) param).getScalarType() == Scalar.TYPE_STRING) {
			if(param.getEnclosingBodyNode() instanceof Program)
			{
				//in case of a scalar string value, return StringConstant
				return new StringConstant(pos, ((Scalar)param).getStringValue(), null);
			}
			else
			{
				throw new UnsupportedStringOpEx("Collecting only the root level assignments!", pos);
			}
			
		}
		else if(param instanceof Variable)
		{
			//goes back to eval function
			return eval(param, pos);
		}
		else if(param instanceof FunctionInvocation)
		{
			throw new UnsupportedStringOpEx("Function invocations as parameters not supported yet!", pos);
		}
		else
		{
			throw new UnsupportedStringOpEx("Parameter has to be a query object!", pos);
		}
	}
	
	/**
	 * Just for testing to collect all variable occurrences and create a StringSequence object out of them
	 * @param var
	 * @param pos
	 * @return
	 */
	private IAbstractString evalVariable(Variable var, Position pos)
	{
		ASTNode root = var.getRoot();
		
		VariableOccurencesFinder varOccFinder = new VariableOccurencesFinder(var);
		root.accept(varOccFinder);
		
		List<Variable> varOccurences = varOccFinder.getOccurences();
		List<IAbstractString> options = new ArrayList<IAbstractString>();
		
		for(Variable v: varOccurences)
		{
			Assignment assign = (Assignment)v.getParent();
			int operator = assign.getOperator();
			
			//not sure if we need to check the operators here or not???
			if((operator != Assignment.OP_EQUAL) && (operator != Assignment.OP_CONCAT_EQUAL))
			{
				throw new UnsupportedStringOpEx("Unknown assignment operator: \"" + assign.getOperationString()+"\"", pos);
			}
				
			Expression assignedValue = assign.getRightHandSide();
			Position varPos = new Position(pos.getPath(), v.getStart(), v.getLength());
			
			if(assignedValue instanceof Scalar)
			{
				//not sure if it should be the position of the occurence or the final hotspot???
				options.add(new StringConstant(varPos, ((Scalar)assignedValue).getStringValue(), null));
			}
			else if(assignedValue instanceof InfixExpression)
			{
				options.add(eval(assignedValue, varPos));
			}
			else
				throw new UnsupportedStringOpEx("Only the assignment of strings and the results of infix operations allowed!", pos);
		}
		
		return new StringSequence(pos, options);
	}
	
	/**
	 * Evaluates the given infix expression
	 * @param expr
	 * @return StringSequence from the parts of the InfixExpression
	 */
	private IAbstractString evalInfixExpression(InfixExpression expr, Position pos)
	{
		//right now we only allow the concatenation of strings, everything else gets discarded
		int operator = expr.getOperator();
		if(operator == InfixExpression.OP_CONCAT)
		{
			List<IAbstractString> ops = new ArrayList<IAbstractString>();
			
			Expression left = expr.getLeft();
			Expression right = expr.getRight();
			
			if(left instanceof InfixExpression || left instanceof Scalar)
			{
				ops.add(eval(left, pos));
			}
			else
			{
				throw new UnsupportedStringOpEx(
						"Unsupported expression inside infix expression!",
						new Position(pos.getPath(), left.getStart(),
								left.getEnd()));
			}
			
			if(right instanceof InfixExpression || right instanceof Scalar)
			{
				ops.add(eval(right, pos));
			}
			else
			{
				throw new UnsupportedStringOpEx(
						"Unsupported expression inside infix expression!",
						new Position(pos.getPath(), right.getStart(),
								right.getEnd()));
			}
			
			return new StringSequence(pos, ops);
		}
		else
		{
			throw new UnsupportedStringOpEx("Unsupported infix operator \""+InfixExpression.getOperator(operator)+"\"", pos);
		}
	}
	
}

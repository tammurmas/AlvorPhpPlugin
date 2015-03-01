package org.eclipse.alvor.php.crawler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.php.internal.core.ast.nodes.ASTNode;
import org.eclipse.php.internal.core.ast.nodes.Assignment;
import org.eclipse.php.internal.core.ast.nodes.Expression;
import org.eclipse.php.internal.core.ast.nodes.FunctionInvocation;
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
				throw new UnsupportedStringOpEx("Unknown assignment operator: " + assign.getOperationString(), pos);
			}
				
			Expression assignedValue = assign.getRightHandSide();
			if(assignedValue instanceof Scalar)
			{
				//not sure if it should be the position of the occurence or the final hotspot???
				Position varPos = new Position(pos.getPath(), v.getStart(), v.getLength());
				options.add(new StringConstant(varPos, ((Scalar)assignedValue).getStringValue(), null));
			}
			else
				throw new UnsupportedStringOpEx("Only the assignment of strings to variable values allowed at the moment!", pos);
		}
		
		return new StringSequence(pos, options);
	}
	
}

package org.eclipse.alvor.php.crawler;

import java.util.Collection;
import java.util.List;

import org.eclipse.php.internal.core.ast.nodes.ASTNode;
import org.eclipse.php.internal.core.ast.nodes.Expression;
import org.eclipse.php.internal.core.ast.nodes.FunctionInvocation;
import org.eclipse.php.internal.core.ast.nodes.Identifier;
import org.eclipse.php.internal.core.ast.nodes.Scalar;
import org.eclipse.php.internal.core.ast.nodes.Variable;

import com.googlecode.alvor.common.HotspotDescriptor;
import com.googlecode.alvor.common.StringHotspotDescriptor;
import com.googlecode.alvor.common.UnsupportedHotspotDescriptor;
import com.googlecode.alvor.common.UnsupportedStringOpEx;
import com.googlecode.alvor.string.IAbstractString;
import com.googlecode.alvor.string.Position;
import com.googlecode.alvor.string.StringConstant;

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
	
	private IAbstractString evalInvocationResult(FunctionInvocation inv, Position pos) throws UnsupportedStringOpEx
	{
		List<Expression> params = inv.parameters();
		Expression param = params.get(0);
		
		if (param instanceof Scalar && ((Scalar) param).getScalarType() == Scalar.TYPE_STRING) {
			//in case of a scalar string value, return StringConstant
			return new StringConstant(pos, ((Scalar)param).getStringValue(), null);
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
	 * Just for testing to collect all variable occurrences
	 * @param var
	 * @param pos
	 * @return
	 */
	private IAbstractString evalVariable(Variable var, Position pos)
	{
		ASTNode root = var.getRoot();
		
		VariableOccurencesFinder varOccFinder = new VariableOccurencesFinder(var);
		root.accept(varOccFinder);
		
		Collection<Variable> varOccurences = varOccFinder.getOccurences();
		for(Variable v: varOccurences)
		{
			System.out.println("Variable occurence "
					+ ((Identifier) (v).getName()).getName() + " "
					+ pos.getPath() + "(" + v.getStart() + ")");
		}
		
		throw new UnsupportedStringOpEx("Variables not supported yet!", pos);
	}
}

package org.eclipse.alvor.php.crawler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.php.internal.core.ast.nodes.ASTNode;
import org.eclipse.php.internal.core.ast.nodes.Assignment;
import org.eclipse.php.internal.core.ast.nodes.Expression;
import org.eclipse.php.internal.core.ast.nodes.Identifier;
import org.eclipse.php.internal.core.ast.nodes.Program;
import org.eclipse.php.internal.core.ast.nodes.Variable;
import org.eclipse.php.internal.core.ast.visitor.AbstractVisitor;

@SuppressWarnings("restriction")
public class VariableOccurencesFinder extends AbstractVisitor{
	
	private Variable var;
	
	public VariableOccurencesFinder(Variable var)
	{
		this.var = var;
	}
	
	private List<Variable> occurences = new ArrayList<Variable>(); 
	
	public boolean visit(Variable variable)
	{
		Expression variableName = variable.getName();
		Expression varName = var.getName();
		
		if (variableName.getType() == ASTNode.IDENTIFIER && variable.isDollared())
		{
			//right now we only collect variables from simple assignments on the program's root level
			if (((Identifier) variableName).getName().equals(((Identifier)varName).getName())
					&& variable.getStart() < var.getStart()
					&& variable.getParent() instanceof Assignment
					&& variable.getEnclosingBodyNode() instanceof Program)
			{
				occurences.add(variable);
			}
		}
		
		return true;
	}
	
	public List<Variable> getOccurences()
	{
		return occurences;
	}
}

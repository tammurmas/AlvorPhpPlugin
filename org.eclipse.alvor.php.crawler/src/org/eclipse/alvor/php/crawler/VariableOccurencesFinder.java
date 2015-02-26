package org.eclipse.alvor.php.crawler;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.php.internal.core.ast.nodes.Identifier;
import org.eclipse.php.internal.core.ast.nodes.Variable;
import org.eclipse.php.internal.core.ast.visitor.AbstractVisitor;

@SuppressWarnings("restriction")
public class VariableOccurencesFinder extends AbstractVisitor{
	
	private Variable var;
	
	public VariableOccurencesFinder(Variable var)
	{
		this.var = var;
	}
	
	private Collection<Variable> occurences = new ArrayList<Variable>(); 
	
	public boolean visit(Variable variable)
	{
		String variableName = ((Identifier)(variable).getName()).getName();
		String varName = ((Identifier)(var).getName()).getName();
		
		if(variableName.equals(varName) && variable.getStart() < var.getStart())
		{
			occurences.add(variable);
		}
			
		return true;
	}
	
	public Collection<Variable> getOccurences()
	{
		return occurences;
	}
}

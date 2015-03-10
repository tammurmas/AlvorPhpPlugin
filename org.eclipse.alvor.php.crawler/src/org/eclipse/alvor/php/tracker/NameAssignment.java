package org.eclipse.alvor.php.tracker;

import org.eclipse.php.internal.core.ast.nodes.ASTNode;
import org.eclipse.php.internal.core.ast.nodes.Assignment;
import org.eclipse.php.internal.core.ast.nodes.Expression;

@SuppressWarnings("restriction")
public class NameAssignment extends NameUsage {
	private int operator;
	private Expression leftHandSide;
	private Expression rightHandSide;
	private ASTNode assOrDecl;//not sure if this is needed at all
	
	public NameAssignment(Assignment assignment) {
		this.assOrDecl = assignment;
		this.operator = assignment.getOperator();
		this.leftHandSide = assignment.getLeftHandSide();
		this.rightHandSide = assignment.getRightHandSide();
	}
	
	//TODO: A special case of an unassigned variable
	
	public ASTNode getAssignmentOrDeclaration() {
		return assOrDecl;
	}
	
	@Override
	public ASTNode getMainNode() {
		return assOrDecl;
	}
	
	public Expression getLeftHandSide() {
		return leftHandSide;
	}
	
	public Expression getRightHandSide() {
		return rightHandSide;
	}
	
	public int getOperator() {
		return operator;
	}
}


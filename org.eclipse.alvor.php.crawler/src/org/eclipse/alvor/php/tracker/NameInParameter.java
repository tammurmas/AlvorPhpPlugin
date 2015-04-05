package org.eclipse.alvor.php.tracker;

import org.eclipse.php.internal.core.ast.nodes.ASTNode;
import org.eclipse.php.internal.core.ast.nodes.FunctionDeclaration;

@SuppressWarnings("restriction")
public class NameInParameter extends NameUsage {

	private int index;
	private FunctionDeclaration funcDecl;
	
	public NameInParameter(FunctionDeclaration funcDecl, int index) {
		this.index = index;
		this.funcDecl = funcDecl;
	}
	
	public FunctionDeclaration getMethodDecl() {
		return funcDecl;
	}

	public int getParameterNo() {
		return index+1;
	}
	
	public ASTNode getParameterNode() {
		return (ASTNode)funcDecl.formalParameters().get(index);
	}
	
	public ASTNode getMainNode() {
		return getParameterNode();
	}

}

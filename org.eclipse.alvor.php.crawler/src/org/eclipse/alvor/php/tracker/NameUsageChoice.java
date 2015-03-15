package org.eclipse.alvor.php.tracker;

import org.eclipse.php.internal.core.ast.nodes.ASTNode;

@SuppressWarnings("restriction")
public class NameUsageChoice extends NameUsage {
	
	private NameUsage thenUsage;
	private NameUsage elseUsage;
	private ASTNode commonParentNode;
	
	public NameUsageChoice(ASTNode commonParentNode, NameUsage thenUsage, NameUsage elseUsage) {
		this.thenUsage = thenUsage;
		this.elseUsage = elseUsage;
		this.commonParentNode = commonParentNode;
	}
	
	public NameUsage getElseUsage() {
		return elseUsage;
	}
	
	public NameUsage getThenUsage() {
		return thenUsage;
	}
	
	public ASTNode getCommonParentNode() {
		return commonParentNode;
	}
	
	public ASTNode getMainNode() {
		return commonParentNode;
	}
}

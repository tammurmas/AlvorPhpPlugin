package org.eclipse.alvor.php.util;

import org.eclipse.php.internal.core.ast.nodes.ASTNode;

import com.googlecode.alvor.common.UnsupportedStringOpEx;


@SuppressWarnings("restriction")
public class UnsupportedStringOpExAtNode extends UnsupportedStringOpEx {
private static final long serialVersionUID = 1L;
	
	/**
	 * Brought ASTNode version to separate class to reduce dependencies
	 * @param message
	 * @param astNode
	 */
	public UnsupportedStringOpExAtNode(String message, ASTNode astNode) {
		super(message, ASTUtil.getPosition(astNode));
	}
}

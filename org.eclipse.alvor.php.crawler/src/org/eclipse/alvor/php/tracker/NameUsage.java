package org.eclipse.alvor.php.tracker;

import org.eclipse.php.internal.core.ast.nodes.ASTNode;

@SuppressWarnings("restriction")
/**
 * The abstract context of the variable usage
 * @author Urmas
 *
 */
abstract public class NameUsage {
	abstract public ASTNode getMainNode();
	
	@Override
	public int hashCode() {
		return this.getMainNode().hashCode();
	}

}


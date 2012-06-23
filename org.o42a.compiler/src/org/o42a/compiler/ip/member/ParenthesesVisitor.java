package org.o42a.compiler.ip.member;

import org.o42a.ast.Node;
import org.o42a.ast.NodeVisitor;
import org.o42a.ast.expression.ParenthesesNode;


final class ParenthesesVisitor extends NodeVisitor<ParenthesesNode, Void> {

	private static final ParenthesesVisitor PARENTHESES_VISITOR =
			new ParenthesesVisitor();

	public static ParenthesesNode extractParentheses(Node node) {
		return node.accept(PARENTHESES_VISITOR, null);
	}

	private ParenthesesVisitor() {
	}

	@Override
	public ParenthesesNode visitParentheses(
			ParenthesesNode parentheses,
			Void p) {
		return parentheses;
	}

	@Override
	protected ParenthesesNode visitAny(Node any, Void p) {
		return null;
	}

}

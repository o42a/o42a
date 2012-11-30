package org.o42a.parser.grammar.clause;

import static org.o42a.parser.Grammar.ref;
import static org.o42a.util.string.Characters.MINUS;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.BinaryNode;
import org.o42a.ast.expression.BinaryOperator;
import org.o42a.ast.ref.RefNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


final class BinaryClauseIdParser implements Parser<BinaryNode> {

	private static final OperatorParser OPERATOR = new OperatorParser();

	private final RefNode leftOperand;

	BinaryClauseIdParser(RefNode leftOperand) {
		this.leftOperand = leftOperand;
	}

	@Override
	public BinaryNode parse(ParserContext context) {

		final SignNode<BinaryOperator> sign = context.push(OPERATOR);

		if (sign == null) {
			return null;
		}

		final BinaryOperator operator = sign.getType();
		RefNode rightOperand = context.parse(ref());

		if (rightOperand == null) {
			context.getLogger().missingRightOperand(
					context.current(),
					operator.getSign());
		}

		return new BinaryNode(this.leftOperand, sign, rightOperand);
	}

	private static final class OperatorParser
			implements Parser<SignNode<BinaryOperator>> {

		@Override
		public SignNode<BinaryOperator> parse(ParserContext context) {

			final SourcePosition start = context.current().fix();
			final BinaryOperator operator;

			switch (context.next()) {
			case '-':
			case MINUS:
				operator = BinaryOperator.SUBTRACT;
				context.acceptAll();
				break;
			case '*':
				operator = BinaryOperator.MULTIPLY;
				context.acceptAll();
				break;
			case '/':
				operator = BinaryOperator.DIVIDE;
				context.acceptAll();
				break;
			case '+':
				operator = BinaryOperator.ADD;
				context.acceptAll();
				break;
			case '<':
				if (context.next() != '=') {
					return null;
				}
				if (context.next() != '>') {
					return null;
				}
				operator = BinaryOperator.COMPARE;
				context.acceptAll();
				break;
			case '=':
				if (context.next() != '=') {
					return null;
				}
				operator = BinaryOperator.EQUAL;
				context.acceptAll();
				break;
			default:
				return null;
			}

			final SignNode<BinaryOperator> result =
					new SignNode<BinaryOperator>(
							start,
							context.current().fix(),
							operator);

			return context.acceptComments(false, result);
		}

	}

}

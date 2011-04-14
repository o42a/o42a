package org.o42a.compiler.ip.phrase.part;

import static org.o42a.compiler.ip.phrase.part.NextClause.errorClause;

import org.o42a.ast.expression.UnaryNode;
import org.o42a.compiler.ip.phrase.ref.PhraseContext;
import org.o42a.core.member.clause.ClauseId;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.Statements;


public class UnaryPhrasePart extends PhraseContinuation {

	private final UnaryNode node;

	public UnaryPhrasePart(UnaryNode node, PhrasePart preceding) {
		super(preceding, preceding);
		this.node = node;
	}

	@Override
	public NextClause nextClause(PhraseContext context) {

		final ClauseId clauseId = clauseId();

		if (clauseId == null) {
			return errorClause(this.node);
		}

		return context.clauseById(this, clauseId);
	}

	private ClauseId clauseId() {

		switch (this.node.getOperator()) {
		case PLUS:
			return ClauseId.PLUS;
		case MINUS:
			return ClauseId.MINUS;
		case IS_TRUE:
		case NOT:
		case KNOWN:
		case UNKNOWN:
		}

		getLogger().error(
				"unsupported_unary",
				this.node.getSign(),
				"Unary operator '%s' not supported",
				this.node.getOperator().getSign());

		return null;
	}

	@Override
	public void define(Block<?> definition) {
		if (getPhrase().createsObject()) {
			// Phrase constructs object. No need to put an argument.
			return;
		}

		final Statements<?> statements =
			definition.propose(this).alternative(this);

		statements.assign(getPhrase().getAncestor().getRef());
	}

	@Override
	public String toString() {
		return this.node.getOperator().getSign() + getPreceding();
	}

}

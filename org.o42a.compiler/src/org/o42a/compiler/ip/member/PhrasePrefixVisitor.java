package org.o42a.compiler.ip.member;

import static org.o42a.compiler.ip.Interpreter.CLAUSE_DEF_IP;
import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.SampleSpecVisitor.parseAscendants;

import org.o42a.ast.expression.AbstractExpressionVisitor;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.ref.ScopeType;
import org.o42a.ast.type.AscendantsNode;
import org.o42a.core.Distributor;
import org.o42a.core.member.clause.ClauseBuilder;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.ref.Ref;


final class PhrasePrefixVisitor
		extends AbstractExpressionVisitor<ClauseBuilder, ClauseBuilder> {

	static final PhrasePrefixVisitor PHRASE_PREFIX_VISITOR =
			new PhrasePrefixVisitor();

	private PhrasePrefixVisitor() {
	}

	@Override
	public ClauseBuilder visitScopeRef(ScopeRefNode ref, ClauseBuilder p) {
		if (ref.getType() != ScopeType.IMPLIED) {
			return super.visitScopeRef(ref, p);
		}

		return p.setAscendants(new AscendantsDefinition(
				location(p, ref),
				p.distribute()));
	}

	@Override
	public ClauseBuilder visitAscendants(
			AscendantsNode ascendants,
			ClauseBuilder p) {

		final Distributor distributor = p.distribute();
		final AscendantsDefinition ascendantsDefinition =
				parseAscendants(CLAUSE_DEF_IP, ascendants, distributor);

		if (ascendantsDefinition == null) {
			return p.setAscendants(new AscendantsDefinition(
					location(p, ascendants),
					distributor));
		}

		return p.setAscendants(ascendantsDefinition);
	}

	@Override
	protected ClauseBuilder visitExpression(
			ExpressionNode expression,
			ClauseBuilder p) {

		final Distributor distributor = p.distribute();
		final Ref ancestor = expression.accept(
				CLAUSE_DEF_IP.targetExVisitor(),
				distributor);

		if (ancestor == null) {
			return p.setAscendants(new AscendantsDefinition(
					location(p, expression),
					distributor));
		}

		return p.setAscendants(
				new AscendantsDefinition(
						ancestor,
						distributor,
						ancestor.toTypeRef()));
	}

}

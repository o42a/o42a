/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

    This file is part of o42a.

    o42a is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    o42a is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.o42a.core.member.clause;

import static org.o42a.core.member.MemberName.clauseName;
import static org.o42a.core.member.clause.ClauseSubstitution.NO_SUBSTITUTION;
import static org.o42a.core.member.clause.impl.DeclaredGroupClause.declaredGroupClause;
import static org.o42a.core.member.clause.impl.DeclaredPlainClause.plainClause;
import static org.o42a.core.st.impl.SentenceErrors.prohibitedIssueAssignment;
import static org.o42a.util.ArrayUtil.append;

import org.o42a.core.*;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.MemberName;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.member.clause.impl.*;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.Location;
import org.o42a.core.st.sentence.*;
import org.o42a.util.ArrayUtil;
import org.o42a.util.string.Name;


public final class ClauseBuilder extends ClauseBuilderBase {

	static final ReusedClauseRef[] NOTHING_REUSED = new ReusedClauseRef[0];

	private final Statements<?, ?> statements;
	private final MemberRegistry memberRegistry;
	private final ClauseDeclaration declaration;

	private MemberId overridden;
	private StaticTypeRef declaredIn;
	private AscendantsDefinition ascendants;
	private Ref outcome;
	private ReusedClauseRef[] reusedClauses = NOTHING_REUSED;
	private BlockBuilder declarations;
	private boolean mandatory;
	private boolean prototype;
	private boolean assignment;
	private ClauseSubstitution substitution = NO_SUBSTITUTION;

	ClauseBuilder(
			Statements<?, ?> statements,
			MemberRegistry memberRegistry,
			ClauseDeclaration declaration) {
		this.statements = statements;
		this.memberRegistry = memberRegistry;
		this.declaration = declaration;
	}

	public final Statements<?, ?> getStatements() {
		return this.statements;
	}

	public final Obj getMemberOwner() {
		return this.memberRegistry.getOwner();
	}

	@Override
	public final Scope getScope() {
		return this.declaration.getScope();
	}

	@Override
	public final Location getLocation() {
		return this.declaration.getLocation();
	}

	public final CompilerContext getContext() {
		return getLocation().getContext();
	}

	@Override
	public final Container getContainer() {
		return this.declaration.getContainer();
	}

	public final CompilerLogger getLogger() {
		return getContext().getLogger();
	}

	public final ClauseDeclaration getDeclaration() {
		return this.declaration;
	}

	public final Ref getOutcome() {
		return this.outcome;
	}

	public final ClauseBuilder setOutcome(Ref outcome) {
		this.outcome = outcome;
		return this;
	}

	public final boolean isMandatory() {
		return this.mandatory;
	}

	public final ClauseBuilder mandatory() {
		this.mandatory = true;
		return this;
	}

	public final MemberId getOverridden() {
		return this.overridden;
	}

	public final ClauseBuilder setOverridden(MemberId overridden) {
		assert getDeclaration().getKind() == ClauseKind.OVERRIDER :
			"Field override expected";
		this.overridden = overridden;
		return this;
	}

	public final StaticTypeRef getDeclaredIn() {
		return this.declaredIn;
	}

	public final ClauseBuilder setDeclaredIn(StaticTypeRef declaredIn) {
		assert getDeclaration().getKind() == ClauseKind.OVERRIDER :
			"Field override expected";
		this.declaredIn = declaredIn;
		return this;
	}

	public final boolean isPrototype() {
		return this.prototype;
	}

	public final ClauseBuilder prototype() {
		assert getDeclaration().getKind() == ClauseKind.OVERRIDER :
			"Field override expected";
		this.prototype = true;
		return this;
	}

	public final boolean isAssignment() {
		return this.assignment;
	}

	public final ClauseBuilder assignment() {
		assert getDeclaration().getKind() == ClauseKind.EXPRESSION :
			"Only expressioncan be assigned";
		if (isAssignment() && getStatements().isInsideIssue()) {
			prohibitedIssueAssignment(getDeclaration());
			return null;
		}
		this.assignment = true;
		return this;
	}

	public final ClauseSubstitution getSubstitution() {
		return this.substitution;
	}

	public final ClauseBuilder setSubstitution(
			ClauseSubstitution substitution) {
		assert validSubstitution(substitution);
		this.substitution = substitution;
		return this;
	}

	public final AscendantsDefinition getAscendants() {
		return this.ascendants;
	}

	public final ClauseBuilder setAscendants(AscendantsDefinition ascendants) {
		this.ascendants = ascendants;
		return this;
	}

	public final BlockBuilder getDeclarations() {
		return this.declarations;
	}

	public final ClauseBuilder setDeclarations(BlockBuilder declarations) {
		assert getDeclaration().getKind().isPlain() :
			"Declarations block is only allowed for plain clause";
		assert !getSubstitution().substitutes() :
			"Can not provide declarations for substitution";
		this.declarations = declarations;
		return this;
	}

	public final ClauseBuilder reuseClause(
			Ref reusedClause,
			boolean reuseContents) {
		this.reusedClauses = append(
				this.reusedClauses,
				new ReusedClauseRef(reusedClause, reuseContents));
		return this;
	}

	public final Path outcome(Clause clause) {

		final Ref outcome = getOutcome();

		if (outcome == null) {

			final Clause enclosingClause = clause.getEnclosingClause();

			if (enclosingClause != null) {
				return enclosingClause.getOutcome();
			}

			return Path.SELF_PATH;
		}

		final OutcomeBuilder outcomeBuilder = new OutcomeBuilder(this.outcome);
		final Resolver resolver =
				clause.getEnclosingScope().walkingResolver(outcomeBuilder);

		if (!outcome.resolve(resolver).isResolved()) {
			return Path.SELF_PATH;
		}

		return outcomeBuilder.getOutcome();
	}

	public ReusedClause[] reuseClauses(Clause clause) {

		final ReusedClauseRef[] reusedRefs = this.reusedClauses;

		if (reusedRefs.length == 0) {
			return Clause.NOTHING_REUSED;
		}

		final ReusedClause[] reused = new ReusedClause[reusedRefs.length];
		int idx = 0;

		// Reuse in descending precedence order,
		// i.e. reverse to declaration order.
		for (int i = reusedRefs.length - 1; i >= 0; --i) {

			final ReusedClause reusedClause = reusedRefs[i].reuse(clause);

			if (reusedClause != null) {
				reused[idx++] = reusedClause;
			}
		}

		return ArrayUtil.clip(reused, idx);
	}

	public void build() {
		assert getDeclaration().getKind().isPlain() :
			"Plain clause declaration expected: " + getDeclaration();

		final DeclaredPlainClause clause = plainClause(this);

		getMemberRegistry().declareMember(clause.toMember());
		declareAlias(clause);

		getStatements().statement(
				new ClauseDeclarationStatement(this, clause, null));
	}

	@Override
	public final Distributor distribute() {
		return Contained.distribute(this);
	}

	@Override
	public final Distributor distributeIn(Container container) {
		return Contained.distributeIn(this, container);
	}

	@Override
	public final void assertScopeIs(Scope scope) {
		Scoped.assertScopeIs(this, scope);
	}

	@Override
	public final void assertCompatible(Scope scope) {
		Scoped.assertCompatible(this, scope);
	}

	@Override
	public final void assertSameScope(ScopeInfo other) {
		Scoped.assertSameScope(this, other);
	}

	@Override
	public final void assertCompatibleScope(ScopeInfo other) {
		Scoped.assertCompatibleScope(this, other);
	}

	@Override
	protected Block<?, ?> parentheses(Group group) {
		assert getDeclaration().getKind() == ClauseKind.GROUP :
			"Group declaration expected: " + getDeclaration();

		final DeclaredGroupClause clause = declaredGroupClause(this);
		final Block<?, ?> definition = clause.parentheses(group);

		if (definition == null) {
			return null;
		}

		getMemberRegistry().declareMember(clause.toMember());
		declareAlias(clause);

		group.getStatements().statement(
				new ClauseDeclarationStatement(this, clause, definition));

		return definition;
	}

	@Override
	protected ImperativeBlock braces(Group group, Name name) {
		assert getDeclaration().getKind() == ClauseKind.GROUP :
			"Group declaration expected: " + getDeclaration();

		final DeclaredGroupClause clause = declaredGroupClause(this);
		final ImperativeBlock definition = clause.braces(group, name);

		if (definition == null) {
			return null;
		}

		getMemberRegistry().declareMember(clause.toMember());
		declareAlias(clause);

		group.getStatements().statement(
				new ClauseDeclarationStatement(this, clause, definition));

		return definition;
	}

	final MemberRegistry getMemberRegistry() {
		return this.memberRegistry;
	}

	private boolean validSubstitution(ClauseSubstitution substitution) {
		if (substitution.substitutes()) {
			assert getDeclaration().getKind() == ClauseKind.EXPRESSION
					|| getDeclaration().getKind() == ClauseKind.OVERRIDER:
				"Can only substitute the value to assignment or overrider";
			assert getDeclarations() == null :
				"Can not provide the declarations for a value substitution";
		}
		return true;
	}

	private void declareAlias(Clause clause) {

		final Name name = getDeclaration().getName();

		if (name == null) {
			return;
		}

		final MemberId id = clause.toMember().getMemberId();
		final MemberName memberName = id.getMemberName();

		if (memberName != null && memberName.getName().is(name)) {
			return;
		}

		final MemberId aliasName = clauseName(name);
		final MemberId aliasId;
		final MemberId enclosingId = id.getEnclosingId();

		if (enclosingId != null) {
			aliasId = enclosingId.append(aliasName);
		} else {
			aliasId = aliasName;
		}

		final ClauseAlias alias = new ClauseAlias(aliasId, clause.toMember());

		getMemberRegistry().declareMember(alias);
	}

}

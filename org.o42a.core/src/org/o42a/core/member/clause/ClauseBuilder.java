/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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

import static org.o42a.core.member.clause.DeclaredGroupClause.declaredGroupClause;
import static org.o42a.core.member.clause.DeclaredPlainClause.plainClause;
import static org.o42a.util.ArrayUtil.append;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.*;
import org.o42a.core.member.DeclarationStatement;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.st.sentence.*;
import org.o42a.util.ArrayUtil;
import org.o42a.util.log.Loggable;


public final class ClauseBuilder extends ClauseBuilderBase {

	static final Ref[] NOTHING_REUSED = new Ref[0];

	private final MemberRegistry memberRegistry;
	private final ClauseDeclaration declaration;

	private boolean mandatory;
	private Ref overridden;
	private boolean prototype;
	private AscendantsDefinition ascendants;
	private Ref[] reusedClauses = NOTHING_REUSED;
	private BlockBuilder declarations;
	private boolean assignment;

	ClauseBuilder(
			MemberRegistry memberRegistry,
			ClauseDeclaration declaration) {
		this.memberRegistry = memberRegistry;
		this.declaration = declaration;
	}

	public final MemberOwner getMemberOwner() {
		return this.memberRegistry.getMemberOwner();
	}

	@Override
	public final Scope getScope() {
		return this.declaration.getScope();
	}

	@Override
	public final CompilerContext getContext() {
		return this.declaration.getContext();
	}

	@Override
	public Loggable getLoggable() {
		return this.declaration.getLoggable();
	}

	@Override
	public final ScopePlace getPlace() {
		return this.declaration.getPlace();
	}

	@Override
	public final Container getContainer() {
		return this.declaration.getContainer();
	}

	public final ClauseDeclaration getDeclaration() {
		return this.declaration;
	}

	public final boolean isMandatory() {
		return this.mandatory;
	}

	public final ClauseBuilder mandatory() {
		this.mandatory = true;
		return this;
	}

	public final Ref getOverridden() {
		return this.overridden;
	}

	public final ClauseBuilder setOverridden(Ref overridden) {
		assert getDeclaration().getKind() == ClauseKind.OVERRIDER :
			"Field override expected";
		this.overridden = overridden;
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
		this.assignment = true;
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
		this.declarations = declarations;
		return this;
	}

	public ClauseBuilder reuseClause(Ref reusedClause) {
		this.reusedClauses = append(this.reusedClauses, reusedClause);
		return this;
	}

	public final Ref[] getReusedClauses() {
		return this.reusedClauses;
	}

	public DeclarationStatement build() {
		assert getDeclaration().getKind().isPlain() :
			"Plain clause declaration expected: " + getDeclaration();

		final DeclaredPlainClause clause = plainClause(this);

		this.memberRegistry.declareMember(clause.toMember());

		return new ClauseDeclarationStatement(this, clause);
	}

	@Override
	public final Distributor distribute() {
		return Placed.distribute(this);
	}

	@Override
	public final Distributor distributeIn(Container container) {
		return Placed.distributeIn(this, container);
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
	protected Block<?> parentheses(Group group) {
		assert getDeclaration().getKind() == ClauseKind.GROUP :
			"Group declaration expected: " + getDeclaration();

		final DeclaredGroupClause clause = declaredGroupClause(this);
		final Block<?> definition = clause.parentheses(group);

		if (definition == null) {
			return null;
		}

		this.memberRegistry.declareMember(clause.toMember());

		group.getStatements().statement(new ClauseDeclarationStatement(this, clause));

		return definition;
	}

	@Override
	protected ImperativeBlock braces(Group group, String name) {
		assert getDeclaration().getKind() == ClauseKind.GROUP :
			"Group declaration expected: " + getDeclaration();

		final DeclaredGroupClause clause = declaredGroupClause(this);
		final ImperativeBlock definition = clause.braces(group, name);

		if (definition == null) {
			return null;
		}

		this.memberRegistry.declareMember(clause.toMember());

		group.getStatements().statement(new ClauseDeclarationStatement(this, clause));

		return definition;
	}

	final MemberRegistry getMemberRegistry() {
		return this.memberRegistry;
	}

	ReusedClause[] reuseClauses(Clause clause) {

		final Ref[] reusedRefs = getReusedClauses();

		if (reusedRefs.length == 0) {
			return Clause.NOTHING_REUSED;
		}

		final ReusedClause[] reused = new ReusedClause[reusedRefs.length];
		int idx = 0;

		for (int i = reusedRefs.length - 1; i >= 0; --i) {
			// Reuse in descending precedence order,
			// i.e. reverse to declaration order.
			final ReusedClause reusedClause =
				reuseClause(reusedRefs[i], clause);

			if (reusedClause != null) {
				reused[idx++] = reusedClause;
			}
		}

		return ArrayUtil.clip(reused, idx);
	}

	private ReusedClause reuseClause(Ref reusedClause, Clause clause) {

		final Path path = reusedClause.getPath();

		if (path == null) {
			clause.getContext().getLogger().invalidClauseReused(reusedClause);
			return null;
		}

		final ClauseReuser reuser = new ClauseReuser(reusedClause);

		if (path.walk(
				reusedClause,
				dummyUser(),
				clause.getEnclosingScope(),
				reuser) == null) {
			return null;
		}

		return reuser.getReused();
	}

}

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

import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.st.Reproducer;


final class DeclaredPlainClause extends PlainClause {

	static DeclaredPlainClause plainClause(ClauseBuilder builder) {
		return new PlainMember(builder).clause;
	}

	private final ClauseBuilder builder;
	private ClauseDefinition definition;
	private MemberKey overridden;
	private ReusedClause[] reused = NOTHING_REUSED;

	private DeclaredPlainClause(MemberClause clause, ClauseBuilder builder) {
		super(clause);
		this.builder = builder;
	}

	private DeclaredPlainClause(
			MemberOwner owner,
			DeclaredPlainClause overridden) {
		super(owner, overridden);
		this.builder = overridden.builder;
		this.definition = overridden.getDefinition();
		this.overridden = overridden.getOverridden();
	}

	public final ClauseDefinition getDefinition() {
		buildDefinition();
		return this.definition;
	}

	public final ClauseBuilder getBuilder() {
		return this.builder;
	}

	@Override
	public boolean isMandatory() {
		return this.builder.isMandatory();
	}

	@Override
	public Obj getObject() {
		buildDefinition();
		return getClauseObject();
	}

	@Override
	public boolean isAssignment() {
		return getBuilder().isAssignment();
	}

	@Override
	public AscendantsDefinition getAscendants() {
		return getBuilder().getAscendants();
	}

	@Override
	public final MemberKey getOverridden() {
		if (getKind() != ClauseKind.OVERRIDER) {
			return null;
		}
		buildDefinition();
		return this.overridden;
	}

	@Override
	public boolean isPrototype() {
		return this.builder.isPrototype();
	}

	@Override
	public final ReusedClause[] getReusedClauses() {
		buildDefinition();
		return this.reused;
	}

	@Override
	public void define(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());
		getDefinition().define(reproducer);
	}

	protected void merge(Clause clause) {
		getContext().getLogger().ambiguousClause(clause, getDisplayName());
	}

	@Override
	protected void fullyResolve() {

		final ClauseDefinition definition = getDefinition();

		if (definition != null) {
			definition.resolveAll();
		}
		validate();
	}

	@Override
	protected PlainClause propagate(MemberOwner owner) {
		return new DeclaredPlainClause(owner, this);
	}

	@Override
	protected Obj propagateClauseObject(PlainClause overridden) {
		return new PropagatedClauseDefinition(this, overridden);
	}

	private void buildDefinition() {
		if (getClauseObject() != null) {
			return;
		}
		if (getKind() == ClauseKind.OVERRIDER) {
			this.definition = createOverriderDefinition();
		} else {
			this.definition = createExpressionDefinition();
		}
		if (this.definition == null) {
			setClauseObject(getContext().getFalse());
		} else {
			setClauseObject(this.definition);
			this.reused = getBuilder().reuseClauses(this);
		}
	}

	private ClauseDefinition createOverriderDefinition() {
		this.overridden = overridden();
		if (this.overridden == null) {
			return null;
		}
		return new ClauseDefinition(this);
	}

	private ClauseDefinition createExpressionDefinition() {
		return new ClauseDefinition(this);
	}

	private MemberKey overridden() {

		final Ref overridden = getBuilder().getOverridden();
		final Path path = overridden.getPath();

		if (path == null) {
			getLogger().invalidOverridden(overridden);
			return null;
		}

		final OverriddenChecker checker = new OverriddenChecker(overridden);

		if (path.walk(
				overridden,
				dummyUser(),
				getEnclosingScope(),
				checker) == null) {
			return null;
		}

		return checker.getOverriddenKey();
	}

	private static final class PlainMember extends MemberClause {

		private final DeclaredPlainClause clause;

		PlainMember(ClauseBuilder builder) {
			super(builder.getMemberOwner(), builder.getDeclaration());
			this.clause = new DeclaredPlainClause(this, builder);
		}

		@Override
		public Clause toClause() {
			return this.clause;
		}

		@Override
		protected void merge(Member member) {

			final Clause clause = member.toClause();

			if (clause == null) {
				getContext().getLogger().notClauseDeclaration(member);
				return;
			}

			this.clause.merge(clause);
		}

	}

}

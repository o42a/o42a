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

import org.o42a.core.artifact.Accessor;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.st.Reproducer;
import org.o42a.util.Holder;


final class DeclaredPlainClause extends PlainClause {

	static DeclaredPlainClause plainClause(ClauseBuilder builder) {
		return new DeclaredPlainClauseMember(builder).toClause();
	}

	private final ClauseBuilder builder;
	private Holder<ClauseDefinition> definition;
	private MemberKey overridden;
	private ReusedClause[] reused;

	DeclaredPlainClause(
			DeclaredPlainClauseMember clause,
			ClauseBuilder builder) {
		super(clause);
		this.builder = builder;
	}

	private DeclaredPlainClause(
			MemberOwner owner,
			DeclaredPlainClause overridden) {
		super(owner, overridden);
		this.builder = overridden.builder;
		this.definition =
				new Holder<ClauseDefinition>(overridden.getDefinition());
		this.overridden = overridden.getOverridden();
		this.reused = overridden.getReusedClauses();
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

		final Obj object = getClauseObject();

		if (object != null) {
			return object;
		}

		final ClauseDefinition definition = getDefinition();

		if (definition != null) {
			return setClauseObject(definition);
		}

		return setClauseObject(getContext().getFalse());
	}

	@Override
	public boolean isAssignment() {
		return getBuilder().isAssignment();
	}

	@Override
	public boolean isSubstitution() {
		return getBuilder().isSubstitution();
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
		getDefinition();
		return this.overridden;
	}

	@Override
	public boolean isPrototype() {
		return this.builder.isPrototype();
	}

	@Override
	public final ReusedClause[] getReusedClauses() {
		if (this.reused != null) {
			return this.reused;
		}
		return this.reused = getBuilder().reuseClauses(this);
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
	protected void validate() {
		super.validate();
		if (isSubstitution()) {

			final ClauseId clauseId = getDeclaration().getClauseId();

			if (!clauseId.hasValue()) {
				getLogger().error(
						"incompatible_clause_substitution",
						this,
						"Clause substitution is not allowed in %s clause",
						clauseId);
			}
		}
	}

	@Override
	protected PlainClause propagate(MemberOwner owner) {
		return new DeclaredPlainClause(owner, this);
	}

	@Override
	protected Obj propagateClauseObject(PlainClause overridden) {
		return new PropagatedClauseDefinition(this, overridden);
	}

	private final ClauseDefinition getDefinition() {
		if (this.definition != null) {
			return this.definition.get();
		}

		final ClauseDefinition definition;

		if (getKind() == ClauseKind.OVERRIDER) {
			definition = createOverriderDefinition();
		} else {
			definition = createExpressionDefinition();
		}

		this.definition = new Holder<ClauseDefinition>(definition);

		return definition;
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

		final Obj enclosing = getScope().getEnclosingScope().toObject();
		final StaticTypeRef declaredIn = getBuilder().getDeclaredIn();

		final Member member = enclosing.objectMember(
				Accessor.INHERITANT,
				getBuilder().getOverridden(),
				declaredIn != null ? declaredIn.typeObject(dummyUser()) : null);

		if (member == null) {
			getLogger().error(
					"unknown_field_overridder",
					this,
					"Can not override unknown field %s",
					getBuilder().getOverridden());
			return null;
		}

		return member.getKey();
	}

}

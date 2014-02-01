/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.core.member.clause.impl;

import org.o42a.core.member.Accessor;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.clause.*;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.st.Reproducer;
import org.o42a.util.fn.Holder;


public final class DeclaredPlainClause extends PlainClause {

	public static DeclaredPlainClause plainClause(ClauseBuilder builder) {
		return new DeclaredPlainClauseMember(builder).clause();
	}

	private final ClauseBuilder builder;
	private Holder<ClauseDefinition> definition;
	private MemberKey overridden;
	private Path outcome;
	private ReusedClause[] reused;

	DeclaredPlainClause(
			DeclaredPlainClauseMember clause,
			ClauseBuilder builder) {
		super(clause);
		this.builder = builder;
	}

	DeclaredPlainClause(
			MemberClause clause,
			DeclaredPlainClause propagatedFrom) {
		super(clause, propagatedFrom);
		this.builder = propagatedFrom.builder;
		this.definition = new Holder<>(propagatedFrom.getDefinition());
		this.overridden = propagatedFrom.getOverridden();
		this.reused = propagatedFrom.getReusedClauses();
	}

	public final ClauseBuilder getBuilder() {
		return this.builder;
	}

	@Override
	public final boolean isMandatory() {
		return this.builder.isMandatory();
	}

	@Override
	public final Obj getObject() {
		return getDefinition();
	}

	@Override
	public final boolean isAssignment() {
		return getBuilder().isAssignment();
	}

	@Override
	public final ClauseSubstitution getSubstitution() {
		return getBuilder().getSubstitution();
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
	public Path getOutcome() {
		if (this.outcome != null) {
			return this.outcome;
		}
		return this.outcome = this.builder.outcome(this);
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
		getContext().getLogger().ambiguousClause(
				clause.getLocation(),
				getDisplayName());
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

		this.definition = new Holder<>(definition);
		setClauseObject(definition);

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
				declaredIn != null ? declaredIn.getType() : null);

		if (member == null) {
			getLogger().error(
					"unknown_field_overridder",
					this,
					"Can not override unknown field %s",
					getBuilder().getOverridden());
			return null;
		}

		return member.getMemberKey();
	}

}

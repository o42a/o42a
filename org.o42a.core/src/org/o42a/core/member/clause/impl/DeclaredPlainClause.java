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

import static org.o42a.util.fn.Init.init;
import static org.o42a.util.fn.NullableInit.nullableInit;

import org.o42a.core.member.Accessor;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.clause.*;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.member.field.PrototypeMode;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.st.Reproducer;
import org.o42a.util.fn.Init;
import org.o42a.util.fn.NullableInit;


public final class DeclaredPlainClause extends PlainClause {

	public static DeclaredPlainClause plainClause(ClauseBuilder builder) {
		return new DeclaredPlainClauseMember(builder).clause();
	}

	private final ClauseBuilder builder;
	private final NullableInit<MemberKey> overridden =
			nullableInit(this::findOverridden);
	private final NullableInit<ClauseDefinition> definition =
			nullableInit(this::createDefinition);
	private final Init<ReusedClause[]> reused =
			init(() -> getBuilder().reuseClauses(this));

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
		this.overridden.set(propagatedFrom.getOverridden());
		this.definition.set(propagatedFrom.getDefinition());
		this.reused.set(propagatedFrom.getReusedClauses());
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
		return this.overridden.get();
	}

	@Override
	public final PrototypeMode getPrototypeMode() {
		return this.builder.getPrototypeMode();
	}

	@Override
	public final ReusedClause[] getReusedClauses() {
		return this.reused.get();
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

	private final ClauseDefinition getDefinition() {
		return this.definition.get();
	}

	private ClauseDefinition createDefinition() {
		if (getKind() != ClauseKind.OVERRIDER) {
			return new ClauseDefinition(this);
		}
		if (getOverridden() == null) {
			return null;
		}
		return new ClauseDefinition(this);
	}

	private MemberKey findOverridden() {
		if (getKind() != ClauseKind.OVERRIDER) {
			return null;
		}

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

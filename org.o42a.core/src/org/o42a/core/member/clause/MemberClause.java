/*
    Compiler Core
    Copyright (C) 2010 Ruslan Lopatin

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

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.member.*;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.local.LocalScope;


public abstract class MemberClause extends Member {

	private final ClauseDeclaration declaration;
	private MemberKey key;

	public MemberClause(ClauseDeclaration declaration) {
		super(declaration, declaration.distribute());
		this.declaration = declaration;
	}

	MemberClause(Container container, MemberClause overridden) {
		super(overridden, overridden.distributeIn(container));
		this.key = overridden.getKey();
		this.declaration = overridden.declaration.overrideBy(this);
	}

	public final ClauseDeclaration getDeclaration() {
		return this.declaration;
	}

	@Override
	public MemberKey getKey() {
		if (this.key != null) {
			return this.key;
		}
		return this.key = getDeclaration().getMemberId().key(getScope());
	}

	@Override
	public MemberId getId() {
		return this.declaration.getMemberId();
	}

	@Override
	public final Field<?> toField() {
		return null;
	}

	@Override
	public final LocalScope toLocal() {
		return null;
	}

	@Override
	public final Visibility getVisibility() {
		return Visibility.PUBLIC;
	}

	@Override
	public final boolean isOverride() {
		return isPropagated();
	}

	@Override
	public boolean isPropagated() {
		return false;
	}

	@Override
	public final boolean isAbstract() {
		return false;
	}

	@Override
	public Container getSubstance() {
		return toClause().getContainer();
	}

	@Override
	public Member propagateTo(Scope scope) {
		return toClause().propagate(scope).toMember();
	}

	@Override
	public Member wrap(Member inherited, Container container) {
		switch (getDeclaration().getKind()) {
		case GROUP:
			return new GroupClauseWrap(
					container,
					inherited.toClause().toGroupClause(),
					toClause().toGroupClause()).toMember();
		case EXPRESSION:
		case OVERRIDER:
			return new PlainClauseWrap(
					container,
					inherited.toClause().toPlainClause(),
					toClause().toPlainClause()).toMember();
		}

		throw new IllegalStateException(
				"Can not wrap " + getDeclaration().getKind());
	}

	@Override
	public void resolveAll() {
		toClause().resolveAll();
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();
		final Scope enclosingScope = getScope();

		if (enclosingScope == getContext().getRoot().getScope()) {
			out.append("$$");
		} else {
			out.append(enclosingScope).append(':');
		}
		out.append(getDisplayName());
		if (isPropagated()) {
			out.append("{propagated}");
		}

		return out.toString();
	}

	@Override
	protected void merge(Member member) {
		throw new IllegalStateException();
	}

	static final class Overridden extends MemberClause {

		private final Clause clause;
		private final boolean propagated;

		Overridden(
				Container container,
				Clause clause,
				MemberClause overridden,
				boolean propagated) {
			super(container, overridden);
			this.clause = clause;
			this.propagated = propagated;
		}

		@Override
		public Clause toClause() {
			return this.clause;
		}

		@Override
		public boolean isPropagated() {
			return this.propagated;
		}

	}

}

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

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.st.Reproducer;


final class GroupClauseWrap extends GroupClause implements ClauseContainer {

	private final GroupClause iface;
	private final GroupClause wrapped;
	private LocalScope localScope;

	GroupClauseWrap(
			Container container,
			GroupClause iface,
			GroupClause wrapped) {
		super(container, wrapped, wrapped.toMember().isPropagated());
		this.iface = iface;
		this.wrapped = wrapped;
	}

	GroupClauseWrap(Container container, GroupClauseWrap overridden) {
		super(container, overridden);

		final Obj inherited = container.toObject();

		this.iface = inherited.member(
				overridden.getInterface().getKey()).toClause().toGroupClause();
		this.wrapped = inherited.getWrapped().member(
				overridden.getInterface().getKey()).toClause().toGroupClause();

	}

	public final GroupClause getInterface() {
		return this.iface;
	}

	public final GroupClause getWrapped() {
		return this.wrapped;
	}

	@Override
	public boolean isMandatory() {
		return this.wrapped.isMandatory();
	}

	@Override
	public boolean isImperative() {
		return this.wrapped.isImperative();
	}

	@Override
	public LocalScope getLocalScope() {
		if (this.localScope != null) {
			return null;
		}

		final LocalScope wrappedScope = this.wrapped.getLocalScope();

		if (wrappedScope == null) {
			return null;
		}

		this.localScope =
			member(wrappedScope.toMember().getKey()).toLocal(dummyUser());

		assert this.localScope != null :
			"Can not wrap local scope: " + wrappedScope;

		final LocalScopeClauseBase local = this.localScope;

		local.setClause(this);

		return this.localScope;
	}

	@Override
	public ClauseContainer getClauseContainer() {

		final LocalScope localScope = getLocalScope();

		return localScope != null ? localScope : this;
	}

	@Override
	public ReusedClause[] getReusedClauses() {
		return this.wrapped.getReusedClauses();
	}

	@Override
	public Clause clause(MemberId memberId, Obj declaredIn) {
		return groupClause(memberId, declaredIn);
	}

	@Override
	public void define(Reproducer reproducer) {
		this.wrapped.define(reproducer);
	}

	@Override
	protected GroupClause propagate(Scope enclosingScope) {
		return new GroupClauseWrap(enclosingScope.getContainer(), this);
	}

}

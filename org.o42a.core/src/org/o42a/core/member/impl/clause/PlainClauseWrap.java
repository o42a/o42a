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
package org.o42a.core.member.impl.clause;

import static org.o42a.core.def.Rescoper.upgradeRescoper;
import static org.o42a.core.def.Rescoper.wrapper;

import org.o42a.core.artifact.common.ObjectWrap;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.OverrideMode;
import org.o42a.core.member.clause.PlainClause;
import org.o42a.core.member.clause.ReusedClause;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.ref.path.Path;
import org.o42a.core.st.Reproducer;


public final class PlainClauseWrap extends PlainClause {

	private final PlainClause iface;
	private final PlainClause wrapped;
	private final AscendantsDefinition ascendants;

	public PlainClauseWrap(
			MemberOwner owner,
			PlainClause iface,
			PlainClause wrapped) {
		super(owner, wrapped, null, OverrideMode.WRAP);
		this.iface = iface;
		this.wrapped = wrapped;
		this.ascendants = wrapped.getAscendants().rescope(
				wrapper(getScope(), wrapped.getScope()));
		setClauseObject(new Wrap(this, wrapped.getObject()));
	}

	private PlainClauseWrap(MemberOwner owner, PlainClauseWrap overridden) {
		this(
				owner,
				overridden,
				owner.toObject().getWrapped()
				.member(overridden.getInterface().getKey())
				.toClause().toPlainClause());
	}

	private PlainClauseWrap(
			MemberOwner owner,
			PlainClauseWrap overridden,
			PlainClause wrapped) {
		super(owner, overridden, wrapped, OverrideMode.WRAP);
		this.iface =
				owner.toObject()
				.member(overridden.getInterface().getKey())
				.toClause().toPlainClause();
		this.wrapped = wrapped;
		this.ascendants = overridden.getAscendants().rescope(
				upgradeRescoper(overridden.getScope(), getScope()));
	}

	public final PlainClause getInterface() {
		return this.iface;
	}

	public final PlainClause getWrapped() {
		return this.wrapped;
	}

	@Override
	public boolean isMandatory() {
		return this.wrapped.isMandatory();
	}

	@Override
	public Obj getObject() {
		return getClauseObject();
	}

	@Override
	public boolean isAssignment() {
		return this.wrapped.isAssignment();
	}

	@Override
	public boolean isSubstitution() {
		return this.wrapped.isSubstitution();
	}

	@Override
	public AscendantsDefinition getAscendants() {
		return this.ascendants;
	}

	@Override
	public MemberKey getOverridden() {
		return this.wrapped.getOverridden();
	}

	@Override
	public boolean isPrototype() {
		return this.wrapped.isPrototype();
	}

	@Override
	public Path getOutcome() {
		return this.wrapped.getOutcome();
	}

	@Override
	public ReusedClause[] getReusedClauses() {
		return this.wrapped.getReusedClauses();
	}

	@Override
	public void define(Reproducer reproducer) {
		this.wrapped.define(reproducer);
	}

	@Override
	protected void fullyResolve() {
	}

	@Override
	protected PlainClause propagate(MemberOwner owner) {
		return new PlainClauseWrap(owner, this);
	}

	@Override
	protected Obj propagateClauseObject(PlainClause overridden) {
		return new Wrap(this, overridden.getObject());
	}

	private static final class Wrap extends ObjectWrap {

		Wrap(PlainClauseWrap scope) {
			super(scope, scope.getWrapped().getArtifact());
			System.err.println("(!) " + this);
		}

		Wrap(PlainClauseWrap scope, Obj sample) {
			super(scope, sample);
		}

		@Override
		public PlainClauseWrap toClause() {
			return (PlainClauseWrap) getScope();
		}

		@Override
		protected Obj createWrapped() {
			return toClause().getWrapped().getObject();
		}

		@Override
		protected Ascendants buildAscendants() {

			final Ascendants ascendants = new Ascendants(this);
			final Obj propagatedFrom = getPropagatedFrom();
			final Path path =
					propagatedFrom.getScope()
					.getEnclosingScopePath()
					.append(propagatedFrom.selfRef().getPath());

			return ascendants.addImplicitSample(
					path.bind(
							this,
							propagatedFrom.getScope().getEnclosingScope())
					.staticTypeRef(
							getScope().getEnclosingScope().distribute()));
		}

	}

}

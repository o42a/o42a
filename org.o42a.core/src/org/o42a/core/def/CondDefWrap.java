/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.core.def;

import org.o42a.core.ref.Logical;


public abstract class CondDefWrap extends CondDef {

	private final CondDef wrapped;

	public CondDefWrap(
			CondDef wrapped,
			LogicalDef prerequisite,
			Rescoper rescoper) {
		super(
				wrapped.getSource(),
				wrapped,
				prerequisite,
				rescoper);
		this.wrapped = wrapped;
	}

	protected CondDefWrap(
			CondDefWrap prototype,
			CondDef wrapped,
			LogicalDef prerequisite,
			Rescoper rescoper) {
		super(prototype, prerequisite, rescoper);
		this.wrapped = prototype.wrapped;
	}

	@Override
	public DefKind getKind() {
		return this.wrapped.getKind();
	}

	@Override
	public boolean hasPrerequisite() {
		return this.wrapped.hasPrerequisite();
	}

	@Override
	public final CondDef and(Logical logical) {

		final CondDef newDef = this.wrapped.and(logical);

		if (newDef == this.wrapped) {
			return this;
		}

		return create(newDef);
	}

	@Override
	public String toString() {
		return this.wrapped.toString();
	}

	@Override
	protected final LogicalDef buildPrerequisite() {
		return this.wrapped.getPrerequisite();
	}

	@Override
	protected Logical getLogical() {
		return this.wrapped.getLogical();
	}

	protected abstract CondDefWrap create(CondDef wrapped);

	@Override
	protected final CondDefWrap create(
			Rescoper rescoper,
			Rescoper additionalRescoper,
			LogicalDef prerequisite) {

		final CondDef newWrapped = this.wrapped.rescope(additionalRescoper);

		return create(rescoper, additionalRescoper, newWrapped, prerequisite);
	}

	protected abstract CondDefWrap create(
			Rescoper rescoper,
			Rescoper additionalRescoper,
			CondDef wrapped,
			LogicalDef prerequisite);

}

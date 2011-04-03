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


class FilteredCondDef extends CondDefWrap {

	private final DefKind kind;
	private final boolean hasPrerequisite;

	FilteredCondDef(
			CondDef def,
			LogicalDef prerequisite,
			boolean hasPrerequisite,
			boolean requirement) {
		super(def, prerequisite, def.getRescoper());
		this.hasPrerequisite = hasPrerequisite;
		this.kind = requirement ? DefKind.REQUIREMENT : DefKind.CONDITION;
	}

	private FilteredCondDef(
			FilteredCondDef prototype,
			CondDef wrapped,
			LogicalDef prerequisite,
			Rescoper rescoper) {
		super(prototype, wrapped, prerequisite, rescoper);
		this.hasPrerequisite = prototype.hasPrerequisite;
		this.kind = prototype.kind;
	}

	@Override
	public DefKind getKind() {
		return this.kind;
	}

	@Override
	public boolean hasPrerequisite() {
		return this.hasPrerequisite;
	}

	@Override
	public CondDef claim() {
		if (isRequirement()) {
			return this;
		}
		return new FilteredCondDef(
				this,
				prerequisite(),
				hasPrerequisite(),
				true);
	}

	@Override
	public CondDef unclaim() {
		if (!isRequirement()) {
			return this;
		}
		return new FilteredCondDef(
				this,
				prerequisite(),
				hasPrerequisite(),
				false);
	}

	@Override
	protected FilteredCondDef create(
			Rescoper rescoper,
			Rescoper additionalRescoper,
			CondDef wrapped,
			LogicalDef prerequisite) {
		return new FilteredCondDef(this, wrapped, prerequisite, rescoper);
	}

	@Override
	protected FilteredCondDef create(CondDef wrapped) {
		return new FilteredCondDef(
				wrapped,
				getPrerequisite(),
				hasPrerequisite(),
				isRequirement());
	}

}

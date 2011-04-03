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


class FilteredValueDef extends ValueDefWrap {

	private final DefKind kind;

	FilteredValueDef(ValueDef def, Logical prerequisite, boolean claim) {
		super(def, prerequisite, def.getRescoper());
		this.kind = claim ? DefKind.CLAIM : DefKind.PROPOSITION;
	}

	private FilteredValueDef(
			FilteredValueDef prototype,
			ValueDef wrapped,
			Logical prerequisite,
			Rescoper rescoper) {
		super(prototype, wrapped, prerequisite, rescoper);
		this.kind = prototype.kind;
	}

	@Override
	public DefKind getKind() {
		return this.kind;
	}

	@Override
	public ValueDef claim() {
		if (isClaim()) {
			return this;
		}
		return new FilteredValueDef(this, getPrerequisite(), true);
	}

	@Override
	public ValueDef unclaim() {
		if (!isClaim()) {
			return this;
		}
		return new FilteredValueDef(this, getPrerequisite(), false);
	}

	@Override
	protected FilteredValueDef create(
			Rescoper rescoper,
			Rescoper additionalRescoper,
			ValueDef wrapped) {
		return new FilteredValueDef(this, wrapped, getPrerequisite(), rescoper);
	}

	@Override
	protected FilteredValueDef create(ValueDef wrapped) {
		return new FilteredValueDef(wrapped, getPrerequisite(), isClaim());
	}

}

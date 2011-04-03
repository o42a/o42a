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


final class ValueCondDef extends CondDef {

	private final ValueDef def;

	ValueCondDef(ValueDef def) {
		super(def.getSource(), def, null, def.getRescoper());
		this.def = def;
	}

	private ValueCondDef(
			ValueCondDef prototype,
			LogicalDef prerequisite,
			Rescoper rescoper) {
		super(prototype, prerequisite, rescoper);
		this.def = prototype.def;
	}

	@Override
	public DefKind getKind() {
		if (this.def.isClaim()) {
			return DefKind.REQUIREMENT;
		}
		return DefKind.PROPOSITION;
	}

	@Override
	public boolean hasPrerequisite() {
		return this.def.hasPrerequisite();
	}

	@Override
	public CondDef and(Logical logical) {
		return this.def.and(logical).toCondition();
	}

	@Override
	protected LogicalDef buildPrerequisite() {
		return this.def.getPrerequisite();
	}

	@Override
	protected Logical getLogical() {
		return this.def.getLogical();
	}

	@Override
	protected CondDef create(
			Rescoper rescoper,
			Rescoper additionalRescoper,
			LogicalDef prerequisite) {
		return new ValueCondDef(this, prerequisite, rescoper);
	}

}

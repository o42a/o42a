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

import static org.o42a.core.def.Rescoper.transparentRescoper;
import static org.o42a.core.ref.Logical.logicalTrue;

import org.o42a.core.ref.Logical;


final class LogicalCondDef extends CondDef {

	private final Logical logical;

	public LogicalCondDef(Logical logical) {
		super(
				sourceOf(logical),
				logical,
				transparentRescoper(logical.getScope()));
		this.logical = logical;
	}

	private LogicalCondDef(LogicalCondDef prototype, Rescoper rescoper) {
		super(prototype, rescoper);
		this.logical = prototype.logical;
	}

	@Override
	protected Logical buildPrerequisite() {
		return logicalTrue(this, this.logical.getScope());
	}

	@Override
	protected Logical buildPrecondition() {
		return logicalTrue(this, this.logical.getScope());
	}

	@Override
	protected final Logical buildLogical() {
		return this.logical;
	}

	@Override
	protected CondDef create(
			Rescoper rescoper,
			Rescoper additionalRescoper) {
		return new LogicalCondDef(this, rescoper);
	}

	@Override
	protected void fullyResolveDef() {
		this.logical.resolveAll();
	}

}

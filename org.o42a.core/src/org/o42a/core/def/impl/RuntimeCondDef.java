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
package org.o42a.core.def.impl;

import static org.o42a.core.Rescoper.transparentRescoper;
import static org.o42a.core.ref.Logical.logicalTrue;
import static org.o42a.core.ref.Logical.runtimeLogical;

import org.o42a.core.Rescoper;
import org.o42a.core.def.CondDef;
import org.o42a.core.def.Definitions;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.Resolver;


public final class RuntimeCondDef extends CondDef {

	private final Definitions definitions;

	public RuntimeCondDef(Definitions definitions) {
		super(
				/* The source should differ from scope,
				 * as this definition is not explicit. */
				definitions.getContext().getVoid(),
				definitions,
				transparentRescoper(definitions.getScope()));
		this.definitions = definitions;
	}

	private RuntimeCondDef(RuntimeCondDef prototype, Rescoper rescoper) {
		super(prototype, rescoper);
		this.definitions = prototype.definitions;
	}

	@Override
	protected Logical buildPrerequisite() {
		return logicalTrue(this, this.definitions.getScope());
	}

	@Override
	protected Logical buildPrecondition() {
		return logicalTrue(this, this.definitions.getScope());
	}

	@Override
	protected Logical buildLogical() {
		return runtimeLogical(this, this.definitions.getScope());
	}

	@Override
	protected void fullyResolveDef(Resolver resolver) {
	}

	@Override
	protected RuntimeCondDef create(
			Rescoper rescoper,
			Rescoper additionalRescoper) {
		return new RuntimeCondDef(this, rescoper);
	}

	@Override
	protected String name() {
		return "RuntimeCondDef";
	}

}

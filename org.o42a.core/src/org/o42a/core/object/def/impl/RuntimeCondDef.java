/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.core.object.def.impl;

import static org.o42a.core.ref.Logical.logicalTrue;
import static org.o42a.core.ref.Logical.runtimeLogical;
import static org.o42a.core.ref.ScopeUpgrade.noScopeUpgrade;

import org.o42a.core.ir.op.InlineCond;
import org.o42a.core.object.def.CondDef;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.ref.*;


public final class RuntimeCondDef extends CondDef {

	private final Definitions definitions;

	public RuntimeCondDef(Definitions definitions) {
		super(
				/* The source should differ from scope,
				 * as this definition is not explicit. */
				definitions.getContext().getVoid(),
				definitions,
				noScopeUpgrade(definitions.getScope()));
		this.definitions = definitions;
	}

	private RuntimeCondDef(
			RuntimeCondDef prototype,
			ScopeUpgrade scopeUpgrade) {
		super(prototype, scopeUpgrade);
		this.definitions = prototype.definitions;
	}

	@Override
	public void normalize(RootNormalizer normalizer) {
	}

	@Override
	protected String name() {
		return "RuntimeCondDef";
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
			ScopeUpgrade upgrade,
			ScopeUpgrade additionalUpgrade) {
		return new RuntimeCondDef(this, upgrade);
	}

	@Override
	protected InlineCond inlineDef(Normalizer normalizer) {
		throw new UnsupportedOperationException(
				"Run-time definition can not generate code");
	}

}

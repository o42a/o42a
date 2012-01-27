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
package org.o42a.core.def;

import org.o42a.core.ref.*;


final class ValueCondDef extends CondDef {

	private final ValueDef def;

	ValueCondDef(ValueDef def) {
		super(def.getSource(), def.getLocation(), def.getScopeUpgrade());
		this.def = def;
		update(
				def.isClaim() ? DefKind.REQUIREMENT : DefKind.CONDITION,
				def.hasPrerequisite());
	}

	private ValueCondDef(ValueCondDef prototype, ScopeUpgrade scopeUpgrade) {
		super(prototype, scopeUpgrade);
		this.def = prototype.def;
	}

	@Override
	protected Logical buildPrerequisite() {
		return this.def.getPrerequisite();
	}

	@Override
	protected Logical buildPrecondition() {
		return this.def.getPrecondition();
	}

	@Override
	protected Logical buildLogical() {
		return this.def.buildLogical();
	}

	@Override
	protected CondDef create(
			ScopeUpgrade upgrade,
			ScopeUpgrade additionalUpgrade) {
		return new ValueCondDef(this, upgrade);
	}

	@Override
	protected void fullyResolveDef(Resolver resolver) {
		this.def.fullyResolve(resolver);
	}

	@Override
	protected InlineCond inlineDef(Normalizer normalizer) {
		return this.def.inline(normalizer, this.def.getValueStruct());
	}

	@Override
	protected void normalizeDef(Normalizer normalizer) {
		this.def.normalize(normalizer);
	}

}

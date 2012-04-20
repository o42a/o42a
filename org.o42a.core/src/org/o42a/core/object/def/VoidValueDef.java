/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.object.def;

import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.*;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;


final class VoidValueDef extends ValueDef {

	private final ValueDef def;

	VoidValueDef(ValueDef def) {
		super(def.getSource(), def.getLocation(), def.getScopeUpgrade());
		this.def = def;
	}

	private VoidValueDef(VoidValueDef prototype, ScopeUpgrade scopeUpgrade) {
		super(prototype, scopeUpgrade);
		this.def = prototype.def;
	}

	@Override
	public final ValueStruct<?, ?> getValueStruct() {
		return ValueStruct.VOID;
	}

	@Override
	public boolean unconditional() {
		return true;
	}

	@Override
	public void normalize(RootNormalizer normalizer) {
		this.def.normalize(normalizer);
	}

	@Override
	protected boolean hasConstantValue() {
		return this.def.hasConstantValue();
	}

	@Override
	protected Value<?> calculateValue(Resolver resolver) {

		final Value<?> value = this.def.calculateValue(resolver);

		return value.getKnowledge().getCondition().toValue(ValueStruct.VOID);
	}

	@Override
	protected ValueDef create(
			ScopeUpgrade upgrade,
			ScopeUpgrade additionalUpgrade) {
		return new VoidValueDef(this, upgrade);
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
	protected void fullyResolveDef(Resolver resolver) {
		this.def.fullyResolve(resolver);
	}

	@Override
	protected InlineValue inlineDef(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected ValOp writeValue(ValDirs dirs, HostOp host) {
		throw new UnsupportedOperationException();
	}

}

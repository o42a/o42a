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

import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


final class CondValueDef extends ValueDef {

	private final CondDef def;

	CondValueDef(CondDef def) {
		super(def.getSource(), def.getLocation(), def.getRescoper());
		this.def = def;
		update(
				def.isRequirement() ? DefKind.CLAIM : DefKind.PROPOSITION,
				def.hasPrerequisite());
	}

	private CondValueDef(CondValueDef prototype, Rescoper rescoper) {
		super(prototype, rescoper);
		this.def = prototype.def;
	}

	@Override
	public ValueType<?> getValueType() {
		return ValueType.VOID;
	}

	@Override
	protected boolean hasConstantValue() {
		return this.def.isConstant();
	}

	@Override
	protected Value<?> calculateValue(Resolver resolver) {
		return this.def.getLogical().logicalValue(resolver).toValue();
	}

	@Override
	protected ValueDef create(Rescoper rescoper, Rescoper additionalRescoper) {
		return new CondValueDef(this, rescoper);
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
	protected ValOp writeValue(ValDirs dirs, HostOp host) {
		this.def.getLogical().write(dirs.dirs(), host);
		return dirs.value().storeVoid(dirs.code());
	}

}

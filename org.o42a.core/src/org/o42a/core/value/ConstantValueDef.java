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
package org.o42a.core.value;

import static org.o42a.core.def.Rescoper.transparentRescoper;
import static org.o42a.core.ref.Logical.logicalTrue;

import org.o42a.core.def.Rescoper;
import org.o42a.core.def.ValueDef;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.Resolver;


final class ConstantValueDef<T> extends ValueDef {

	private final ConstantObject<T> source;

	ConstantValueDef(ConstantObject<T> source) {
		super(source, source, transparentRescoper(source.getScope()));
		this.source = source;
	}

	private ConstantValueDef(ConstantValueDef<T> prototype, Rescoper rescoper) {
		super(prototype, rescoper);
		this.source = prototype.source;
	}

	@Override
	public ValueType<?> getValueType() {
		return this.source.getValueType();
	}

	@Override
	protected Value<?> calculateValue(Resolver resolver) {
		return this.source.getValue();
	}

	@Override
	protected void fullyResolveDef(Resolver resolver) {
		this.source.resolveDefinitions(resolver);
	}

	@Override
	protected Logical buildPrerequisite() {
		return logicalTrue(this, this.source.getScope());
	}

	@Override
	protected Logical buildPrecondition() {
		return logicalTrue(this, this.source.getScope());
	}

	@Override
	protected Logical buildLogical() {
		return logicalTrue(this, this.source.getScope());
	}

	@Override
	protected ValueDef create(Rescoper rescoper, Rescoper additionalRescoper) {
		return new ConstantValueDef<T>(this, rescoper);
	}

	@Override
	protected ValOp writeValue(ValDirs dirs, HostOp host) {
		return this.source.getValue().op(dirs.getBuilder(), dirs.code());
	}

	@Override
	protected String name() {
		return "ConstantValueDef";
	}

}

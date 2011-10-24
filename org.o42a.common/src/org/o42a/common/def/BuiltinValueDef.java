/*
    Modules Commons
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
package org.o42a.common.def;

import static org.o42a.core.def.Rescoper.transparentRescoper;
import static org.o42a.core.ref.Logical.logicalTrue;
import static org.o42a.core.ref.Logical.runtimeLogical;

import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.Rescoper;
import org.o42a.core.def.ValueDef;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;


public class BuiltinValueDef extends ValueDef {

	private final Builtin builtin;

	public BuiltinValueDef(Builtin builtin) {
		super(
				builtin.toObject(),
				builtin,
				transparentRescoper(builtin.toObject().getScope()));
		this.builtin = builtin;
	}

	private BuiltinValueDef(BuiltinValueDef prototype, Rescoper rescoper) {
		super(prototype, rescoper);
		this.builtin = prototype.builtin;
	}

	@Override
	public ValueStruct<?, ?> getValueStruct() {
		return this.builtin.toObject().value().getValueStruct();
	}

	@Override
	protected boolean hasConstantValue() {
		return this.builtin.isConstantBuiltin();
	}

	@Override
	protected Logical buildPrerequisite() {
		return logicalTrue(this, this.builtin.toObject().getScope());
	}

	@Override
	protected Logical buildPrecondition() {
		return logicalTrue(this, this.builtin.toObject().getScope());
	}

	@Override
	protected Logical buildLogical() {
		return runtimeLogical(this, this.builtin.toObject().getScope());
	}

	@Override
	protected BuiltinValueDef create(
			Rescoper rescoper,
			Rescoper additionalRescoper) {
		return new BuiltinValueDef(this, rescoper);
	}

	@Override
	protected Value<?> calculateValue(Resolver resolver) {
		return this.builtin.calculateBuiltin(resolver);
	}

	@Override
	protected void fullyResolveDef(Resolver resolver) {

		final Obj object = resolver.getContainer().toObject();
		final Obj builtin = this.builtin.toObject();

		if (builtin != object) {
			builtin.value().resolveAll(resolver);
		}
		object.resolveAll();
		this.builtin.resolveBuiltin(
				object.value().valuePart(isClaim()).resolver());
	}

	@Override
	protected ValOp writeValue(ValDirs dirs, HostOp host) {
		return this.builtin.writeBuiltin(dirs, host);
	}

	@Override
	protected String name() {
		return "BuiltinValueDef";
	}

}

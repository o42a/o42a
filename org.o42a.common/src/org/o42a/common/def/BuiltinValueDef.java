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

import org.o42a.codegen.code.Code;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.Rescoper;
import org.o42a.core.def.ValueDef;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.util.use.UserInfo;


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
	public ValueType<?> getValueType() {
		return this.builtin.toObject().getValueType();
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
	protected void fullyResolveDef(UserInfo user) {

		final Obj object =
			getRescoper().rescope(getScope()).getContainer().toObject();

		object.resolveAll();
		this.builtin.resolveBuiltin(object);
	}

	@Override
	protected void writeValue(CodeDirs dirs, ValOp result, HostOp host) {

		final Code code = dirs.code();

		this.builtin.writeBuiltin(code, result, host);
		result.go(code, dirs);
	}

	@Override
	protected String name() {
		return "BuiltinValueDef";
	}

}

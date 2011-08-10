/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core.ir.object;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ObjectValFunc;
import org.o42a.core.ir.value.ValOp;


public class ObjectTypeOp extends IROp {

	private final ObjectPrecision precision;

	ObjectTypeOp(
			CodeBuilder builder,
			ObjectIRType.Op ptr,
			ObjectPrecision precision) {
		super(builder, ptr);
		this.precision = precision;
	}

	public final ObjectPrecision getPrecision() {
		return this.precision;
	}

	@Override
	public final ObjectIRType.Op ptr() {
		return (ObjectIRType.Op) super.ptr();
	}

	public final ObjectOp object(Code code, Obj wellKnownType) {
		return new AnonymousObjOp(
				this,
				mainBody(code),
				wellKnownType);
	}

	public final DataOp start(Code code) {
		return ptr().data(code).loadStart(code);
	}

	public final ObjOp objectOfType(Code code, Obj type) {
		return mainBody(code).to(
				null,
				code, type.ir(getGenerator()).getBodyType())
				.op(this, type);
	}

	public final ValOp writeValue(ValDirs dirs, ObjectOp body) {

		final Code code = dirs.code();
		final ObjectValFunc function =
				ptr().data(code).valueFunc(code).load(null, code);
		final ValOp result = function.call(dirs, body(code, body));

		code.dump("Value: ", result.ptr());

		return result;
	}

	public final void writeRequirement(CodeDirs dirs, ObjectOp body) {

		final Code code = dirs.code();
		final ObjectCondFunc function =
				ptr().data(code).requirementFunc(code).load(null, code);

		function.call(code, body(code, body)).go(code, dirs);
	}

	public final ValOp writeClaim(ValDirs dirs, ObjectOp body) {

		final Code code = dirs.code();
		final ObjectValFunc function =
				ptr().data(code).claimFunc(code).load(null, code);

		return function.call(dirs, body(code, body));
	}

	public final void writeCondition(CodeDirs dirs, ObjectOp body) {

		final Code code = dirs.code();
		final ObjectCondFunc function =
				ptr().data(code).conditionFunc(code).load(null, code);

		function.call(code, body(code, body)).go(code, dirs);
	}

	public final ValOp writeProposition(ValDirs dirs, ObjectOp body) {

		final Code code = dirs.code();
		final ObjectValFunc function =
				ptr().data(code).propositionFunc(code).load(null, code);

		return function.call(dirs, body(code, body));
	}

	public final ValOp writeOverriddenValue(ValDirs dirs) {
		// TODO overridden value
		return null;
	}

	public final void writeOverriddenRequirement(CodeDirs dirs) {
		// TODO overridden requirement
	}

	public final void writeOverriddenCondition(CodeDirs dirs) {
		// TODO overridden condition
	}

	public final ValOp writeOverriddenClaim(ValDirs dirs) {
		// TODO overridden claim
		return null;
	}

	public final ValOp writeOverriddenProposition(ValDirs dirs) {
		// TODO overridden definition
		return null;
	}

	@Override
	public String toString() {
		return "ObjectData[" + ptr().toString() + ']';
	}

	private final DataOp body(Code code, ObjectOp body) {
		return body != null ? body.toData(code) : mainBody(code);
	}

	private final DataOp mainBody(Code code) {
		return ptr().data(code).loadObject(code);
	}

}

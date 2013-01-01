/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.object.impl.AnonymousObjOp;
import org.o42a.core.ir.object.value.ObjectValueFunc;
import org.o42a.core.ir.op.IROp;
import org.o42a.core.ir.value.ObjectValFunc;
import org.o42a.core.object.Obj;


public final class ObjectTypeOp extends IROp {

	private final ObjectPrecision precision;
	private final ObjectIRTypeOp ptr;

	ObjectTypeOp(
			CodeBuilder builder,
			ObjectIRTypeOp ptr,
			ObjectPrecision precision) {
		super(builder);
		this.ptr = ptr;
		this.precision = precision;
	}

	public final ObjectPrecision getPrecision() {
		return this.precision;
	}

	@Override
	public final ObjectIRTypeOp ptr() {
		return this.ptr;
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
				code,
				type.ir(getGenerator()).getBodyType())
				.op(null, this, type);
	}

	public final void writeValue(DefDirs dirs) {

		final Code code = dirs.code();
		final ObjectValueFunc function =
				ptr().data(code).valueFunc(code).load(null, code);
		final ObjectIRDataOp data = ptr().data(code);

		function.call(dirs, data, data.loadObject(code));
	}

	public final void writeClaim(DefDirs dirs, ObjectOp body) {

		final Code code = dirs.code();
		final ObjectValFunc function =
				ptr().data(code).claimFunc(code).load(null, code);

		function.call(dirs, body(code, body));
	}

	public final void writeProposition(DefDirs dirs, ObjectOp body) {

		final Block code = dirs.code();
		final ObjectValFunc function =
				ptr().data(code).propositionFunc(code).load(null, code);

		function.call(dirs, body(code, body));
	}

	@Override
	public String toString() {
		return "ObjectData[" + ptr().toString() + ']';
	}

	private final DataOp body(Code code, ObjectOp body) {
		return body != null ? body.toData(null, code) : mainBody(code);
	}

	private final DataOp mainBody(Code code) {
		return ptr().data(code).loadObject(code);
	}

}

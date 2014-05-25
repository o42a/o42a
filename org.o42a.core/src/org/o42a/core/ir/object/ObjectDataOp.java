/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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
import org.o42a.core.ir.object.value.ObjectCondFunc;
import org.o42a.core.ir.object.value.ObjectValueFunc;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.DefiniteIROp;
import org.o42a.core.ir.value.ObjectDefFunc;
import org.o42a.core.object.Obj;


public final class ObjectDataOp extends DefiniteIROp {

	private final ObjectPrecision precision;
	private final ObjectIRDataOp ptr;

	ObjectDataOp(
			CodeBuilder builder,
			ObjectIRDataOp ptr,
			ObjectPrecision precision) {
		super(builder);
		this.ptr = ptr;
		this.precision = precision;
	}

	public final ObjectPrecision getPrecision() {
		return this.precision;
	}

	@Override
	public final ObjectIRDataOp ptr() {
		return this.ptr;
	}

	@Override
	public final ObjectIRDataOp ptr(Code code) {
		return ptr();
	}

	public final ObjectIRDescOp loadDesc(Code code) {
		return ptr().desc(code).load(null, code);
	}

	public final ObjectOp object(Code code, Obj wellKnownType) {
		return new AnonymousObjOp(
				this,
				mainBody(code),
				wellKnownType);
	}

	public final DataOp start(Code code) {
		return ptr().loadStart(code);
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
				ptr().valueFunc(code).load(null, code);

		function.call(dirs, ptr(), ptr().loadObject(code));
	}

	public final void writeCond(CodeDirs dirs) {

		final Code code = dirs.code();
		final ObjectCondFunc function =
				ptr().condFunc(code).load(null, code);

		function.call(dirs, ptr().loadObject(code));
	}

	public final void writeDefs(DefDirs dirs, ObjectOp body) {

		final Block code = dirs.code();
		final ObjectDefFunc function =
				ptr().defsFunc(code).load(null, code);

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
		return ptr().loadObject(code);
	}

}

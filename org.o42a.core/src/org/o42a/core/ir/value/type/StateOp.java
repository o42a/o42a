/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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
package org.o42a.core.ir.value.type;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.value.ValueType;


public abstract class StateOp {

	private final FldOp<?> fld;

	public StateOp(FldOp<?> fld) {
		this.fld = fld;
	}

	public final Generator getGenerator() {
		return fld().getGenerator();
	}

	public final CompilerContext getContext() {
		return fld().getContext();
	}

	public final CodeBuilder getBuilder() {
		return fld().getBuilder();
	}

	public final ValueType<?> getValueType() {
		return fld()
				.fld()
				.getBodyIR()
				.getObjectIR()
				.getObject()
				.type()
				.getValueType();
	}

	public final FldOp<?> fld() {
		return this.fld;
	}

	public abstract void useByValueFunction(Code code);

	public final ValOp writeValue(ValDirs dirs) {

		final ValOp value = dirs.value();

		assert dirs.getValueType().is(getValueType()) :
			"Wrong value type: " + getValueType()
			+ ", but " + dirs.getValueType() + " expected";

		final Block code = dirs.code();

		start(code);

		final Block definite = code.addBlock("definite");

		loadIndefinite(code).goUnless(code, definite.head());

		value.store(code, constructValue(dirs));
		code.dump(this + " value calculated: ", value);

		loadCondition(definite).goUnless(definite, dirs.falseDir());
		value.store(definite, loadValue(dirs, definite));
		definite.dump(this + " value is definite: ", value);
		definite.go(code.tail());

		return value;
	}

	public abstract BoolOp loadCondition(Code code);

	public abstract ValOp loadValue(ValDirs dirs, Code code);

	public abstract void init(Block code, ValOp value);

	public abstract void initToFalse(Block code);

	public abstract void assign(CodeDirs dirs, ObjectOp value);

	protected void start(final Block code) {
		code.acquireBarrier();
	}

	protected abstract BoolOp loadIndefinite(Code code);

	protected ValOp constructValue(ValDirs dirs) {

		final DefDirs defDirs = dirs.nested().def();

		fld().host().objectType(defDirs.code()).writeValue(defDirs);
		defDirs.done();

		return defDirs.result();
	}

}

/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import static org.o42a.codegen.code.op.Atomicity.ATOMIC;
import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.object.ObjectIRDataOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValFlagsOp;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.value.ValueType;


public abstract class StateOp {

	private final ObjectOp host;
	private ObjectIRDataOp data;
	private ValType.Op value;
	private ValFlagsOp flags;

	public StateOp(ObjectOp host) {
		this.host = host;
	}

	public final Generator getGenerator() {
		return host().getGenerator();
	}

	public final CompilerContext getContext() {
		return host().getContext();
	}

	public final CodeBuilder getBuilder() {
		return host().getBuilder();
	}

	public ValueType<?> getValueType() {
		return host().getWellKnownType().type().getValueType();
	}

	public final ObjectOp host() {
		return this.host;
	}

	public final ValType.Op value() {
		return this.value;
	}

	public final ValFlagsOp flags() {
		return this.flags;
	}

	public final ObjectIRDataOp data() {
		return this.data;
	}

	public void startEval(Block code, ObjectIRDataOp data) {
		this.data = data;
		this.value = this.data.value(code);
		this.flags = this.value.flags(code, ATOMIC);
	}

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

	public BoolOp loadCondition(Code code) {
		return this.flags.condition(null, code);
	}

	public ValOp loadValue(ValDirs dirs, Code code) {
		return dirs.value().store(
				code,
				this.value.op(
						code.getAllocator(),
						dirs.getBuilder(),
						getValueType(),
						TEMP_VAL_HOLDER));
	}

	public abstract void init(Block code, ValOp value);

	public void initToFalse(Block code) {
		code.releaseBarrier();
		this.flags.storeFalse(code);
	}

	public abstract void assign(CodeDirs dirs, ObjectOp value);

	protected void start(Block code) {
		this.data = host().objectData(code).ptr();
		this.value = this.data.value(code);
		code.acquireBarrier();
		this.flags = this.value.flags(code, ATOMIC);
	}

	protected BoolOp loadIndefinite(Code code) {
		return this.flags.indefinite(null, code);
	}

	protected ValOp constructValue(ValDirs dirs) {

		final DefDirs defDirs = dirs.nested().def();

		host().objectData(defDirs.code()).writeValue(defDirs);
		defDirs.done();

		return defDirs.result();
	}

}

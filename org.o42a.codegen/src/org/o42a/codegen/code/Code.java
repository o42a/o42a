/*
    Compiler Code Generator
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
package org.o42a.codegen.code;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Type;
import org.o42a.codegen.debug.DebugCodeBase;


public abstract class Code extends DebugCodeBase {

	private final CodeId id;
	private final Head head = new Head(this);
	private int localSeq;
	int blockSeq;

	Code(Code enclosing, CodeId name) {
		super(enclosing);
		this.id = enclosing.nestedId(name);
	}

	Code(Generator generator, CodeId id) {
		super(generator);
		this.id = id;
	}

	public final CodeId getId() {
		return this.id;
	}

	public final CodeId id() {
		return getGenerator().id();
	}

	public final CodeId id(String name) {
		return getGenerator().id(name);
	}

	public final CodePos head() {
		if (exists()) {
			return writer().head();
		}
		return this.head;
	}

	public final CodePos tail() {
		assert assertIncomplete();
		return writer().tail();
	}

	public final AllocationCode allocate() {
		return new AllocationCode(this, null);
	}

	public final AllocationCode allocate(CodeId name) {
		return new AllocationCode(this, name);
	}

	public final CodeBlk addBlock(String name) {
		assert assertIncomplete();
		return new CodeBlk(this, getGenerator().id(name));
	}

	public final CodeBlk addBlock(CodeId name) {
		assert assertIncomplete();
		return new CodeBlk(this, name);
	}

	public final void go(CodePos pos) {
		assert assertIncomplete();
		writer().go(unwrapPos(pos));
	}

	public final Int8op int8(byte value) {
		assert assertIncomplete();
		return writer().int8(value);
	}

	public final Int16op int16(short value) {
		assert assertIncomplete();
		return writer().int16(value);
	}

	public final Int32op int32(int value) {
		assert assertIncomplete();
		return writer().int32(value);
	}

	public final Int64op int64(long value) {
		assert assertIncomplete();
		return writer().int64(value);
	}

	public final Fp32op fp32(float value) {
		assert assertIncomplete();
		return writer().fp32(value);
	}

	public final Fp64op fp64(double value) {
		assert assertIncomplete();
		return writer().fp64(value);
	}

	public final BoolOp bool(boolean value) {
		assert assertIncomplete();
		return writer().bool(value);
	}

	public final RelOp nullRelPtr() {
		assert assertIncomplete();
		return writer().nullRelPtr();
	}

	public final AnyOp nullPtr() {
		assert assertIncomplete();
		return writer().nullPtr();
	}

	public final DataOp nullDataPtr() {
		assert assertIncomplete();
		return writer().nullDataPtr();
	}

	public final <O extends StructOp> O nullPtr(Type<O> type) {
		assert assertIncomplete();
		return writer().nullPtr(type.pointer(getGenerator()).getAllocation());
	}

	public final <F extends Func> F nullPtr(Signature<F> signature) {
		assert assertIncomplete();
		return signature.op(writer().nullPtr(
				getGenerator().getFunctions().allocate(signature)));
	}

	public final <O extends Op> O phi(CodeId id, O op) {
		assert assertIncomplete();
		return writer().phi(opId(id), op);
	}

	public final <O extends Op> O phi(CodeId id, O op1, O op2) {
		assert assertIncomplete();
		return writer().phi(opId(id), op1, op2);
	}

	public void returnVoid() {
		assert assertIncomplete();
		writer().returnVoid();
		complete();
	}

	@Override
	public abstract CodeWriter writer();

	public CodeId opId(CodeId id) {
		if (id != null) {
			return id;
		}
		return getId().setLocal(getGenerator().id().anonymous(++this.localSeq));
	}

	@Override
	public String toString() {
		return this.id.toString();
	}

	@Override
	protected final CondBlk choose(
			BoolOp condition,
			CodeId trueName,
			CodeId falseName) {
		assert assertIncomplete();
		return new CondBlk(this, condition, trueName, falseName);
	}

	CodeId nestedId(CodeId name) {
		if (name != null) {
			return getId().setLocal(name);
		}

		return getId().setLocal(getGenerator().id().anonymous(++this.blockSeq));
	}

}

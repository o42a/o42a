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
		if (created()) {
			return writer().head();
		}
		return this.head;
	}

	public final CodePos tail() {
		assert assertIncomplete();
		return writer().tail();
	}

	public final AllocationCode allocate() {
		return new AllocationCode(this, null, true);
	}

	public final AllocationCode allocate(String name) {
		return new AllocationCode(this, id(name), true);
	}

	public final AllocationCode allocate(CodeId name) {
		return new AllocationCode(this, name, true);
	}

	public final AllocationCode undisposable() {
		return new AllocationCode(this, null, false);
	}

	public final AllocationCode undisposable(String name) {
		return new AllocationCode(this, id(name), false);
	}

	public final AllocationCode undisposable(CodeId name) {
		return new AllocationCode(this, name, false);
	}

	public final Code addBlock(String name) {
		assert assertIncomplete();
		return new CodeBlock(this, getGenerator().id(name));
	}

	public final Code addBlock(CodeId name) {
		assert assertIncomplete();
		return new CodeBlock(this, name);
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

	public final <S extends StructOp<S>> S nullPtr(Type<S> type) {
		assert assertIncomplete();
		return writer().nullPtr(type.pointer(getGenerator()).getAllocation());
	}

	public final <F extends Func<F>> F nullPtr(Signature<F> signature) {
		assert assertIncomplete();
		return signature.op(writer().nullPtr(
				getGenerator().getFunctions().allocate(signature)));
	}

	public final <O extends Op> O phi(CodeId id, O op) {
		assert assertIncomplete();
		return writer().phi(id != null ? id : op.getId(), op);
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
	protected final CondCode choose(
			BoolOp condition,
			CodeId trueName,
			CodeId falseName) {
		assert assertIncomplete();
		return new CondCode(this, condition, trueName, falseName);
	}

	CodeId nestedId(CodeId name) {
		if (name != null) {
			return getId().setLocal(name);
		}

		return getId().setLocal(getGenerator().id().anonymous(++this.blockSeq));
	}

}

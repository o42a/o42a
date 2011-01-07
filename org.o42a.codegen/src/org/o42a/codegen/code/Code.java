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

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Type;
import org.o42a.codegen.debug.DebugCodeBase;


public abstract class Code extends DebugCodeBase {

	private final String name;
	private final Head head = new Head(this);

	Code(Code enclosing, String name) {
		super(enclosing);
		this.name = name;
	}

	Code(Generator generator, String name) {
		super(generator);
		this.name = name;
	}

	public final String getName() {
		return this.name;
	}

	public abstract boolean exists();

	public final CodePos head() {
		if (exists()) {
			return writer().head();
		}
		return this.head;
	}

	public final CodePos tail() {
		assertIncomplete();
		return writer().tail();
	}

	public final CodeBlk addBlock() {
		return addBlock(null);
	}

	public final CodeBlk addBlock(String name) {
		assertIncomplete();
		return new CodeBlk(this, name);
	}

	public final void go(CodePos pos) {
		assertIncomplete();
		writer().go(unwrapPos(pos));
	}

	public final Int32op int32(int value) {
		assertIncomplete();
		return writer().int32(value);
	}

	public final Int64op int64(long value) {
		assertIncomplete();
		return writer().int64(value);
	}

	public final Fp64op fp64(double value) {
		assertIncomplete();
		return writer().fp64(value);
	}

	public final BoolOp bool(boolean value) {
		assertIncomplete();
		return writer().bool(value);
	}

	public final AnyOp nullPtr() {
		assertIncomplete();
		return writer().nullPtr();
	}

	public final <O extends PtrOp> O nullPtr(Type<O> type) {
		assertIncomplete();
		return writer().nullPtr(type.getPointer().getAllocation());
	}

	public final <F extends Func> F nullPtr(Signature<F> signature) {
		assertIncomplete();
		return signature.op(writer().nullPtr(signature.allocate(backend())));
	}

	public final DataOp<AnyOp> allocatePtr() {
		assertIncomplete();
		return writer().allocatePtr();
	}

	public final DataOp<AnyOp> allocateNull() {

		final DataOp<AnyOp> result = allocatePtr();

		result.store(this, nullPtr());

		return result;
	}

	public final <O extends Op> O phi(O op) {
		assertIncomplete();
		return writer().phi(op);
	}

	public final <O extends Op> O phi(O op1, O op2) {
		assertIncomplete();
		return writer().phi(op1, op2);
	}

	public void returnVoid() {
		assertIncomplete();
		writer().returnVoid();
		complete();
	}

	@Override
	public abstract CodeWriter writer();

	@Override
	public String toString() {
		return this.name;
	}

	@Override
	protected void assertIncomplete() {
		super.assertIncomplete();
	}

	@Override
	protected final CondBlk choose(
			BoolOp condition,
			String trueName,
			String falseName) {
		assertIncomplete();
		return new CondBlk(this, condition, trueName, falseName);
	}

}

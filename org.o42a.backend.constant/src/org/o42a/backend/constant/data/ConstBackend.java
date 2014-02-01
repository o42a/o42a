/*
    Constant Handler Compiler Back-end
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.backend.constant.data;

import org.o42a.backend.constant.ConstGenerator;
import org.o42a.backend.constant.code.*;
import org.o42a.backend.constant.code.op.COp;
import org.o42a.backend.constant.code.signature.CSignature;
import org.o42a.backend.constant.data.func.CFAlloc;
import org.o42a.backend.constant.data.struct.CStruct;
import org.o42a.backend.constant.data.struct.CType;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.BlockWriter;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.Op;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Type;
import org.o42a.codegen.data.backend.FuncAllocation;


public class ConstBackend {

	public static CCode<?> cast(Code code) {
		return cast(code.writer());
	}

	public static CBlock<?> cast(Block code) {
		return cast(code.writer());
	}

	public static CCode<?> cast(CodeWriter writer) {
		return (CCode<?>) writer;
	}

	public static CBlock<?> cast(BlockWriter writer) {
		return (CBlock<?>) writer;
	}

	public static CCodePos cast(CodePos codePos) {
		return (CCodePos) codePos;
	}

	public static CodePos underlying(CodePos codePos) {
		return codePos != null ? cast(codePos).getUnderlying() : null;
	}

	@SuppressWarnings({
		"unchecked", "rawtypes"
	})
	public static <O extends Op> COp<O, ?> cast(O op) {
		if (op instanceof StructOp) {
			return cast((StructOp) op);
		}
		if (op instanceof Func) {
			return cast((Func) op);
		}
		return (COp<O, ?>) op;
	}

	public static <O extends Op> O underlying(O op) {
		return cast(op).backend().underlying();
	}

	public static <S extends StructOp<S>> CStruct<S> cast(StructOp<S> op) {
		return (CStruct<S>) op.writer();
	}

	public static <F extends Func<F>> CFAlloc<F> cast(
			FuncAllocation<F> funcAllocation) {
		return (CFAlloc<F>) funcAllocation;
	}

	public static <F extends Func<F>> CFunc<F> cast(Func<F> func) {
		return (CFunc<F>) func.caller();
	}

	private final ConstGenerator generator;
	private final ConstCodeBackend codeBackend;
	private final ConstDataAllocator dataAllocator;
	private final ConstDataWriter dataWriter;
	private final UnderlyingBackend underlyingBackend;

	public ConstBackend(
			ConstGenerator generator,
			UnderlyingBackend underlyingBackend) {
		this.generator = generator;
		this.underlyingBackend = underlyingBackend;
		this.codeBackend = new ConstCodeBackend(this);
		this.dataAllocator = new ConstDataAllocator(this);
		this.dataWriter = new ConstDataWriter(this);
	}

	public final ConstGenerator getGenerator() {
		return this.generator;
	}

	public final Generator getUnderlyingGenerator() {
		return getGenerator().getProxiedGenerator();
	}

	public final UnderlyingBackend getUnderlyingBackend() {
		return this.underlyingBackend;
	}

	public final ConstCodeBackend codeBackend() {
		return this.codeBackend;
	}

	public final ConstDataAllocator dataAllocator() {
		return this.dataAllocator;
	}

	public final ConstDataWriter dataWriter() {
		return this.dataWriter;
	}

	public final <S extends StructOp<S>> CType<S> underlying(Type<S> type) {

		final ContainerCDAlloc<S> alloc = (ContainerCDAlloc<S>) type.pointer(
				getGenerator()).getAllocation();

		return alloc.getUnderlyingInstance();
	}

	public final <F extends Func<F>> CSignature<F> underlying(
			Signature<F> signature) {
		return (CSignature<F>) signature.allocation(getGenerator());
	}

	public void close() {
		getUnderlyingGenerator().close();
	}

}

/*
    Constant Handler Compiler Back-end
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
package org.o42a.backend.constant.data;

import org.o42a.backend.constant.ConstGenerator;
import org.o42a.backend.constant.code.*;
import org.o42a.backend.constant.code.signature.CSignature;
import org.o42a.backend.constant.data.type.CType;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.backend.FuncAllocation;
import org.o42a.codegen.code.op.Op;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Type;


public class ConstBackend {

	public static CCode<?> cast(Code code) {
		return cast(code.writer());
	}

	public static CCode<?> cast(CodeWriter codeWriter) {
		return (CCode<?>) codeWriter;
	}

	public static Code underlying(Code code) {
		return cast(code).getUnderlying();
	}

	public static CodeWriter underlying(CodeWriter codeWriter) {
		return cast(codeWriter).getUnderlying().writer();
	}

	public static CCodePos cast(CodePos codePos) {
		return (CCodePos) codePos;
	}

	public static CodePos underlying(CodePos codePos) {
		return cast(codePos).getUnderlying();
	}

	@SuppressWarnings("unchecked")
	public static <O extends Op> COp<O> cast(O op) {
		return (COp<O>) op;
	}

	public static <O extends Op> O underlying(O op) {
		return cast(op).getUnderlying();
	}

	public static <F extends Func<F>> FuncCAlloc<F> cast(
			FuncAllocation<F> funcAllocation) {
		return (FuncCAlloc<F>) funcAllocation;
	}

	public static <F extends Func<F>> FuncAllocation<F> underlying(
			FuncAllocation<F> funcAllocation) {
		return cast(funcAllocation).getUnderlyingPtr().getAllocation();
	}

	public <O extends StructOp<O>> CType<O> underlying(Type<O> type) {
		// TODO unwrap underlying type
		return null;
	}

	public <F extends Func<F>> CSignature<F> underlying(
			Signature<F> signature) {
		return (CSignature<F>) signature.allocation(getGenerator());
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

	public void close() {
		// TODO close constant back-end
		getUnderlyingGenerator().close();
	}

}

/*
    Compiler Code Generator
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
package org.o42a.codegen.code;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Type;
import org.o42a.codegen.debug.DebugCodeBase;
import org.o42a.util.string.ID;


public abstract class Code extends DebugCodeBase {

	private final ID id;
	private OpNames opNames = new OpNames(this);

	public Code(Code enclosing, ID name) {
		super(enclosing);
		this.id = enclosing.opNames().nestedId(name);
	}

	public Code(Generator generator, ID id) {
		super(generator);
		this.id = id;
	}

	public Allocator getAllocator() {
		return getClosestAllocator().getAllocator();
	}

	public abstract Allocator getClosestAllocator();

	public abstract Block getBlock();

	public boolean contains(Code code) {

		Code c = code;

		do {
			if (this == c) {
				return true;
			}
			c = c.getEnclosing();
		} while (c != null);

		return false;
	}

	/**
	 * Retrieves all assets available at current execution point.
	 *
	 * <p>It is possible that some assets are not available at the moment of
	 * this method execution. It is possible that code transitions (e.g.
	 * {@link Block#go(CodePos)}) will bring more assets available at this
	 * execution point. So, it is advisable to perform actual assets checking
	 * only at the function is {@link Function#addCompleteListener(
	 * FunctionCompleteListener) fully built}.<p>
	 *
	 * @return available assets.
	 */
	public abstract CodeAssets assets();

	/**
	 * Adds asset to code.
	 *
	 * <p>This asset will be available after current execution point, but not
	 * before it.</p>
	 *
	 * @param assetType asset type.
	 * @param asset asset value.
	 *
	 * @return updated code assets.
	 */
	public final <A extends CodeAsset<A>> CodeAssets addAsset(
			Class<? extends A> assetType,
			A asset) {
		assert assetType != null:
			"Asset type not specified";
		assert asset != null :
			"Asset value not specified";
		updateAssets(assets().update(assetType, asset));
		return assets();
	}

	public final OpNames opNames() {
		return this.opNames;
	}

	public final void setOpNames(OpNames opNames) {
		this.opNames = opNames;
	}

	public final ID getId() {
		return this.id;
	}

	public final Code inset(String name) {
		return inset(ID.id(name));
	}

	public final Code inset(ID name) {
		assert assertIncomplete();

		final Inset inset = new Inset(this, name);

		updateAssets(new CodeAssets(inset, "inset", inset));

		return inset;
	}

	public final <O> OpMeans<O> means(
			java.util.function.Function<Code, O> createOp) {
		return means((ID) null, createOp);
	}

	public final <O> OpMeans<O> means(
			String id,
			java.util.function.Function<Code, O> createOp) {
		return means(ID.id(id), createOp);
	}

	public final <O> OpMeans<O> means(
			ID id,
			java.util.function.Function<Code, O> createOp) {
		return new InsetOpMeans<>(
				opId(id),
				inset(id),
				createOp);
	}

	public final Block addBlock(String name) {
		return addBlock(ID.id(name));
	}

	public final Block addBlock(ID name) {
		return new CodeBlock(this, name);
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

	public final <F extends Fn<F>> F nullPtr(Signature<F> signature) {
		assert assertIncomplete();
		return signature.op(writer().nullPtr(
				getGenerator().getFunctions().allocate(signature)));
	}

	public final <T> Allocated<T> allocate(
			ID id,
			Allocatable<T> allocatable) {
		assert assertIncomplete();

		final Allocator allocator;

		if (allocatable.getAllocationMode().isDebug()) {
			allocator = getClosestAllocator();
		} else {
			allocator = getAllocator();
		}

		final Allocated<T> allocated =
				allocator.addAllocation(opNames().nestedId(id), allocatable);

		allocated.init(this);

		return allocated;
	}

	public final <O extends Op> O phi(ID id, O op) {
		assert assertIncomplete();
		return writer().phi(id != null ? id : op.getId(), op);
	}

	public final <O extends Op> OpMeans<O> phiMeans(ID id, OpMeans<O> op) {
		assert assertIncomplete();
		return means(id, c -> c.phi(id, op.op()));
	}

	public final <O extends Op> O phi(ID id, O op1, O op2) {
		assert assertIncomplete();
		return writer().phi(opId(id), op1, op2);
	}

	public final <O extends Op> OpMeans<O> phiMeans(
			ID id,
			OpMeans<O> op1,
			OpMeans<O> op2) {
		assert assertIncomplete();
		return means(id, c -> c.phi(id, op1.op(), op2.op()));
	}

	public final <O extends Op> O phi(ID id, O[] ops) {
		assert assertIncomplete();
		if (ops.length == 1) {
			return phi(id, ops[0]);
		}
		return writer().phi(opId(id), ops);
	}

	/**
	 * An "acquire" memory barrier.
	 */
	public final void acquireBarrier() {
		assert assertIncomplete();
		writer().acquireBarrier();
	}

	/**
	 * A "release" memory barrier.
	 */
	public final void releaseBarrier() {
		assert assertIncomplete();
		writer().releaseBarrier();
	}

	/**
	 * Full memory barrier.
	 */
	public final void fullBarrier() {
		assert assertIncomplete();
		writer().fullBarrier();
	}

	public final ID opId(ID id) {
		return opNames().opId(id);
	}

	@Override
	public String toString() {
		return this.id.toString();
	}

	protected abstract void updateAssets(CodeAssets assets);

	protected void removeAllAssets() {
		updateAssets(new CodeAssets(this, "clean"));
	}

	private static final class InsetOpMeans<O> implements OpMeans<O> {

		private final ID id;
		private final Code inset;
		private final java.util.function.Function<Code, O> createOp;
		private O op;

		InsetOpMeans(
				ID id,
				Code inset,
				java.util.function.Function<Code, O> createOp) {
			this.id = id;
			this.inset = inset;
			this.createOp = createOp;
		}

		@Override
		public ID getId() {
			return this.id;
		}

		@Override
		public O op() {
			if (this.op != null) {
				return this.op;
			}
			return this.op = this.createOp.apply(this.inset);
		}

		@Override
		public String toString() {
			if (this.inset == null) {
				return super.toString();
			}
			return this.inset.toString();
		}

	}

}

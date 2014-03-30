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
		this.id = enclosing.getOpNames().nestedId(name);
	}

	public Code(Generator generator, ID id) {
		super(generator);
		this.id = id;
	}

	public abstract Allocator getAllocator();

	public abstract Block getBlock();

	/**
	 * Retrieves the assets available at the current execution point.
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
	 * @param asset asset value, or <code>null</code> to do nothing.
	 *
	 * @return updated code assets.
	 */
	public <A extends CodeAsset<A>> CodeAssets addAsset(
			Class<? extends A> assetType,
			A asset) {
		assert assetType != null:
			"Asset type not specified";
		if (asset == null) {
			return assets();
		}
		updateAssets(assets().update(assetType, asset));
		return assets();
	}

	/**
	 * Removes all assets of the given type.
	 *
	 * <p>No assets of the given type will be available after current execution
	 * point.</p>
	 *
	 * @param assetType asset type.
	 *
	 * @return updated code assets.
	 */
	public final <A extends CodeAsset<A>> CodeAssets removeAsset(
			Class<? extends A> assetType) {
		assert assetType != null:
			"Asset type not specified";
		updateAssets(assets().update(assetType, null));
		return assets();
	}

	public final OpNames getOpNames() {
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

		updateAssets(new CodeAssets(inset));

		return inset;
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

	public final <F extends Func<F>> F nullPtr(Signature<F> signature) {
		assert assertIncomplete();
		return signature.op(writer().nullPtr(
				getGenerator().getFunctions().allocate(signature)));
	}

	public final AnyRecOp allocatePtr(ID id) {
		assert assertIncomplete();
		return writer().allocatePtr(opId(id));
	}

	public final AnyRecOp allocateNull(ID id) {

		final AnyRecOp result = allocatePtr(id);

		result.store(this, nullPtr());

		return result;
	}

	public final <S extends StructOp<S>> S allocate(ID id, Type<S> type) {
		assert assertIncomplete();

		final S result = writer().allocateStruct(
				opId(id),
				type.data(getGenerator()).getPointer().getAllocation());

		result.allocated(this, null);

		return result;
	}

	public <S extends StructOp<S>> StructRecOp<S> allocatePtr(
			ID id,
			Type<S> type) {
		assert assertIncomplete();

		final StructRecOp<S> result = writer().allocatePtr(
				opId(id),
				type.data(getGenerator()).getPointer().getAllocation());

		result.allocated(this, null);

		return result;
	}

	public final <O extends Op> O phi(ID id, O op) {
		assert assertIncomplete();
		return writer().phi(id != null ? id : op.getId(), op);
	}

	public final <O extends Op> O phi(ID id, O op1, O op2) {
		assert assertIncomplete();
		return writer().phi(opId(id), op1, op2);
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
		return getOpNames().opId(id);
	}

	@Override
	public String toString() {
		return this.id.toString();
	}

	protected abstract void updateAssets(CodeAssets assets);

}

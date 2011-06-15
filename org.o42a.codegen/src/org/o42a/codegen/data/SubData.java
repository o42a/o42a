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
package org.o42a.codegen.data;

import static org.o42a.codegen.data.Struct.structContent;

import java.util.Iterator;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.backend.DataWriter;


public abstract class SubData<O extends StructOp>
		extends Data<O>
		implements Iterable<Data<?>> {

	private final Type<O> instance;
	private final DataChain data = new DataChain();
	private int size;

	SubData(Generator generator, CodeId id, Type<O> instance) {
		super(generator, id);
		this.instance = instance;
	}

	@Override
	public DataType getDataType() {
		return DataType.STRUCT;
	}

	@Override
	public final Type<O> getInstance() {
		return this.instance;
	}

	public final Int8rec addInt8(String name) {
		return add(new Int8rec(this, id(name), null));
	}

	public final Int8rec addInt8(String name, Content<Int8rec> content) {
		return add(new Int8rec(this, id(name), content));
	}

	public final Int16rec addInt16(String name) {
		return add(new Int16rec(this, id(name), null));
	}

	public final Int16rec addInt16(String name, Content<Int16rec> content) {
		return add(new Int16rec(this, id(name), content));
	}

	public final Int32rec addInt32(String name) {
		return add(new Int32rec(this, id(name), null));
	}

	public final Int32rec addInt32(String name, Content<Int32rec> content) {
		return add(new Int32rec(this, id(name), content));
	}

	public final Int64rec addInt64(String name) {
		return add(new Int64rec(this, id(name), null));
	}

	public final Int64rec addInt64(String name, Content<Int64rec> content) {
		return add(new Int64rec(this, id(name), content));
	}

	public final Fp32rec addFp32(String name) {
		return add(new Fp32rec(this, id(name), null));
	}

	public final Fp32rec addFp32(String name, Content<Fp32rec> content) {
		return add(new Fp32rec(this, id(name), content));
	}

	public final Fp64rec addFp64(String name) {
		return add(new Fp64rec(this, id(name), null));
	}

	public final Fp64rec addFp64(String name, Content<Fp64rec> content) {
		return add(new Fp64rec(this, id(name), content));
	}

	public final <F extends Func<F>> FuncRec<F> addFuncPtr(
			String name,
			Signature<F> signature) {
		return addFuncPtr(name, signature, null);
	}

	public final <F extends Func<F>> FuncRec<F> addFuncPtr(
			String name,
			Signature<F> signature,
			Content<FuncRec<F>> content) {
		return add(codePtrRecord(
				this,
				id(name),
				getGenerator().getFunctions().allocate(signature),
				content));
	}

	public final AnyPtrRec addPtr(String name) {
		return add(new AnyPtrRec(this, id(name), null));
	}

	public final AnyPtrRec addPtr(
			String name,
			Content<AnyPtrRec> content) {
		return add(new AnyPtrRec(this, id(name), content));
	}

	public final DataRec addDataPtr(String name) {
		return addDataPtr(name, null);
	}

	public final DataRec addDataPtr(
			String name,
			Content<DataRec> content) {
		return add(new DataRec(this, id(name), content));
	}

	public final <P extends StructOp> StructRec<P> addPtr(
			String name,
			Type<P> type) {
		return add(new StructRec<P>(this, id(name), type, null));
	}

	public final <P extends StructOp> StructRec<P> addPtr(
			String name,
			Type<P> type,
			Content<StructRec<P>> content) {
		return add(new StructRec<P>(
				this,
				id(name),
				type,
				content));
	}

	public final RelPtrRec addRelPtr(String name) {
		return add(new RelPtrRec(this, id(name), null));
	}

	public final RelPtrRec addRelPtr(String name, Content<RelPtrRec> content) {
		return add(new RelPtrRec(this, id(name), content));
	}

	public final <OP extends StructOp, T extends Type<OP>> T addInstance(
			CodeId name,
			T type) {
		return addInstance(name, type, null, null);
	}

	public final <OP extends StructOp, T extends Type<OP>> T addInstance(
			CodeId name,
			T type,
			Content<T> content) {
		return addInstance(name, type, null, content);
	}

	public final <OP extends StructOp, T extends Type<OP>> T addInstance(
			CodeId name,
			T type,
			T instance) {
		return addInstance(name, type, instance, null);
	}

	public final <OP extends StructOp, T extends Type<OP>> T addInstance(
			CodeId name,
			T type,
			T instance,
			Content<T> content) {
		instance = type.instantiate(this, name, instance, content);

		final SubData<?> data = instance.getTypeData();

		add(data);

		return instance;
	}

	public final <OP extends StructOp, T extends Struct<OP>> T addStruct(
			CodeId name,
			T type,
			T instance) {

		final Content<T> content = structContent();

		return addInstance(name, type, instance, content);
	}

	public final <S extends Struct<?>> S addStruct(CodeId name, S struct) {
		struct.setStruct(this, name);
		add(struct.getTypeData());
		return struct;
	}

	public final int size() {
		return this.size;
	}

	@Override
	public Iterator<Data<?>> iterator() {
		return this.data.iterator();
	}

	protected <D extends Data<?>> D add(D data) {
		data.allocateData();
		this.size++;
		return this.data.add(data);
	}

	final DataChain data() {
		return this.data;
	}

	final void writeIncluded(DataWriter writer) {

		Data<?> data = this.data.getFirst();

		while (data != null) {
			data.write(writer);
			data = data.getNext();
		}
	}

	private CodeId id(String name) {
		return getGenerator().id(name);
	}

}

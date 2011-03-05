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

import java.util.Iterator;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.backend.DataWriter;


public abstract class SubData<O extends PtrOp>
		extends Data<O>
		implements Iterable<Data<?>> {

	private final Type<O> type;
	private final DataChain data = new DataChain();
	private int size;

	SubData(CodeId id, Type<O> type) {
		super(id);
		this.type = type;
	}

	@Override
	public DataType getDataType() {
		return DataType.STRUCT;
	}

	public final Type<O> getType() {
		return this.type;
	}

	public final Int32rec addInt32(String name) {
		return add(new Int32rec(id(name), null));
	}

	public final Int32rec addInt32(String name, Content<Int32rec> content) {
		return add(new Int32rec(id(name), content));
	}

	public final Int64rec addInt64(String name) {
		return add(new Int64rec(id(name), null));
	}

	public final Int64rec addInt64(String name, Content<Int64rec> content) {
		return add(new Int64rec(id(name), content));
	}

	public final Fp64rec addFp64(String name) {
		return add(new Fp64rec(id(name), null));
	}

	public final Fp64rec addFp64(String name, Content<Fp64rec> content) {
		return add(new Fp64rec(id(name), content));
	}

	public final <F extends Func> CodeRec<F> addCodePtr(
			String name,
			Signature<F> signature) {

		final SignatureDataBase<F> sign = signature;

		return add(codePtrRecord(
				id(name),
				sign.allocate(getGenerator().codeBackend()),
				null));
	}

	public final <F extends Func> CodeRec<F> addCodePtr(
			String name,
			Signature<F> signature,
			Content<CodeRec<F>> content) {

		final SignatureDataBase<F> sign = signature;

		return add(codePtrRecord(
				id(name),
				sign.allocate(getGenerator().codeBackend()),
				content));
	}

	public final AnyPtrRec addPtr(String name) {
		return add(new AnyPtrRec(id(name), null));
	}

	public final AnyPtrRec addPtr(
			String name,
			Content<AnyPtrRec> content) {
		return add(new AnyPtrRec(id(name), content));
	}

	public final <P extends StructOp> StructPtrRec<P> addPtr(
			String name,
			Type<P> type) {
		return add(new StructPtrRec<P>(id(name), type, null));
	}

	public final <P extends StructOp> StructPtrRec<P> addPtr(
			String name,
			Type<P> type,
			Content<StructPtrRec<P>> content) {
		return add(new StructPtrRec<P>(id(name), type, content));
	}

	public final RelPtrRec addRelPtr(String name) {
		return add(new RelPtrRec(id(name), null));
	}

	public final RelPtrRec addRelPtr(String name, Content<RelPtrRec> content) {
		return add(new RelPtrRec(id(name), content));
	}

	public final <T extends Type<?>> T addInstance(CodeId name, T type) {
		return addInstance(name, type, null);
	}

	public final <T extends Type<?>> T addInstance(
			CodeId name,
			T type,
			Content<T> content) {

		@SuppressWarnings("unchecked")
		final T instance = (T) type.instantiate(name, content);
		final SubData<?> data = instance.getTypeData();

		add(data);

		return instance;
	}

	public final <S extends Struct<?>> S addStruct(CodeId name, S struct) {
		struct.setStruct(getGenerator(), name);
		add(struct.getTypeData());

		final Globals globals = getGenerator();

		globals.addType(struct.getTypeData());

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
		data.allocateData(getGenerator());
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
		return getId().setLocal(getGenerator().id(name));
	}

}

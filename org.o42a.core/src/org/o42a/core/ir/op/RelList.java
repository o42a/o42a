/*
    Compiler Core
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
package org.o42a.core.ir.op;

import java.util.ArrayList;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.*;


public abstract class RelList<T> implements Content<RelList.Type> {

	public static final Type REL_LIST_TYPE = new Type();

	private final ArrayList<T> content = new ArrayList<T>();
	private Ptr<?> firstItem;
	private Type instance;

	public RelList<T> allocate(SubData<?> data, String fieldName) {
		data.addInstance(
				data.getGenerator().id(fieldName),
				REL_LIST_TYPE,
				this);
		return this;
	}

	public final Type getInstance() {
		return this.instance;
	}

	public final void add(T item) {
		this.content.add(item);
	}

	public final void addAll(Iterable<? extends T> items) {
		for (T item : items) {
			add(item);
		}
	}

	public void allocateItems(SubData<?> data) {

		assert this.firstItem == null :
			this + " items already allocated: " + this.firstItem;

		for (int i = 0, s = this.content.size(); i < s; ++i) {

			final T itemContent = this.content.get(i);
			final Ptr<?> item = allocateItem(data, i, itemContent);

			if (this.firstItem == null) {
				this.firstItem = item;
			}
		}
	}

	@Override
	public void allocated(Type instance) {
		this.instance = instance;
	}

	@Override
	public void fill(Type instance) {

		final int size = this.content.size();

		instance.getSize().setValue(size);
		if (size == 0) {
			instance.getList().setNull();
			return;
		}

		assert this.firstItem != null :
			this + " items not allocated yet";

		instance.getList().setValue(this.firstItem.relativeTo(
				instance.data(instance.generator()).getPointer()));
	}

	@Override
	public String toString() {
		return "RelList" + this.content;
	}

	protected abstract Ptr<?> allocateItem(
			SubData<?> data,
			int index,
			T item);

	public static final class Op extends StructOp {

		private Op(StructWriter writer) {
			super(writer);
		}

		@Override
		public final Type getType() {
			return (Type) super.getType();
		}

		public final DataOp<RelOp> list(Code code) {
			return writer().relPtr(code, getType().getList());
		}

		public final DataOp<Int32op> size(Code code) {
			return writer().int32(code, getType().getSize());
		}

		@Override
		public final Op create(StructWriter writer) {
			return new Op(writer);
		}

	}

	public static final class Type extends org.o42a.codegen.data.Type<Op> {

		private RelPtrRec list;
		private Int32rec size;

		private Type() {
		}

		public final RelPtrRec getList() {
			return this.list;
		}

		public final Int32rec getSize() {
			return this.size;
		}

		@Override
		public final Op op(StructWriter writer) {
			return new Op(writer);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("RelList");
		}

		@Override
		protected void allocate(SubData<Op> data) {
			this.list = data.addRelPtr("list");
			this.size = data.addInt32("size");
		}

	}

}

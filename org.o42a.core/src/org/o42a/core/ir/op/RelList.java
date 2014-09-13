/*
    Compiler Core
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
package org.o42a.core.ir.op;

import java.util.ArrayList;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.*;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.util.string.ID;


public abstract class RelList<T> implements Content<RelList.Type> {

	public static final Type REL_LIST_TYPE = new Type();

	private final ArrayList<T> content = new ArrayList<>();
	private Ptr<?> firstItem;
	private Type instance;

	public RelList<T> allocate(SubData<?> data, String fieldName) {
		data.addNewInstance(ID.id(fieldName), REL_LIST_TYPE, this);
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

		instance.size().setConstant(true).setValue(size);
		if (size == 0) {
			instance.list().setConstant(true).setNull();
			return;
		}

		assert this.firstItem != null :
			this + " items not allocated yet";

		instance.list().setConstant(true).setValue(this.firstItem.relativeTo(
				instance.data(instance.getGenerator()).getPointer()));
	}

	@Override
	public String toString() {
		return "RelList" + this.content;
	}

	protected abstract Ptr<?> allocateItem(
			SubData<?> data,
			int index,
			T item);

	public static final class Op extends StructOp<Op> {

		private Op(StructWriter<Op> writer) {
			super(writer);
		}

		@Override
		public final Type getType() {
			return (Type) super.getType();
		}

		public final RelRecOp list(Code code) {
			return relPtr(null, code, getType().list());
		}

		public final Int32recOp size(Code code) {
			return int32(null, code, getType().size());
		}

		public final AnyOp loadList(Code code) {
			return toAny(null, code)
					.offset(null, code, list(code).load(null, code));
		}

	}

	public static final class Type extends org.o42a.codegen.data.Type<Op> {

		private RelRec list;
		private Int32rec size;

		private Type() {
			super(ID.rawId("o42a_rlist_t"));
		}

		public final RelRec list() {
			return this.list;
		}

		public final Int32rec size() {
			return this.size;
		}

		@Override
		public final Op op(StructWriter<Op> writer) {
			return new Op(writer);
		}

		@Override
		protected void allocate(SubData<Op> data) {
			this.list = data.addRelPtr("list");
			this.size = data.addInt32("size");
		}

		@Override
		protected DebugTypeInfo createTypeInfo() {
			return externalTypeInfo(0x042a0001);
		}

	}

}

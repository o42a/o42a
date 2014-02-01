/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.ir.gc;

import static org.o42a.core.ir.system.GCBlockPaddingSystemType.GC_BLOCK_PADDING_SYSTEM_TYPE;

import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.*;
import org.o42a.util.string.ID;


public final class GCBlockOp extends StructOp<GCBlockOp> {

	public static final Type GC_BLOCK_TYPE = new Type();
	public static final ID GC_BLOCK_ID = ID.id("gc_block");

	private GCBlockOp(StructWriter<GCBlockOp> writer) {
		super(writer);
	}

	public static final class Type
			extends org.o42a.codegen.data.Type<GCBlockOp> {

		private Int8rec lock;
		private Int8rec list;
		private Int16rec flags;
		private Int32rec useCount;
		private StructRec<GCDescOp> desc;
		private StructRec<GCBlockOp> prev;
		private StructRec<GCBlockOp> next;
		private Int32rec size;

		private Type() {
			super(ID.rawId("o42a_gc_block_t"));
		}

		@Override
		public boolean isDebuggable() {
			return false;
		}

		public final Int8rec lock() {
			return this.lock;
		}

		public final Int8rec list() {
			return this.list;
		}

		public final Int16rec flags() {
			return this.flags;
		}

		public final Int32rec useCount() {
			return this.useCount;
		}

		public final StructRec<GCDescOp> desc() {
			return this.desc;
		}

		public final StructRec<GCBlockOp> prev() {
			return this.prev;
		}

		public final StructRec<GCBlockOp> next() {
			return this.next;
		}

		public final Int32rec size() {
			return this.size;
		}

		@Override
		public GCBlockOp op(StructWriter<GCBlockOp> writer) {
			return null;
		}

		@Override
		protected void allocate(SubData<GCBlockOp> data) {
			this.lock = data.addInt8("lock");
			this.list = data.addInt8("list");
			this.flags = data.addInt16("flags");
			this.useCount = data.addInt32("use_count");
			this.desc = data.addPtr("desc", GCDescOp.GC_DESC_TYPE);
			this.prev = data.addPtr("prev", GC_BLOCK_TYPE);
			this.next = data.addPtr("next", GC_BLOCK_TYPE);
			this.size = data.addInt32("size");
			data.addSystem("padding", GC_BLOCK_PADDING_SYSTEM_TYPE);
		}

	}

}

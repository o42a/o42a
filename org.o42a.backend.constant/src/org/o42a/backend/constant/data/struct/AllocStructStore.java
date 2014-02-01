/*
    Constant Handler Compiler Back-end
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
package org.o42a.backend.constant.data.struct;

import org.o42a.analysis.use.SimpleUsage;
import org.o42a.analysis.use.Usable;
import org.o42a.backend.constant.code.op.*;
import org.o42a.backend.constant.code.rec.RecCOp;
import org.o42a.backend.constant.code.rec.RecStore;
import org.o42a.codegen.code.op.Op;
import org.o42a.codegen.data.*;


final class AllocStructStore extends StructStore {

	AllocStructStore(AllocPlace allocPlace) {
		super(allocPlace);
	}

	@Override
	public RecStore fieldStore(CStruct<?> struct, Rec<?, ?> field) {
		return new FieldStore(getAllocPlace(), struct);
	}

	@Override
	public SystemStore systemStore(CStruct<?> struct, SystemData field) {
		return new SystemFieldStore(getAllocPlace(), struct);
	}

	@Override
	public StructStore subStore(CStruct<?> struct, Type<?> field) {
		return new SubStore(this, struct);
	}

	@Override
	protected final Usable<SimpleUsage> init(
			CStruct<?> struct,
			Usable<SimpleUsage> allUses) {
		return allUses;
	}

	private static final class FieldStore extends RecStore {

		private final CStruct<?> enclosing;
		private final RecStore recStore;

		FieldStore(AllocPlace allocPlace, CStruct<?> struct) {
			super(allocPlace);
			this.enclosing = struct;
			this.recStore = allocRecStore(allocPlace);
		}

		@Override
		public <O extends Op> void store(
				InstrBE instr,
				RecCOp<?, O, ?> rec,
				OpBE<O> value) {
			this.recStore.store(instr, rec, value);
		}

		@Override
		public <O extends Op> void load(RecCOp<?, O, ?> rec, OpBE<O> value) {
			this.recStore.load(rec, value);
		}

		@Override
		protected Usable<SimpleUsage> init(
				RecCOp<?, ?, ?> rec,
				Usable<SimpleUsage> allUses) {
			this.enclosing.useBy(allUses);
			return init(this.recStore, rec, allUses);
		}

	}

	private static final class SystemFieldStore extends SystemStore {

		private final CStruct<?> enclosing;
		private final SystemStore systemStore;

		SystemFieldStore(AllocPlace allocPlace, CStruct<?> struct) {
			super(allocPlace);
			this.enclosing = struct;
			this.systemStore = allocSystemStore(allocPlace);
		}

		@Override
		protected Usable<SimpleUsage> init(
				SystemCOp op,
				Usable<SimpleUsage> allUses) {
			this.enclosing.useBy(allUses);
			return init(this.systemStore, op, allUses);
		}

	}

	private static final class SubStore extends StructStore {

		private final CStruct<?> enclosing;
		private final AllocStructStore structStore;

		SubStore(AllocStructStore allocStore, CStruct<?> enclosing) {
			super(allocStore.getAllocPlace());
			this.enclosing = enclosing;
			this.structStore = allocStore;
		}

		@Override
		public RecStore fieldStore(CStruct<?> struct, Rec<?, ?> field) {
			return this.structStore.fieldStore(struct, field);
		}

		@Override
		public SystemStore systemStore(CStruct<?> struct, SystemData field) {
			return this.structStore.systemStore(struct, field);
		}

		@Override
		public StructStore subStore(CStruct<?> struct, Type<?> field) {
			return this.structStore.subStore(struct, field);
		}

		@Override
		protected Usable<SimpleUsage> init(
				CStruct<?> struct,
				Usable<SimpleUsage> allUses) {
			struct.backend().use(this.enclosing);
			return allUses;
		}

	}

}

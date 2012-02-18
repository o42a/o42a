/*
    Constant Handler Compiler Back-end
    Copyright (C) 2012 Ruslan Lopatin

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
import org.o42a.backend.constant.code.op.InstrBE;
import org.o42a.backend.constant.code.op.OpBE;
import org.o42a.backend.constant.code.rec.RecCOp;
import org.o42a.backend.constant.code.rec.RecStore;
import org.o42a.codegen.code.op.Op;
import org.o42a.codegen.data.AllocClass;
import org.o42a.codegen.data.Rec;
import org.o42a.codegen.data.Type;


final class AllocStructStore extends StructStore {

	public AllocStructStore(AllocClass allocClass) {
		super(allocClass);
	}

	@Override
	public RecStore fieldStore(CStruct<?> struct, Rec<?, ?> field) {
		return new FieldStore(getAllocClass(), struct);
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

		FieldStore(AllocClass allocClass, CStruct<?> struct) {
			super(allocClass);
			this.enclosing = struct;
			this.recStore = allocRecStore(allocClass);
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

	private static final class SubStore extends StructStore {

		private final CStruct<?> enclosing;
		private final AllocStructStore structStore;

		SubStore(AllocStructStore allocStore, CStruct<?> enclosing) {
			super(allocStore.getAllocClass());
			this.enclosing = enclosing;
			this.structStore = allocStore;
		}

		@Override
		public RecStore fieldStore(CStruct<?> struct, Rec<?, ?> field) {
			return this.structStore.fieldStore(struct, field);
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

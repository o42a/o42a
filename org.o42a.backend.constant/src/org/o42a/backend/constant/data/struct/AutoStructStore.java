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

import static org.o42a.analysis.use.SimpleUsage.SIMPLE_USAGE;
import static org.o42a.analysis.use.SimpleUsage.simpleUsable;

import java.util.HashMap;

import org.o42a.analysis.use.SimpleUsage;
import org.o42a.analysis.use.Usable;
import org.o42a.backend.constant.code.op.*;
import org.o42a.backend.constant.code.rec.RecCOp;
import org.o42a.backend.constant.code.rec.RecStore;
import org.o42a.codegen.code.op.Op;
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.codegen.data.*;


public class AutoStructStore extends StructStore {

	private CStruct<?> struct;
	private HashMap<Ptr<?>, Usable<SimpleUsage>> storeUses;
	private Usable<SimpleUsage> allUses;

	AutoStructStore(AllocPlace allocPlace) {
		super(allocPlace);
	}

	@Override
	public RecStore fieldStore(CStruct<?> struct, Rec<?, ?> field) {
		assert this.struct == struct;
		return new FieldStore(this, field);
	}

	@Override
	public SystemStore systemStore(CStruct<?> struct, SystemData field) {
		assert this.struct == struct;
		return new SystemFieldScore(this, field);
	}

	@Override
	public StructStore subStore(CStruct<?> struct, Type<?> field) {
		return new SubStore(this);
	}

	@Override
	protected Usable<SimpleUsage> init(
			CStruct<?> struct,
			Usable<SimpleUsage> allUses) {
		this.struct = struct;
		this.allUses = allUses;

		final Usable<SimpleUsage> explicitUses =
				simpleUsable("PointerTo", struct);

		allUses.useBy(explicitUses, SIMPLE_USAGE);

		return explicitUses;
	}

	private final Usable<SimpleUsage> storeUses(
			Ptr<? extends PtrOp<?>> field) {
		if (this.storeUses == null) {

			final int size = this.struct.getType().size(
					this.struct.getBackend().getGenerator());

			this.storeUses = new HashMap<>(size);
		} else {

			final Usable<SimpleUsage> found = this.storeUses.get(field);

			if (found != null) {
				return found;
			}
		}

		final Usable<SimpleUsage> storeUses = simpleUsable("StoreOf", field);

		this.storeUses.put(field, storeUses);
		storeUses.useBy(this.struct.explicitUses(), SIMPLE_USAGE);

		return storeUses;
	}

	public static final class FieldStore extends RecStore {

		private final AutoStructStore enclosing;
		private final Rec<?, ?> field;
		private RecCOp<?, ?, ?> rec;
		private Usable<SimpleUsage> allUses;

		FieldStore(AutoStructStore enclosing, Rec<?, ?> field) {
			super(enclosing.getAllocPlace());
			this.enclosing = enclosing;
			this.field = field;
		}

		@Override
		public <O extends Op> void store(
				InstrBE instr,
				RecCOp<?, O, ?> rec,
				OpBE<O> value) {
			assert this.rec == rec;
			instr.useBy(storeUses());
			value.useBy(instr);
			this.allUses.useBy(instr, SIMPLE_USAGE);
		}

		@Override
		public <O extends Op> void load(RecCOp<?, O, ?> rec, OpBE<O> value) {
			assert this.rec == rec;
			this.allUses.useBy(value, SIMPLE_USAGE);
		}

		@Override
		protected Usable<SimpleUsage> init(
				RecCOp<?, ?, ?> rec,
				Usable<SimpleUsage> allUses) {
			this.rec = rec;
			this.allUses = allUses;
			storeUses().useBy(allUses, SIMPLE_USAGE);

			final Usable<SimpleUsage> explicitUses =
					simpleUsable("PointerTo", rec);

			allUses.useBy(explicitUses, SIMPLE_USAGE);
			this.enclosing.allUses.useBy(allUses, SIMPLE_USAGE);
			this.enclosing.struct.useBy(explicitUses);

			return explicitUses;
		}

		public final Usable<SimpleUsage> storeUses() {
			return this.enclosing.storeUses(this.field.getPointer());
		}

	}

	private final class SystemFieldScore extends SystemStore {

		private final AutoStructStore enclosing;
		private final SystemData field;

		SystemFieldScore(AutoStructStore enclosing, SystemData field) {
			super(enclosing.getAllocPlace());
			this.enclosing = enclosing;
			this.field = field;
		}

		@Override
		protected Usable<SimpleUsage> init(
				SystemCOp op,
				Usable<SimpleUsage> allUses) {
			storeUses().useBy(allUses, SIMPLE_USAGE);

			final Usable<SimpleUsage> explicitUses =
					simpleUsable("PointerTo", op);

			allUses.useBy(explicitUses, SIMPLE_USAGE);
			this.enclosing.allUses.useBy(allUses, SIMPLE_USAGE);
			this.enclosing.struct.useBy(explicitUses);

			return explicitUses;
		}

		public final Usable<SimpleUsage> storeUses() {
			return this.enclosing.storeUses(this.field.getPointer());
		}

	}

	private final class SubStore extends AutoStructStore {

		private final AutoStructStore enclosing;

		SubStore(AutoStructStore enclosing) {
			super(enclosing.getAllocPlace());
			this.enclosing = enclosing;
		}

		@Override
		protected Usable<SimpleUsage> init(
				CStruct<?> struct,
				Usable<SimpleUsage> allUses) {

			final Usable<SimpleUsage> explicitUses =
					super.init(struct, allUses);

			this.enclosing.allUses.useBy(allUses, SIMPLE_USAGE);
			this.enclosing.struct.useBy(explicitUses);
			explicitUses.useBy(
					this.enclosing.struct.explicitUses(),
					SIMPLE_USAGE);

			return explicitUses;
		}

	}

}

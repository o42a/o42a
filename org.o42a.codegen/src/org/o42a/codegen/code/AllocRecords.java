/*
    Compiler Code Generator
    Copyright (C) 2014 Ruslan Lopatin

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

import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Type;
import org.o42a.util.ArrayUtil;
import org.o42a.util.string.ID;


final class AllocRecords {

	private static final AllocRecord<?>[] NO_ALLOC_RECORDS =
			new AllocRecord[0];

	private AllocRecord<?>[] records = NO_ALLOC_RECORDS;

	public final int size() {
		return this.records.length;
	}

	public final AllocRecord<?> get(int index) {
		return this.records[index];
	}

	@SuppressWarnings("unchecked")
	public final <P extends AllocPtrOp<P>> AllocRecord<P> cast(int index) {
		return (AllocRecord<P>) get(index);
	}

	public AllocRecord<AnyRecOp> recordPtr(int index, ID id) {
		if (index < this.records.length) {
			return cast(index);
		}

		return record(new AllocRecord<AnyRecOp>(id, index) {
			@Override
			AnyRecOp load(Code code, AnyRecOp prealloc) {
				return prealloc.load(null, code).toRec(getId(), code);
			}
			@Override
			AnyRecOp allocate(Code code) {
				return code.writer().allocatePtr(getId());
			}
			@Override
			void combine(
					AllocationsMap map,
					AnyRecOp prealloc,
					AnyRecOp[] ptrs) {

				final AnyRecOp phi = map.phis().phi(getId(), ptrs);
				final Code code = map.entry();

				prealloc.store(code, phi.toAny(null, code));
			}
		});
	}

	public <S extends StructOp<S>> AllocRecord<StructRecOp<S>> recordPtr(
			int index,
			ID id,
			final Type<S> type) {
		if (index < this.records.length) {
			return cast(index);
		}

		return record(new AllocRecord<StructRecOp<S>>(id, index) {
			@Override
			StructRecOp<S> load(Code code, AnyRecOp prealloc) {
				return prealloc.load(null, code)
						.toRec(getId(), code, type);
			}
			@Override
			StructRecOp<S> allocate(Code code) {

				final StructRecOp<S> result = code.writer().allocatePtr(
						code.opId(getId()),
						type.data(code.getGenerator())
						.getPointer()
						.getAllocation());

				result.allocated(code, null);

				return result;
			}
			@Override
			void combine(
					AllocationsMap map,
					AnyRecOp prealloc,
					StructRecOp<S>[] ptrs) {

				final StructRecOp<S> phi = map.phis().phi(getId(), ptrs);
				final Code code = map.entry();

				prealloc.store(code, phi.toAny(null, code));
			}
		});
	}

	public <S extends StructOp<S>> AllocRecord<S> recordStruct(
			int index,
			ID id,
			final Type<S> type) {
		if (index < this.records.length) {
			return cast(index);
		}

		return record(new AllocRecord<S>(id, index) {
			@Override
			S load(Code code, AnyRecOp prealloc) {
				return prealloc.load(null, code).to(getId(), code, type);
			}
			@Override
			S allocate(Code code) {

				final S result = code.writer().allocateStruct(
						code.opId(getId()),
						type.data(code.getGenerator())
						.getPointer()
						.getAllocation());

				result.allocated(code, null);

				return result;
			}
			@Override
			void combine(AllocationsMap map, AnyRecOp prealloc, S[] ptrs) {

				final S phi = map.phis().phi(getId(), ptrs);
				final Code code = map.entry();

				prealloc.store(code, phi.toAny(null, code));
			}
		});
	}

	private <P extends AllocPtrOp<P>> AllocRecord<P> record(
			AllocRecord<P> record) {
		assert this.records.length == record.getIndex() :
			"Wrong record index: " + record.getIndex() + ", but "
			+ this.records.length + " expected";

		this.records = ArrayUtil.append(this.records, record);

		return record;
	}

	static abstract class AllocRecord<P extends AllocPtrOp<P>> {

		private final ID id;
		private final int index;

		AllocRecord(ID id, int index) {
			this.id = id;
			this.index = index;
		}

		public final ID getId() {
			return this.id;
		}

		public final int getIndex() {
			return this.index;
		}

		abstract P load(Code code, AnyRecOp prealloc);

		abstract P allocate(Code code);

		abstract void combine(AllocationsMap map, AnyRecOp prealloc, P[] ptrs);

	}

}

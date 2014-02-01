/*
    Constant Handler Compiler Back-end
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.backend.constant.code.rec;

import static org.o42a.backend.constant.code.rec.RecStore.allocRecStore;
import static org.o42a.backend.constant.data.ConstBackend.cast;
import static org.o42a.codegen.code.op.Atomicity.NOT_ATOMIC;

import org.o42a.analysis.use.SimpleUsage;
import org.o42a.analysis.use.Usable;
import org.o42a.backend.constant.code.CCode;
import org.o42a.backend.constant.code.CCodePart;
import org.o42a.backend.constant.code.op.*;
import org.o42a.backend.constant.data.rec.RecCDAlloc;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.Atomicity;
import org.o42a.codegen.code.op.Op;
import org.o42a.codegen.code.op.RecOp;
import org.o42a.codegen.data.Ptr;
import org.o42a.codegen.data.Rec;
import org.o42a.util.fn.Getter;
import org.o42a.util.string.ID;


public abstract class RecCOp<R extends RecOp<R, O>, O extends Op, T>
		extends AllocPtrCOp<R>
		implements RecOp<R, O> {

	private final RecStore store;
	private final Usable<SimpleUsage> explicitUses;

	public RecCOp(OpBE<R> backend, RecStore store) {
		this(backend, store, null);
	}

	public RecCOp(OpBE<R> backend, RecStore store, Ptr<R> constant) {
		super(backend, store != null ? store.getAllocPlace() : null, constant);
		this.store = store != null ? store : allocRecStore(getAllocPlace());
		this.explicitUses = this.store.init(this, allUses());
	}

	public final T getConstantValue() {
		if (!isConstant()) {
			return null;
		}

		getBackend().getGenerator().getGlobals().write();

		final RecCDAlloc<?, ?, T> alloc = getAllocation();
		final Rec<?, T> rec = alloc.getData();

		if (!rec.isConstant() || rec.isLowLevel()) {
			return null;
		}

		final Getter<T> value = alloc.getValue();

		return value != null ? value.get() : null;
	}

	public final RecStore store() {
		return this.store;
	}

	@Override
	public final O load(ID id, Code code) {
		return load(id, code, NOT_ATOMIC);
	}

	public final O load(
			final ID id,
			final Code code,
			final Atomicity atomicity) {
		assert getAllocPlace().ensureAccessibleFrom(code);

		final ID derefId = code.getOpNames().derefId(id, this);
		final CCode<?> ccode = cast(code);
		final T constant = getConstantValue();

		if (constant != null) {
			return loaded(
					new ConstBE<O, T>(derefId, ccode, constant) {
						@Override
						protected O write() {
							return underlyingConstant(part(), constant());
						}
					},
					constant);
		}

		return loaded(
				new OpBE<O>(derefId, ccode) {
					@Override
					public void prepare() {
						store().load(RecCOp.this, this);
					}
					@Override
					protected O write() {
						return loadUnderlying(this, atomicity);
					}
				},
				null);
	}

	@Override
	public final void store(Code code, O value) {
		store(code, value, NOT_ATOMIC);
	}

	public final void store(
			final Code code,
			final O value,
			final Atomicity atomicity) {
		assert getAllocPlace().ensureAccessibleFrom(code);

		final COp<O, ?> cValue = cast(value);

		new BaseInstrBE(cast(code)) {
			@Override
			public void prepare() {
				store().store(this, RecCOp.this, cValue.backend());
			}
			@Override
			public String toString() {
				return RecCOp.this + " = " + value;
			}
			@Override
			protected void emit() {
				storeUnderlying(this, cValue, atomicity);
			}
		};
	}

	@Override
	protected final Usable<SimpleUsage> explicitUses() {
		return this.explicitUses;
	}

	protected abstract O loaded(OpBE<O> backend, T constant);

	protected abstract O underlyingConstant(CCodePart<?> part, T constant);

	/**
	 * Performs the underlying load operation.
	 *
	 * @param be load operation back-end.
	 * @param atomicity operations atomicity.
	 *
	 * @return loaded underlying loaded value. Can be atomic only for atomic
	 * records.
	 */
	protected O loadUnderlying(OpBE<O> be, Atomicity atomicity) {
		return backend().underlying().load(
				be.getId(),
				be.part().underlying());
	}

	/**
	 * Performs the underlying store.
	 *
	 * @param be store instruction back-end.
	 * @param value value to store.
	 * @param atomicity operation atomicity. Can be atomic only for atomic
	 * records.
	 */
	protected void storeUnderlying(
			InstrBE be,
			COp<O, ?> value,
			Atomicity atomicity) {
		backend().underlying().store(
				be.part().underlying(),
				value.backend().underlying());
	}

	@SuppressWarnings("unchecked")
	private RecCDAlloc<?, ?, T> getAllocation() {
		return (RecCDAlloc<?, ?, T>) getConstant().getAllocation();
	}

}

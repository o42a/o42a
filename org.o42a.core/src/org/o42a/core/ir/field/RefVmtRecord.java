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
package org.o42a.core.ir.field;

import static org.o42a.util.fn.Init.init;

import org.o42a.codegen.Generator;
import org.o42a.codegen.data.Rec;
import org.o42a.codegen.data.RelPtr;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.object.vmt.VmtIR;
import org.o42a.core.ir.object.vmt.VmtIROp;
import org.o42a.core.ir.object.vmt.VmtRecord;
import org.o42a.util.fn.Init;
import org.o42a.util.string.ID;


public abstract class RefVmtRecord<
		F extends RefFld.Op<F>,
		T extends RefFld.Type<F>,
		P,
		R extends Rec<?, P>> implements VmtRecord {

	public static final ID CONSTRUCT_ID = ID.rawId("construct");
	public static final ID CLONE_ID = ID.rawId("clone");
	public static final ID FLD_CTR_ID = ID.id("fctr");

	private final RefFld<F, T, P, R> fld;
	private final Init<RefRecord<P>> content = init(this::buildContent);
	private R record;
	private boolean derived;

	public RefVmtRecord(RefFld<F, T, P, R> fld) {
		this.fld = fld;
	}

	public final Generator getGenerator() {
		return fld().getGenerator();
	}

	public RefFld<F, T, P, R> fld() {
		return this.fld;
	}

	public final P content() {
		return this.content.get().ptr();
	}

	@Override
	public boolean derive(VmtIR vmtIR) {
		if (!this.content.get().isDerived()) {
			return false;
		}

		@SuppressWarnings("unchecked")
		final RefFld<F, T, P, R> derivedFld =
				(RefFld<F, T, P, R>)
				vmtIR.getObjectIR().bodies().findFld(fld().getKey());

		if (derivedFld == null) {
			return false;
		}

		this.record = derivedFld.vmtRecord().record();
		this.derived = true;

		assert this.record != null :
			"Derived record for `" + this + "` is not allocated in VMT";

		return true;
	}

	@Override
	public final void allocateMethods(SubData<VmtIROp> vmt) {
		assert this.derived || this.record == null :
			"VMT record for `" + fld() + "` already allocated";
		this.record = allocateRecord(vmt);
		this.derived = false;
	}

	@Override
	public void fillMethods() {
		record().setConstant(true).setValue(this::content);
	}

	public final RelPtr recordOffset() {
		return record()
				.getPointer()
				.relativeTo(record().getEnclosing().pointer(getGenerator()));
	}

	protected abstract R allocateRecord(SubData<VmtIROp> vmt);

	protected final R record() {
		fld().getObjectIR().getVmtIR().allocate();
		assert this.record != null :
			"A record for `" + this + "` is not allocated in VMT";
		return this.record;
	}

	protected abstract P dummyContent();

	protected abstract P reuseContent();

	protected abstract P createContent();

	private RefRecord<P> buildContent() {
		assert !fld().getBodyIR().bodies().isTypeBodies() :
			"Can not build a VMT content for type field `" + fld() + "`";
		if (fld().isDummy()) {
			return new RefRecord<>(dummyContent(), true);
		}

		final P reusedContent = reuseContent();

		if (reusedContent != null) {
			return new RefRecord<>(reusedContent, true);
		}

		return new RefRecord<>(createContent(), false);
	}

	private static final class RefRecord<P> {

		private final P ptr;
		private final boolean derived;

		RefRecord(P ptr, boolean derived) {
			this.ptr = ptr;
			this.derived = derived;
		}

		public final P ptr() {
			return this.ptr;
		}

		public final boolean isDerived() {
			return this.derived;
		}

		@Override
		public String toString() {
			if (this.ptr == null) {
				return super.toString();
			}
			return this.ptr.toString();
		}

	}

}

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

import static org.o42a.codegen.code.op.Atomicity.ATOMIC;
import static org.o42a.core.ir.object.ObjectOp.anonymousObject;
import static org.o42a.core.ir.object.op.ObjHolder.useVar;
import static org.o42a.core.ir.object.vmt.VmtIR.VMT_ID;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CondBlock;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.object.op.ObjectFn;
import org.o42a.core.ir.object.vmt.VmtIRChain.Op;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.object.Obj;
import org.o42a.util.string.ID;


public abstract class RefFldOp<
		F extends RefFld.Op<F>,
		T extends RefFld.Type<F>,
		C extends ObjectFn<C>>
				extends FldOp<F, T> {

	public RefFldOp(ObjOp host, RefFld<F, T, C> fld, F ptr) {
		super(host, fld, ptr);
	}

	@SuppressWarnings("unchecked")
	@Override
	public RefFld<F, T, C> fld() {
		return (RefFld<F, T, C>) super.fld();
	}

	public ObjectOp target(CodeDirs dirs, ObjHolder holder) {

		final Block code = dirs.code();

		if (isOmitted()) {

			final Obj target = fld().getTarget();

			if (target.isNone()) {
				code.go(dirs.falseDir());
			}

			final ObjectIR targetIR = target.ir(getGenerator());

			return holder.hold(code, targetIR.op(getBuilder(), dirs.code()));
		}

		final FldKind kind = fld().getKind();

		code.dumpName(kind + " field: ", this);
		code.dumpName(kind + " host: ", host());

		return findTarget(dirs, holder);
	}

	protected abstract ObjectOp findTarget(CodeDirs dirs, ObjHolder holder);

	protected final ObjectOp loadOrConstructTarget(
			CodeDirs dirs,
			ObjHolder holder,
			boolean trappingConstructor) {

		final Block code = dirs.code();
		final DataRecOp objectRec = ptr().object(null, code);

		code.acquireBarrier();

		final DataOp existing;

		if (trappingConstructor) {
			existing = useVar(code, objectRec);
		} else {
			existing = objectRec.load(null, code, ATOMIC);
		}

		final CondBlock noTarget =
				existing.isNull(null, code)
				.branch(code, "no_target", "has_target");
		final Block hasTarget = noTarget.otherwise();

		isNone(hasTarget, existing).go(hasTarget, dirs.falseDir());

		final DataOp ptr1 = hasTarget.phi(null, existing);

		if (trappingConstructor) {
			holder.set(hasTarget, createObject(hasTarget, ptr1));
		}

		hasTarget.go(code.tail());

		final DataOp constructed = construct(noTarget);

		constructed.isNull(null, noTarget).go(noTarget, dirs.falseDir());

		final DataOp ptr2 = noTarget.phi(null, constructed);

		if (trappingConstructor) {
			// Object is trapped in variable constructor.
			// Add it to holder to unuse it automatically.
			holder.set(noTarget, createObject(noTarget, ptr2));
		}
		noTarget.go(code.tail());

		final ObjectOp target = createObject(code, code.phi(null, ptr1, ptr2));

		if (!trappingConstructor) {
			holder.hold(code, target);
		}

		return target;
	}

	protected final DataOp construct(Code code) {

		final Op vmtc = host().vmtc(code);
		final C constructor =
				vmtc.vmt(null, code)
				.load(null, code)
				.to(VMT_ID, code, fld().getObjectIR().getVmtIR())
				.func(null, code, fld().vmtConstructor())
				.load(null, code);

		code.dumpName("Constructor: ", constructor);
		code.dumpName("Host: ", host());

		return construct(code, constructor, vmtc);
	}

	protected abstract DataOp construct(Code code, C constructor, Op vmtc);

	/**
	 * Create an object by the given pointer.
	 *
	 * @param code code block to construct the object in.
	 * @param ptr object pointer.
	 *
	 * @return constructed object.
	 */
	protected ObjectOp createObject(Block code, DataOp ptr) {

		final Obj hostAscendant = host().getAscendant();
		final Obj targetType = fld().targetType(hostAscendant);

		return anonymousObject(getBuilder(), code, ptr, targetType);
	}

	private BoolOp isNone(Block hasTarget, DataOp ptr) {

		final ObjectIR noneIR = getContext().getNone().ir(getGenerator());
		final DataOp none = noneIR.ptr().toData().op(null, hasTarget);

		return ptr.eq(ID.id("is_none"), hasTarget, none);
	}

}

/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.CondBlock;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.object.op.ObjectFunc;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.Obj;
import org.o42a.util.string.ID;


public abstract class RefFldOp<
		S extends RefFld.Op<S, C>,
		C extends ObjectFunc<C>>
				extends MemberFldOp {

	public RefFldOp(RefFld<C> fld, ObjOp host) {
		super(fld, host);
	}

	@SuppressWarnings("unchecked")
	@Override
	public RefFld<C> fld() {
		return (RefFld<C>) super.fld();
	}

	@Override
	public abstract S ptr();

	@Override
	public final FldOp field(CodeDirs dirs, MemberKey memberKey) {
		return target(dirs, tempObjHolder(dirs.getAllocator()))
				.field(dirs, memberKey);
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

	@Override
	public final ObjectOp materialize(CodeDirs dirs, ObjHolder holder) {
		return target(dirs, holder);
	}

	protected ObjectOp findTarget(CodeDirs dirs, ObjHolder holder) {

		final Block code = dirs.code();
		final DataRecOp objectRec = ptr().object(null, code);

		code.acquireBarrier();

		final DataOp existing = objectRec.load(null, code, ATOMIC);
		final CondBlock noTarget =
				existing.isNull(null, code)
				.branch(code, "no_target", "has_target");
		final Block hasTarget = noTarget.otherwise();

		isNone(hasTarget, existing).go(hasTarget, dirs.falseDir());

		final DataOp ptr1 = hasTarget.phi(null, existing);

		if (fld().getKind().isVariable()) {
			holder.hold(hasTarget, createObject(hasTarget, ptr1));
		}

		hasTarget.go(code.tail());

		final DataOp constructed = ptr().construct(noTarget, host());

		constructed.isNull(null, noTarget).go(noTarget, dirs.falseDir());

		final DataOp ptr2 = noTarget.phi(null, constructed);

		if (fld().getKind().isVariable()) {
			// Object is trapped in variable constructor.
			// Add it to holder to unuse it automatically.
			holder.set(noTarget, createObject(noTarget, ptr2));
		}
		noTarget.go(code.tail());

		final ObjectOp target = createObject(code, code.phi(null, ptr1, ptr2));

		if (!fld().getKind().isVariable()) {
			holder.hold(code, target);
		}

		return target;
	}

	protected ObjectOp createObject(Block code, DataOp ptr) {

		final Obj hostAscendant = host().getAscendant();
		final Obj targetType = fld().targetType(hostAscendant);

		if (!fld().isLink() && host().getPrecision().isExact()) {

			final ObjectIRBodyOp targetBodyPtr = ptr.to(
					null,
					code,
					fld().getTargetAscendant()
					.ir(getGenerator()).getBodyType());

			return targetBodyPtr.op(
					getBuilder(),
					fld().getTargetAscendant(),
					ObjectPrecision.EXACT);
		}

		return anonymousObject(getBuilder(), ptr, targetType);
	}

	private BoolOp isNone(Block hasTarget, DataOp ptr) {

		final ObjectIR noneIR = getContext().getNone().ir(getGenerator());
		final Ptr<ObjectIRBodyOp> nonePtr =
				noneIR.getMainBodyIR().pointer(getGenerator());
		final DataOp none = nonePtr.toData().op(null, hasTarget);

		return ptr.eq(ID.id("is_none"), hasTarget, none);
	}

}

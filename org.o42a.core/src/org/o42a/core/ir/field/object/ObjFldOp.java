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
package org.o42a.core.ir.field.object;

import static org.o42a.core.ir.object.op.CtrOp.allocateCtr;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.OpMeans;
import org.o42a.codegen.data.Ptr;
import org.o42a.codegen.data.StructRec;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.field.RefFld;
import org.o42a.core.ir.field.RefFldOp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.CtrOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.object.vmt.VmtIRChain;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.HostValueOp;
import org.o42a.core.ir.op.OpPresets;
import org.o42a.core.member.MemberKey;


public class ObjFldOp<
		F extends RefFld.Op<F>,
		T extends RefFld.Type<F>> extends RefFldOp<
				F,
				T,
				Ptr<ObjFldConf.Op>,
				StructRec<ObjFldConf.Op>,
				ObjectConstructorFn> {

	ObjFldOp(ObjOp host, AbstractObjFld<F, T> fld, OpMeans<F> ptr) {
		super(host, fld, ptr);
	}

	private ObjFldOp(ObjFldOp<F, T> proto, OpPresets presets) {
		super(proto, presets);
	}

	@Override
	public final ObjFldOp<F, T> setPresets(OpPresets presets) {
		if (presets.is(getPresets())) {
			return this;
		}
		return new ObjFldOp<>(this, presets);
	}

	@Override
	public final AbstractObjFld<F, T> fld() {
		return (AbstractObjFld<F, T>) super.fld();
	}

	@Override
	public HostValueOp value() {
		return objectFldValueOp();
	}

	@Override
	public FldOp<?, ?> field(CodeDirs dirs, MemberKey memberKey) {
		return target(dirs, tempObjHolder(dirs.getAllocator()))
				.field(dirs, memberKey);
	}

	@Override
	public ObjectOp materialize(CodeDirs dirs, ObjHolder holder) {
		return target(dirs, holder);
	}

	@Override
	public ObjectOp dereference(CodeDirs dirs, ObjHolder holder) {
		return target(dirs, tempObjHolder(dirs.getAllocator()))
				.setPresets(getPresets())
				.dereference(dirs, holder);
	}

	@Override
	protected ObjectOp findTarget(CodeDirs dirs, ObjHolder holder) {
		if (!isStateless()) {
			return loadOrConstructTarget(dirs, holder, false);
		}
		return holder.holdVolatile(dirs.code(), constructTarget(dirs));
	}

	@Override
	protected DataOp construct(
			Code code,
			ObjectConstructorFn constructor,
			VmtIRChain.Op vmtc) {

		final CtrOp ctr = allocateCtr(getBuilder(), code);
		final CtrOp.Op ptr = ctr.ptr(code);

		ctr.fillOwner(code, host());
		ptr.object(code).store(code, code.nullDataPtr());

		return constructor.call(code, vmtc, ctr);
	}

	@Override
	protected ObjectConstructorFn constructor(Code code, VmtIRChain.Op vmtc) {
		return fld().conf().constructor().op(null, code);
	}

	@Override
	protected ObjectOp createObject(Block code, DataOp ptr) {
		if (!host().getPrecision().isExact()) {
			return super.createObject(code, ptr);
		}

		final ObjectIR ir = fld().getTargetAscendant().ir(getGenerator());

		return ir.compatibleOp(
				getBuilder(),
				code.means(c -> ptr.to(null, c, ir.getType())))
				.setPresets(getPresets());
	}

	private final ObjectOp constructTarget(CodeDirs dirs) {

		final Block code = dirs.code();
		final ObjOp host = host().setPresets(getPresets());
		final VmtIRChain.Op vmtc = host.vmtc(code);
		final ObjectConstructorFn constructor = constructor(code, vmtc);

		code.dumpName("Constructor: ", constructor);
		code.dumpName("Host: ", host);

		final CtrOp ctr = allocateCtr(getBuilder(), code);

		ctr.fillOwner(code, host)
		.sample(fld().getTarget())
		.allocateObject(dirs);

		final DataOp objectPtr = constructor.call(code, vmtc, ctr);
		final ObjectOp object = createObject(code, objectPtr);

		return object.setStackAllocated(ctr.isStackAllocated());
	}

}

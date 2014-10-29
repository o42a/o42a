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

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.util.fn.ReInit.reInit;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.OpMeans;
import org.o42a.codegen.data.Ptr;
import org.o42a.codegen.data.StructRec;
import org.o42a.core.ir.field.*;
import org.o42a.core.ir.field.RefFld.StatefulOp;
import org.o42a.core.ir.field.RefFld.StatefulType;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.object.vmt.VmtIR;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;
import org.o42a.core.object.type.Sample;
import org.o42a.util.fn.ReInit;


public class ObjFld extends RefFld<
		StatefulOp,
		StatefulType,
		Ptr<ObjFldConf.Op>,
		StructRec<ObjFldConf.Op>> {

	private final ReInit<ObjFldConf> conf = reInit(
			this::reuseConf,
			() -> new ObjFldConf(this));

	public ObjFld(
			ObjectIRBody bodyIR,
			Field field,
			boolean dummy) {
		super(bodyIR, field, dummy, field.toObject(), field.toObject());
	}

	@Override
	public final FldKind getKind() {
		return FldKind.OBJ;
	}

	public final ObjFldConf conf() {
		return this.conf.get();
	}

	@Override
	protected StatefulType getType() {
		return STATEFUL_FLD;
	}

	@Override
	protected Obj targetType(Obj bodyType) {
		return bodyType.member(getField().getKey())
				.toField()
				.object(dummyUser());
	}

	@Override
	protected ObjVmtRecord createVmtRecord() {
		return new ObjVmtRecord(this);
	}

	@Override
	protected ObjFldOp op(
			Code code,
			ObjOp host,
			OpMeans<RefFld.StatefulOp> ptr) {
		return new ObjFldOp(host, this, ptr);
	}

	final ObjectValueIR targetValueIR() {

		final Obj target = getTarget();
		final ObjectIR targetIR = target.ir(getGenerator());

		if (target.value().getDefinitions().areInherited()) {
			return null;
		}

		return targetIR.getObjectValueIR();
	}

	final VmtIR targetVmtIR() {
		return getTarget().ir(getGenerator()).getVmtIR();
	}

	private ObjFldConf reuseConf() {
		if (!getField().isOverride()) {
			return null;
		}

		final Sample sample = getObjectIR().getObject().type().getSample();

		if (sample != null) {

			final ObjFldConf reused = reuseConfFrom(sample.getObject());

			if (reused != null) {
				return reused;
			}
		}

		final Obj ancestor = getObjectIR().getAncestor();

		if (ancestor == null) {
			return null;
		}

		return reuseConfFrom(ancestor);
	}

	private ObjFldConf reuseConfFrom(Obj ascendant) {

		final ObjectIR ascendantIR = ascendant.ir(getGenerator());
		final ObjFld src = (ObjFld) ascendantIR.bodies().findFld(getKey());

		if (src == null) {
			// No such field in ascendant.
			return null;
		}
		if (!getField().isUpdated()) {
			return src.conf();
		}

		final VmtIR targetVmtIR = targetVmtIR();

		if (targetVmtIR != src.targetVmtIR()) {
			// Source field has different VMT. Can not reuse it.
			return null;
		}

		final ObjectValueIR targetValueIR = targetValueIR();
		final ObjectValueIR srcTargetValueIR = src.targetValueIR();

		if (targetValueIR == null) {
			if (srcTargetValueIR != null) {
				// Source target value is inherited,
				// but the one of this field is not.
				return null;
			}
			// Both targets are inherited.
			return src.conf();
		}
		if (srcTargetValueIR == null) {
			// This field's target value is inherited,
			// but the one of source field is not.
			return null;
		}
		if (targetValueIR.getOrigin() == srcTargetValueIR.getOrigin()) {
			// Target values have the same origin, i.e. they are the same.
			return src.conf();
		}

		final ObjectValueIRKind kind = targetValueIR.getKind();

		if (!kind.isPredefined()) {
			// Target value is not predefined.
			return null;
		}
		if (kind == srcTargetValueIR.getKind()) {
			// Targets have the same predefined values.
			return src.conf();
		}

		return null;
	}

}

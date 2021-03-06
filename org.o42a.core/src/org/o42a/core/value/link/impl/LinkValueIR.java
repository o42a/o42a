/*
    Compiler Core
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
package org.o42a.core.value.link.impl;

import static org.o42a.codegen.code.op.Atomicity.ATOMIC;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;
import static org.o42a.core.ir.value.Val.INDEFINITE_VAL;
import static org.o42a.core.ir.value.Val.VAL_CONDITION;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.object.ObjectDataIR;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.OpPresets;
import org.o42a.core.ir.value.Val;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.type.*;
import org.o42a.core.object.Obj;
import org.o42a.core.value.Value;
import org.o42a.core.value.link.KnownLink;
import org.o42a.core.value.link.LinkValueType;


final class LinkValueIR extends ValueIR {

	LinkValueIR(LinkValueTypeIR valueStructIR, ObjectIR objectIR) {
		super(valueStructIR, objectIR);
	}

	@Override
	public OpPresets valuePresets(OpPresets presets) {
		return presets.setStackAllocationAllowed(false);
	}

	@Override
	public <H extends ObjectOp> ValueOp<H> op(H object) {
		if (!getValueType().isVariable()) {
			return new LinkValueOp<>(this, object);
		}
		return new VarValueOp<>(this, object);
	}

	@Override
	public Val initialValue(ObjectDataIR dataIR) {

		final Obj object = dataIR.getObjectIR().getObject();
		final Value<?> value = object.value().getValue();

		final LinkValueType linkType = getValueType().toLinkType();
		final KnownLink link =
				linkType.cast(value).getCompilerValue();
		final Obj target = link.getTarget().getWrapped();

		if (target.getConstructionMode().isRuntime()) {
			return INDEFINITE_VAL;
		}

		return new Val(
				getValueType(),
				VAL_CONDITION,
				0,
				target.ir(getGenerator()).ptr().toAny());
	}

	private static final class LinkValueOp<H extends ObjectOp>
			extends DefaultValueOp<H> {

		LinkValueOp(LinkValueIR variableIR, H object) {
			super(variableIR, object);
		}

		@Override
		public StateOp<H> state() {
			return new LinkStateOp<>(object());
		}

	}

	private static final class VarValueOp<H extends ObjectOp>
			extends StatefulValueOp<H> {

		VarValueOp(ValueIR valueIR, H object) {
			super(valueIR, object);
		}

		@Override
		public StateOp<H> state() {
			return new VarStateOp<>(object());
		}

	}

	private static abstract class AbstractLinkStateOp<H extends ObjectOp>
			extends StateOp<H> {

		AbstractLinkStateOp(H host) {
			super(host);
		}

		@Override
		public void init(Block code, ValOp value) {

			final DataOp target =
					value.value(null, code)
					.toDataRec(null, code)
					.load(null, code);

			value().rawValue(null, code)
					.toAny(null, code)
					.toDataRec(null, code)
					.store(code, target, ATOMIC);

			code.releaseBarrier();

			flags().store(code, VAL_CONDITION);
		}

	}

	private static final class LinkStateOp<H extends ObjectOp>
			extends AbstractLinkStateOp<H> {

		LinkStateOp(H host) {
			super(host);
		}

		@Override
		public void assign(CodeDirs dirs, ObjectOp value) {
			throw new UnsupportedOperationException();
		}

	}

	private static final class VarStateOp<H extends ObjectOp>
			extends AbstractLinkStateOp<H> {

		VarStateOp(H host) {
			super(host);
		}

		@Override
		public void assign(CodeDirs dirs, ObjectOp value) {

			final Block code = dirs.code();

			start(code);
			tempObjHolder(code.getAllocator()).holdVolatile(code, host());

			final ObjectOp valueObject =
					value.materialize(dirs, tempObjHolder(code.getAllocator()));

			value().rawValue(null, code)
					.toAny(null, code)
					.toDataRec(null, code)
					.store(
							code,
							valueObject.toData(null, code),
							ATOMIC);
			code.releaseBarrier();

			flags().store(code, VAL_CONDITION);
			code.dump("Assigned: ", value());
		}

	}

}

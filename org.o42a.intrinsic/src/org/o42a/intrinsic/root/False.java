/*
    Intrinsics
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.intrinsic.root;

import static org.o42a.core.ir.op.Val.FALSE_VAL;
import static org.o42a.core.ir.op.ValOp.VAL_TYPE;
import static org.o42a.core.member.MemberId.memberName;
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;
import static org.o42a.core.value.Value.falseValue;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.data.Global;
import org.o42a.codegen.data.Ptr;
import org.o42a.common.ir.BuiltinValueIR;
import org.o42a.common.object.IntrinsicBuiltin;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjValOp;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public final class False extends IntrinsicBuiltin {

	public False(Root root) {
		super(
				root.toMemberOwner(),
				fieldDeclaration(root, root.distribute(), memberName("false")));
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {
		return falseValue();
	}

	@Override
	public void resolveBuiltin(Obj object) {
	}

	@Override
	public void writeBuiltin(Code code, ValOp result, HostOp host) {
		result.storeFalse(code);
	}

	@Override
	public String toString() {
		return "false";
	}

	@Override
	protected Ascendants createAscendants() {
		return new Ascendants(this).setAncestor(
				ValueType.VOID.typeRef(
						this,
						getScope().getEnclosingScope()));
	}

	@Override
	protected BuiltinValueIR createValueIR(ObjectIR objectIR) {
		return new ValueIR(this, objectIR);
	}

	private static final class ValueIR
			extends BuiltinValueIR
			implements ObjValOp {

		private Ptr<ValOp> falsePtr;

		ValueIR(False builtin, ObjectIR objectIR) {
			super(builtin, objectIR);
		}

		@Override
		public void writeLogicalValue(CodeDirs dirs) {
			dirs.goWhenFalse(dirs.code());
		}

		@Override
		public ValOp writeValue(Code code) {
			return falseVal(code);
		}

		@Override
		public ValOp writeValue(CodeDirs dirs) {

			final Code code = dirs.code();
			final ValOp result = falseVal(code);

			dirs.goWhenFalse(code);

			return result;
		}

		@Override
		public ValOp writeValue(CodeDirs dirs, ValOp result) {

			final Code code = dirs.code();

			result.storeFalse(code);
			dirs.goWhenFalse(code);

			return result;
		}

		@Override
		public ObjValOp op(CodeBuilder builder, Code code) {
			return this;
		}

		private final ValOp falseVal(Code code) {
			return falsePtr().op(code.id("FALSE"), code);
		}

		private final Ptr<ValOp> falsePtr() {
			if (this.falsePtr != null) {
				return this.falsePtr;
			}

			final Global<ValOp, ValOp.Type> instance =
				getGenerator().newGlobal().setConstant().newInstance(
						getGenerator().topId().sub("FALSE"),
						VAL_TYPE,
						FALSE_VAL);

			return this.falsePtr = instance.getPointer();
		}

	}

}

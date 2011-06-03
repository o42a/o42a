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

import static org.o42a.core.member.MemberId.memberName;
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;
import static org.o42a.core.value.Value.falseValue;

import org.o42a.codegen.code.Code;
import org.o42a.common.ir.BuiltinValueIR;
import org.o42a.common.object.IntrinsicBuiltin;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjValOp;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
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
	public ValOp writeBuiltin(ValDirs dirs, HostOp host) {
		dirs.dirs().goWhenFalse(dirs.code());
		return falseValue().op(dirs.code());
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

		ValueIR(False builtin, ObjectIR objectIR) {
			super(builtin, objectIR);
		}

		@Override
		public void writeLogicalValue(CodeDirs dirs) {
			dirs.goWhenFalse(dirs.code());
		}

		@Override
		public ValOp writeValue(ValDirs dirs) {
			dirs.dirs().goWhenFalse(dirs.code());
			return falseValue().op(dirs.code());
		}

		@Override
		public ObjValOp op(CodeBuilder builder, Code code) {
			return this;
		}

	}

}

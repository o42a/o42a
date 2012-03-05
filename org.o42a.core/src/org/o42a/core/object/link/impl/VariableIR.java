/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.object.link.impl;

import static org.o42a.core.ir.field.variable.AssignerFld.assignerKey;

import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.field.variable.AssignerFld;
import org.o42a.core.ir.field.variable.AssignerFldOp;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.struct.ValueIR;
import org.o42a.core.ir.value.struct.ValueOp;
import org.o42a.core.object.link.LinkValueStruct;


final class VariableIR extends ValueIR<VariableIR.Op> {

	private ObjectBodyIR bodyIR;
	private AssignerFld fld;

	VariableIR(LinkValueStruct valueStruct, ObjectIR objectIR) {
		super(valueStruct, objectIR);
	}

	public final ObjectBodyIR getBodyIR() {
		return this.bodyIR;
	}

	@Override
	public Fld allocateBody(ObjectBodyIR bodyIR, SubData<?> data) {
		this.bodyIR = bodyIR;
		this.fld = new AssignerFld(bodyIR);
		this.fld.declare(data);
		return this.fld;
	}

	@Override
	public void allocateMethods(ObjectMethodsIR methodsIR, SubData<?> data) {
	}

	@Override
	public Op op(ObjectOp object) {
		return new Op(defaultOp(object));
	}

	static final class Op extends ValueOp {

		private final ValueOp value;

		Op(ValueOp defaultOp) {
			super(defaultOp.getValueIR(), defaultOp.object());
			this.value = defaultOp;
		}

		@Override
		public ValOp writeValue(ValDirs dirs, ObjectOp body) {
			return this.value.writeValue(dirs, body);
		}

		@Override
		public void writeRequirement(CodeDirs dirs, ObjectOp body) {
			this.value.writeRequirement(dirs, body);
		}

		@Override
		public ValOp writeClaim(ValDirs dirs, ObjectOp body) {
			return this.value.writeClaim(dirs, body);
		}

		@Override
		public void writeCondition(CodeDirs dirs, ObjOp body) {
			this.value.writeCondition(dirs, body);
		}

		@Override
		public ValOp writeProposition(ValDirs dirs, ObjectOp body) {
			return this.value.writeProposition(dirs, body);
		}

		@Override
		public void assign(CodeDirs dirs, ObjectOp value) {

			final AssignerFldOp fld = (AssignerFldOp) object().field(
					dirs,
					assignerKey(getBuilder().getContext()));

			fld.assign(dirs, value);
		}

	}

}

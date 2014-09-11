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
package org.o42a.core.ir.object;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.object.value.ObjectCondFnIR;
import org.o42a.core.ir.object.value.ObjectDefFnIR;
import org.o42a.core.ir.object.value.ObjectValueFnIR;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.value.type.ValueOp;
import org.o42a.core.object.Obj;


public class ObjectValueIR {

	private final ObjectIR objectIR;
	private final ObjectValueFnIR value;
	private final ObjectCondFnIR condition;
	private final ObjectDefFnIR def;

	protected ObjectValueIR(ObjectIR objectIR) {
		this.objectIR = objectIR;
		this.value = new ObjectValueFnIR(this);
		this.condition = new ObjectCondFnIR(this);
		this.def = new ObjectDefFnIR(this);
	}

	public final Generator getGenerator() {
		return getObjectIR().getGenerator();
	}

	public final Obj getObject() {
		return getObjectIR().getObject();
	}

	public final ObjectIR getObjectIR() {
		return this.objectIR;
	}

	public final ObjectValueFnIR value() {
		return this.value;
	}

	public final ObjectCondFnIR condition() {
		return this.condition;
	}

	public final ObjectDefFnIR def() {
		return this.def;
	}

	public final ValueOp op(CodeBuilder builder, Code code) {
		return getObjectIR().op(builder, code).value();
	}

	public final void writeValue(DefDirs dirs, ObjOp host) {
		value().call(dirs, host);
	}

	public final void writeCondition(CodeDirs dirs, ObjOp host) {
		condition().call(dirs, host);
	}

	public final void writeDef(DefDirs dirs, ObjOp host) {
		def().call(dirs, host);
	}

	@Override
	public String toString() {
		return this.objectIR + " Value IR";
	}

	protected void allocate(ObjectDataIR dataIR) {
		value().allocate(dataIR);
		condition().allocate(dataIR);
		def().allocate(dataIR);
	}

}

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
import org.o42a.core.ir.object.value.*;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.value.type.ValueOp;
import org.o42a.core.object.Obj;


public class ObjectValueIR {

	private final ObjectIR objectIR;
	private final ObjectValueFnIR value;
	private final ObjectCondFnIR condition;
	private final ObjectClaimFnIR claim;
	private final ObjectPropositionFnIR proposition;

	protected ObjectValueIR(ObjectIR objectIR) {
		this.objectIR = objectIR;
		this.value = new ObjectValueFnIR(this);
		this.condition = new ObjectCondFnIR(this);
		this.claim = new ObjectClaimFnIR(this);
		this.proposition = new ObjectPropositionFnIR(this);
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

	public final ObjectClaimFnIR claim() {
		return this.claim;
	}

	public final ObjectPropositionFnIR proposition() {
		return this.proposition;
	}

	public final ObjectValuePartFnIR value(boolean claim) {
		return claim ? claim() : proposition();
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

	public final void writeClaim(DefDirs dirs, ObjOp host, ObjectOp body) {
		claim().call(dirs, host, body);
	}

	public final void writeProposition(
			DefDirs dirs,
			ObjOp host,
			ObjectOp body) {
		proposition().call(dirs, host, body);
	}

	@Override
	public String toString() {
		return this.objectIR + " Value IR";
	}

	protected void allocate(ObjectTypeIR typeIR) {
		value().allocate(typeIR);
		condition().allocate(typeIR);
		claim().allocate(typeIR);
		proposition().allocate(typeIR);
	}

}

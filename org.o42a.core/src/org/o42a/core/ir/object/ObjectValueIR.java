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
package org.o42a.core.ir.object;

import static org.o42a.core.ir.value.Val.FALSE_VAL;
import static org.o42a.core.ir.value.Val.INDEFINITE_VAL;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.object.impl.ObjectIRLocals;
import org.o42a.core.ir.object.impl.value.*;
import org.o42a.core.ir.value.Val;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.ir.value.struct.ValueOp;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.Value;


public class ObjectValueIR {

	private final ObjectIR objectIR;
	private final ObjectIRLocals locals;
	private final ObjectValueFunc value;
	private final ObjectClaimFunc claim;
	private final ObjectPropositionFunc proposition;

	protected ObjectValueIR(ObjectIR objectIR) {
		this.objectIR = objectIR;
		this.locals = new ObjectIRLocals(this);
		this.value = new ObjectValueFunc(this);
		this.claim = new ObjectClaimFunc(this);
		this.proposition = new ObjectPropositionFunc(this);
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

	public final ObjectClaimFunc claim() {
		return this.claim;
	}

	public final ObjectPropositionFunc proposition() {
		return this.proposition;
	}

	public final ObjectValueIRFunc value(boolean claim) {
		return claim ? claim() : proposition();
	}

	public final ValueOp op(CodeBuilder builder, Code code) {
		return getObjectIR().op(builder, code).value();
	}

	public final void writeValue(DefDirs dirs, ObjOp host, ObjectOp body) {
		this.value.call(dirs, host, body);
	}

	public final void writeClaim(DefDirs dirs, ObjOp host, ObjectOp body) {
		this.claim.call(dirs, host, body);
	}

	public final void writeProposition(
			DefDirs dirs,
			ObjOp host,
			ObjectOp body) {
		this.proposition.call(dirs, host, body);
	}

	@Override
	public String toString() {
		return this.objectIR + " Value IR";
	}

	protected void allocate(ObjectTypeIR typeIR) {
		this.value.allocate(typeIR);
		this.claim.allocate(typeIR);
		this.proposition.allocate(typeIR);
	}

	protected void fill(ObjectTypeIR typeIR) {

		final Val initialValue = initialValue();
		final ValType value = typeIR.getObjectData().value();

		value.setConstant(!initialValue.isIndefinite());
		value.set(initialValue);
	}

	final ObjectIRLocals getLocals() {
		return this.locals;
	}

	private final Definitions definitions() {
		return getObject().value().getDefinitions();
	}

	private Val initialValue() {

		final Definitions definitions = definitions();
		final Resolver resolver = definitions.getScope().dummyResolver();
		final Value<?> value = definitions.value(resolver);

		switch (value.getKnowledge().getCondition()) {
		case TRUE:
			if (!value.getKnowledge().isInitiallyKnown()) {
				return INDEFINITE_VAL;
			}
			return value.val(getGenerator());
		case RUNTIME:
			return INDEFINITE_VAL;
		case FALSE:
			return FALSE_VAL;
		}

		throw new IllegalStateException("Unsupported value: " + value);
	}

}

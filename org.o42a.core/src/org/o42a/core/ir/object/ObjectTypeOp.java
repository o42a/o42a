/*
    Compiler Core
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
package org.o42a.core.ir.object;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.op.*;


public class ObjectTypeOp extends IROp {

	private final ObjectPrecision precision;

	ObjectTypeOp(
			CodeBuilder builder,
			ObjectType.Op ptr,
			ObjectPrecision precision) {
		super(builder, ptr);
		this.precision = precision;
	}

	public final ObjectPrecision getPrecision() {
		return this.precision;
	}

	@Override
	public final ObjectType.Op ptr() {
		return (ObjectType.Op) super.ptr();
	}

	public final ObjectOp object(Code code, Obj wellKnownType) {
		return new AnonymousObjOp(this, mainBody(code), wellKnownType);
	}

	public final AnyOp start(Code code) {
		return ptr().data(code).startPtr(code);
	}

	public final ObjOp objectOfType(Code code, Obj type) {
		return mainBody(code).to(
				code,
				type.ir(getGenerator()).getBodyType())
				.op(this, type);
	}

	final void writeValue(Code code, ValOp result, ObjectOp body) {

		final ObjectValFunc function =
			ptr().data(code).valueFunc(code).load(code);

		function.call(code, result, body(code, body));
		code.dumpValue("Value", result);
	}

	final void writeRequirement(Code code, CodePos exit, ObjectOp body) {

		final ObjectCondFunc function =
			ptr().data(code).requirementFunc(code).load(code);

		function.call(code, body(code, body)).go(code, code.tail(), exit);
	}

	final void writeClaim(Code code, ValOp result, ObjectOp body) {

		final ObjectValFunc function =
			ptr().data(code).claimFunc(code).load(code);

		function.call(code, result, body(code, body));
	}

	final void writeCondition(
			Code code,
			CodePos exit,
			ObjectOp body) {

		final ObjectCondFunc function =
			ptr().data(code).conditionFunc(code).load(code);

		function.call(code, body(code, body)).go(code, code.tail(), exit);
	}

	final void writeProposition(Code code, ValOp result, ObjectOp body) {

		final ObjectValFunc function =
			ptr().data(code).propositionFunc(code).load(code);

		function.call(code, result, body(code, body));
	}

	public final void writeOverriddenValue(Code code, ValOp result) {
		writeOverriddenValue(code, null, result);
	}

	public final void writeOverriddenValue(
			Code code,
			CodePos exit,
			ValOp result) {
		// TODO overridden value
	}

	public final void writeOverriddenClaim(Code code, ValOp result) {
		writeOverriddenClaim(code, null, result);
	}

	public final void writeOverriddenClaim(Code code, CodePos exit, ValOp result) {
		// TODO overridden claim
	}

	public final void writeOverriddenProposition(Code code, ValOp result) {
		writeOverriddenPsoposition(code, null, result);
	}

	public final void writeOverriddenPsoposition(
			Code code,
			CodePos exit,
			ValOp result) {
		// TODO overridden definition
	}

	public final void writeOverriddenRequirement(Code code, CodePos exit) {
		// TODO overridden requirement
	}

	public final void writeOverriddenCondition(Code code, CodePos exit) {
		// TODO overridden condition
	}

	public void writeOverriddenInitializer(Code code, CodePos exit, ValOp result) {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		return "ObjectData[" + ptr().toString() + ']';
	}

	private final AnyOp body(Code code, ObjectOp body) {
		return body != null ? body.toAny(code) : mainBody(code);
	}

	private final AnyOp mainBody(Code code) {
		return ptr().data(code).objectPtr(code);
	}

}

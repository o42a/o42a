/*
    Test Framework
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.lib.test.rt;

import static org.o42a.core.member.MemberId.memberName;
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;

import org.o42a.codegen.code.Code;
import org.o42a.common.adapter.FloatByString;
import org.o42a.common.intrinsic.IntrinsicObject;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.ObjectMembers;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.lib.test.TestModule;


public class RtFloat extends IntrinsicObject {

	public RtFloat(TestModule module) {
		super(
				fieldDeclaration(
						module,
						module.distribute(),
						memberName("rt-float"))
				.prototype());
		setValueType(ValueType.FLOAT);
	}

	@Override
	protected Ascendants createAscendants() {
		return new Ascendants(this).setAncestor(
				getValueType().typeRef(this, getScope().getEnclosingScope()));
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
		members.addMember(new Parse(this).toMember());
		super.declareMembers(members);
	}

	@Override
	protected Definitions explicitDefinitions() {
		return null;
	}

	private static final class Parse extends FloatByString {

		Parse(Obj owner) {
			super(owner);
		}

		@Override
		protected Value<?> calculateValue(Scope scope) {

			final Value<?> value = super.calculateValue(scope);

			if (!value.getLogicalValue().isTrue()) {
				return value;
			}

			return getValueType().runtimeValue();
		}

		@Override
		protected strictfp void parse(Code code, ValOp result, ObjectOp input) {
			code.debug("Run-time float");
			super.parse(code, result, input);
		}

	}

}

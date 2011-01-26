/*
    Console Module
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
package org.o42a.lib.console.impl;

import static org.o42a.core.member.MemberId.memberName;
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;
import static org.o42a.core.ref.Ref.runtimeRef;
import static org.o42a.lib.console.impl.PrintFunc.printSignature;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodeBlk;
import org.o42a.codegen.code.CondBlk;
import org.o42a.common.intrinsic.IntrinsicObject;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.value.ValueType;
import org.o42a.lib.console.ConsoleModule;


public class Print extends IntrinsicObject {

	public Print(ConsoleModule module) {
		super(
				fieldDeclaration(
						module,
						module.distribute(),
						memberName("print"))
				.prototype());
	}

	@Override
	protected Ascendants createAscendants() {
		return new Ascendants(this).setAncestor(
				ValueType.VOID.typeRef(
						this,
						getScope().getEnclosingScope()));
	}

	@Override
	protected void postResolve() {
		super.postResolve();
		includeSource("print.o42a");
	}

	@Override
	protected Definitions explicitDefinitions() {
		return runtimeRef(this, distribute()).toDef().toDefinitions();
	}

	@Override
	protected ObjectValueIR createValueIR(ObjectIR objectIR) {
		return new ValueIR(objectIR);
	}

	private final class ValueIR extends ProposedValueIR {

		public ValueIR(ObjectIR objectIR) {
			super(objectIR);
		}

		@Override
		protected void proposition(Code code, ValOp result, ObjectOp host) {

			final MemberKey textKey = memberName("text").key(getScope());
			final CodeBlk cantPrint = code.addBlock("cant_print");
			final ObjectOp text =
				host.field(code, cantPrint.head(), textKey)
				.materialize(code, cantPrint.head());
			final PrintFunc printFunc = getGenerator().externalFunction(
					"o42a_io_print_str",
					printSignature(getGenerator())).op(code);
			final ValOp value =
				code.allocate(getGenerator().valType()).storeUnknown(code);

			text.writeValue(code, value);

			final CondBlk print =
				value.condition(code).branch(code, "print", "dont_print");
			final CodeBlk dontPrint = print.otherwise();

			printFunc.print(print, value);
			result.storeVoid(print);
			print.returnVoid();

			if (cantPrint.exists()) {
				result.storeFalse(cantPrint);
				cantPrint.returnVoid();
			}
			if (dontPrint.exists()) {
				result.storeFalse(dontPrint);
				dontPrint.returnVoid();
			}
		}

	}

}

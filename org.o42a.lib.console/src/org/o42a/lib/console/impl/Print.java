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
import static org.o42a.lib.console.impl.PrintFunc.printSignature;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodeBlk;
import org.o42a.codegen.code.CondBlk;
import org.o42a.common.intrinsic.IntrinsicObject;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.ref.path.Path;
import org.o42a.lib.console.ConsoleModule;


public class Print extends IntrinsicObject {

	private final String funcName;

	public Print(
			ConsoleModule module,
			String name,
			String funcName) {
		super(
				fieldDeclaration(
						module,
						module.distribute(),
						memberName(name))
				.prototype());
		this.funcName = funcName;
	}

	@Override
	protected Ascendants createAscendants() {

		final Scope enclosingScope = getScope().getEnclosingScope();
		final Path printToConsole =
			memberName("print_to_console").key(enclosingScope).toPath();

		return new Ascendants(this).setAncestor(
				printToConsole.target(
						this,
						enclosingScope.distribute())
				.toTypeRef());
	}

	@Override
	protected Definitions explicitDefinitions() {
		return getValueType().runtimeDef(this, distribute()).toDefinitions();
	}

	@Override
	protected ObjectValueIR createValueIR(ObjectIR objectIR) {
		return new ValueIR(objectIR);
	}

	private final class ValueIR extends ProposedValueIR {

		ValueIR(ObjectIR objectIR) {
			super(objectIR);
		}

		@Override
		protected void proposition(Code code, ValOp result, ObjectOp host) {

			final MemberKey textKey =
				memberName("text").key(getAncestor().getType().getScope());
			final CodeBlk cantPrint = code.addBlock("cant_print");
			final ObjectOp textObject =
				host.field(code, cantPrint.head(), textKey)
				.materialize(code, cantPrint.head());
			final ValOp text = textObject.writeValue(code);
			final CondBlk print =
				text.condition(code).branch(code, "print", "dont_print");
			final CodeBlk dontPrint = print.otherwise();
			final PrintFunc printFunc = getGenerator().externalFunction(
					Print.this.funcName,
					printSignature(getGenerator())).op(print);

			printFunc.print(print, text);
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

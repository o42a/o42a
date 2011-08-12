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

import static org.o42a.core.member.MemberId.fieldName;
import static org.o42a.core.value.Value.voidValue;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.common.object.AnnotatedBuiltin;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.core.Scope;
import org.o42a.core.artifact.Accessor;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.Path;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public abstract class AbstractPrint extends AnnotatedBuiltin {

	private final String funcName;
	private Ref text;

	public AbstractPrint(
			MemberOwner owner,
			AnnotatedSources sources,
			String funcName) {
		super(owner, sources);
		this.funcName = funcName;
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {
		return value().getValueType().runtimeValue();
	}

	@Override
	public void resolveBuiltin(Resolver resolver) {
		text().resolveValues(resolver);
	}

	@Override
	public ValOp writeBuiltin(ValDirs dirs, HostOp host) {

		final ValDirs textDirs = dirs.dirs().value(ValueType.STRING, "text");
		final Code code = textDirs.code();

		final ValOp text = text().op(host).writeValue(textDirs);
		final PrintFunc printFunc =
				printFunc(code.getGenerator()).op(null, code);

		printFunc.print(code, text);

		textDirs.done();

		return voidValue().op(dirs.getBuilder(), dirs.code());
	}

	@Override
	protected Ascendants buildAscendants() {

		final Scope enclosingScope = getScope().getEnclosingScope();
		final Path printToConsole =
				fieldName("print_to_console").key(enclosingScope).toPath();

		return new Ascendants(this).setAncestor(
				printToConsole.target(
						this,
						enclosingScope.distribute())
				.toTypeRef());
	}

	private Ref text() {
		if (this.text != null) {
			return this.text;
		}

		final MemberKey key = field("text", Accessor.DECLARATION).getKey();

		return this.text = key.toPath().target(this, distribute());
	}

	private FuncPtr<PrintFunc> printFunc(Generator generator) {
		return generator.externalFunction(
				AbstractPrint.this.funcName,
				PrintFunc.PRINT);
	}

}

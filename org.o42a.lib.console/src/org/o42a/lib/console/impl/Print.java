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
import static org.o42a.core.value.Value.voidValue;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.common.object.IntrinsicBuiltin;
import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.Path;
import org.o42a.core.value.Value;
import org.o42a.lib.console.ConsoleModule;
import org.o42a.util.use.UserInfo;


public class Print extends IntrinsicBuiltin {

	private final String funcName;

	public Print(
			ConsoleModule module,
			String name,
			String funcName) {
		super(
				module.toMemberOwner(),
				fieldDeclaration(
						module,
						module.distribute(),
						memberName(name))
				.prototype());
		this.funcName = funcName;
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {
		return getValueType().runtimeValue();
	}

	@Override
	public void resolveBuiltin(Obj object) {

		final UserInfo user = object.value();
		final Artifact<?> textPath = textKey().toPath().resolveArtifact(
				object,
				user,
				object.getScope());

		textPath.materialize().value().useBy(user);
	}

	@Override
	public ValOp writeBuiltin(ValDirs dirs, HostOp host) {

		final ValDirs textDirs = dirs.dirs().value();
		final ObjectOp textObject =
			host.field(textDirs.dirs(), textKey()).materialize(textDirs.dirs());
		final ValOp text = textObject.writeValue(textDirs);

		final PrintFunc printFunc =
			printFunc(dirs.getGenerator()).op(null, textDirs.code());

		printFunc.print(textDirs.code(), text);

		textDirs.done();

		return voidValue().op(dirs.code());
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

	private MemberKey textKey() {
		return memberName("text").key(
				type().useBy(dummyUser()).getAncestor()
				.typeObject(dummyUser()).getScope());
	}

	private FuncPtr<PrintFunc> printFunc(Generator generator) {
		return generator.externalFunction(
				Print.this.funcName,
				PrintFunc.PRINT);
	}

}

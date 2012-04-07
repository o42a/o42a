/*
    Console Module
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
package org.o42a.lib.console.impl;

import static org.o42a.core.member.MemberId.fieldName;
import static org.o42a.core.value.Value.voidValue;
import static org.o42a.lib.console.impl.PrintFunc.PRINT;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.common.object.AnnotatedBuiltin;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.object.Accessor;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.Path;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.fn.Cancelable;


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
		return value().getValueStruct().runtimeValue();
	}

	@Override
	public void resolveBuiltin(Resolver resolver) {
		text().resolve(resolver).resolveValue();
	}

	@Override
	public InlineValue inlineBuiltin(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct,
			Scope origin) {

		final InlineValue text = text().inline(normalizer, origin);

		if (text == null) {
			return null;
		}

		return new Inline(valueStruct, text);
	}

	@Override
	public ValOp writeBuiltin(ValDirs dirs, HostOp host) {

		final ValDirs textDirs = dirs.dirs().value(ValueStruct.STRING, "text");
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
				printToConsole.bind(this, enclosingScope)
				.typeRef(enclosingScope.distribute()));
	}

	private Ref text() {
		if (this.text != null) {
			return this.text;
		}

		final Path path =
				field("text", Accessor.DECLARATION)
				.getKey()
				.toPath()
				.dereference();

		return this.text = path.bind(this, getScope()).target(distribute());
	}

	private FuncPtr<PrintFunc> printFunc(Generator generator) {
		return generator.externalFunction().link(this.funcName, PRINT);
	}

	private final class Inline extends InlineValue {

		private final InlineValue inlineText;

		Inline(ValueStruct<?, ?> valueStruct, InlineValue text) {
			super(null, valueStruct);
			this.inlineText = text;
		}

		@Override
		public ValOp writeValue(ValDirs dirs, HostOp host) {

			final ValDirs textDirs =
					dirs.dirs().value(ValueStruct.STRING, "text");
			final Code code = textDirs.code();

			final ValOp text = this.inlineText.writeValue(textDirs, host);
			final PrintFunc printFunc =
					printFunc(code.getGenerator()).op(null, code);

			printFunc.print(code, text);

			textDirs.done();

			return voidValue().op(dirs.getBuilder(), dirs.code());
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

}

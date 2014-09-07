/*
    Console Module
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
package org.o42a.lib.console.impl;

import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;
import static org.o42a.core.member.MemberIdKind.FIELD_NAME;
import static org.o42a.lib.console.impl.PrintFn.PRINT;
import static org.o42a.util.string.Capitalization.CASE_INSENSITIVE;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.common.builtin.AnnotatedBuiltin;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.core.Scope;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.op.HostOp;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.Accessor;
import org.o42a.core.member.MemberName;
import org.o42a.core.object.Obj;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.Path;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.Void;
import org.o42a.util.fn.Cancelable;


public abstract class AbstractPrint extends AnnotatedBuiltin {

	private static final MemberName PRINT_TO_CONSOLE_MEMBER =
			FIELD_NAME.memberName(
					CASE_INSENSITIVE.canonicalName("print to console"));
	private static final MemberName TEXT_MEMBER =
			FIELD_NAME.memberName(
					CASE_INSENSITIVE.canonicalName("text"));

	private final String funcName;
	private Ref text;

	public AbstractPrint(Obj owner, AnnotatedSources sources, String funcName) {
		super(owner, sources);
		this.funcName = funcName;
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {
		return type().getParameters().runtimeValue();
	}

	@Override
	public void resolveBuiltin(FullResolver resolver) {
		text().resolveAll(resolver);
	}

	@Override
	public InlineEval inlineBuiltin(Normalizer normalizer, Scope origin) {

		final InlineValue text = text().inline(normalizer, origin);

		if (text == null) {
			return null;
		}

		return new InlinePrint(this, text);
	}

	@Override
	public Eval evalBuiltin() {
		return new PrintEval(this);
	}

	@Override
	protected Ascendants buildAscendants() {

		final Scope enclosingScope = getScope().getEnclosingScope();
		final Path printToConsole =
				PRINT_TO_CONSOLE_MEMBER.key(enclosingScope).toPath();

		return new Ascendants(this).setAncestor(
				printToConsole.bind(this, enclosingScope)
				.typeRef(enclosingScope.distribute()));
	}

	private Ref text() {
		if (this.text != null) {
			return this.text;
		}

		final Path path =
				member(TEXT_MEMBER, Accessor.DECLARATION)
				.getMemberKey()
				.toPath()
				.dereference();

		return this.text = path.bind(this, getScope()).target(distribute());
	}

	private FuncPtr<PrintFn> printFunc(Generator generator) {
		return generator.externalFunction().link(this.funcName, PRINT);
	}

	private static final class InlinePrint extends InlineEval {

		private final InlineValue text;
		private final AbstractPrint print;

		InlinePrint(AbstractPrint print, InlineValue inlineText) {
			super(null);
			this.print = print;
			this.text = inlineText;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {

			final ValDirs textDirs = dirs.dirs().nested().value(
					"text",
					ValueType.STRING,
					TEMP_VAL_HOLDER);
			final Block code = textDirs.code();

			final ValOp text = this.text.writeValue(textDirs, host);
			final PrintFn printFunc =
					this.print.printFunc(code.getGenerator()).op(null, code);

			printFunc.print(code, text);
			dirs.returnValue(
					code,
					ValueType.VOID
					.cast(this.print.type().getParameters())
					.compilerValue(Void.VOID)
					.op(dirs.getBuilder(), code));

			textDirs.done();
		}

		@Override
		public String toString() {
			if (this.print == null) {
				return super.toString();
			}
			return this.print.toString();
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

	private static final class PrintEval implements Eval {

		private final AbstractPrint print;

		PrintEval(AbstractPrint print) {
			this.print = print;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {

			final ValDirs textDirs = dirs.dirs().nested().value(
					"text",
					ValueType.STRING,
					TEMP_VAL_HOLDER);
			final Block code = textDirs.code();

			final ValOp text = this.print.text().op(host).writeValue(textDirs);
			final PrintFn printFunc =
					this.print.printFunc(code.getGenerator()).op(null, code);

			printFunc.print(code, text);
			dirs.returnValue(
					code,
					ValueType.VOID
					.cast(this.print.type().getParameters())
					.compilerValue(Void.VOID)
					.op(dirs.getBuilder(), code));

			textDirs.done();
		}

		@Override
		public String toString() {
			if (this.print == null) {
				return super.toString();
			}
			return this.print.toString();
		}

	}

}

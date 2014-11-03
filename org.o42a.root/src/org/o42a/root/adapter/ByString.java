/*
    Root Object Definition
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.root.adapter;

import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;
import static org.o42a.core.member.MemberIdKind.FIELD_NAME;
import static org.o42a.util.string.Capitalization.CASE_INSENSITIVE;

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
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberName;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.EscapeMode;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.util.fn.Cancelable;


public abstract class ByString<T> extends AnnotatedBuiltin {

	private static final MemberName INPUT_MEMBER =
			FIELD_NAME.memberName(CASE_INSENSITIVE.canonicalName("input"));

	private Ref input;

	public ByString(Obj owner, AnnotatedSources sources) {
		super(owner, sources);
	}

	@Override
	public EscapeMode escapeMode(Scope scope) {
		return input().escapeMode(scope);
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {

		final Value<?> inputValue = input().value(resolver);

		if (inputValue.getKnowledge().isFalse()) {
			return type().getParameters().falseValue();
		}
		if (!inputValue.getKnowledge().isKnown()) {
			return type().getParameters().runtimeValue();
		}

		final String input =
				ValueType.STRING.cast(inputValue).getCompilerValue();
		final T result = byString(resolver, resolver, input);

		if (result == null) {
			return type().getParameters().falseValue();
		}

		@SuppressWarnings("unchecked")
		final TypeParameters<T> typeParameters =
				(TypeParameters<T>) type().getParameters();

		return typeParameters.compilerValue(result);
	}

	@Override
	public void resolveBuiltin(FullResolver resolver) {
		input().resolveAll(resolver);
	}

	@Override
	public InlineEval inlineBuiltin(Normalizer normalizer, Scope origin) {

		final InlineValue input = input().inline(normalizer, origin);

		if (input == null) {
			return null;
		}

		return new InlineByString(this, input);
	}

	@Override
	public Eval evalBuiltin() {
		return new EvalByString(this);
	}

	protected abstract T byString(
			LocationInfo location,
			Resolver resolver,
			String input);

	protected abstract ValOp parse(ValDirs dirs, ValOp inputVal);

	protected final Ref input() {
		if (this.input != null) {
			return this.input;
		}

		final Member member = member(INPUT_MEMBER, Accessor.DECLARATION);
		final Path path = member.getMemberKey().toPath().dereference();

		return this.input = path.bind(this, getScope()).target(distribute());
	}

	private static final class InlineByString extends InlineEval {

		private final ByString<?> byString;
		private final InlineValue inputValue;

		InlineByString(ByString<?> byString, InlineValue inputValue) {
			super(null);
			this.byString = byString;
			this.inputValue = inputValue;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {

			final ValDirs inputDirs = dirs.dirs().nested().value(
					"input",
					ValueType.STRING,
					TEMP_VAL_HOLDER);
			final ValOp inputValue =
					this.inputValue.writeValue(inputDirs, host);

			final ValDirs parseDirs =
					inputDirs.dirs().nested().value(dirs.valDirs());

			dirs.returnValue(
					parseDirs.code(),
					this.byString.parse(parseDirs, inputValue));

			parseDirs.done();
			inputDirs.done();
		}

		@Override
		public String toString() {
			if (this.byString == null) {
				return super.toString();
			}
			return this.byString.toString();
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

	private static final class EvalByString implements Eval {

		private final ByString<?> byString;

		EvalByString(ByString<?> byString) {
			this.byString = byString;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {

			final ValDirs inputDirs = dirs.dirs().nested().value(
					"input",
					ValueType.STRING,
					TEMP_VAL_HOLDER);
			final ValOp inputValue =
					this.byString.input().op(host).writeValue(inputDirs);

			final ValDirs parseDirs =
					inputDirs.dirs().nested().value(dirs.valDirs());

			dirs.returnValue(
					parseDirs.code(),
					this.byString.parse(parseDirs, inputValue));

			parseDirs.done();
			inputDirs.done();
		}

		@Override
		public String toString() {
			if (this.byString == null) {
				return super.toString();
			}
			return this.byString.toString();
		}

	}

}

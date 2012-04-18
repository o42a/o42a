/*
    Modules Commons
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.common.adapter;

import org.o42a.common.object.AnnotatedBuiltin;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.object.Accessor;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.fn.Cancelable;


public abstract class ByString<T> extends AnnotatedBuiltin {

	private Ref input;

	public ByString(MemberOwner owner, AnnotatedSources sources) {
		super(owner, sources);
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {

		final Value<?> inputValue = input().value(resolver);

		if (inputValue.getKnowledge().isFalse()) {
			return value().getValueStruct().falseValue();
		}
		if (!inputValue.getKnowledge().isKnown()) {
			return value().getValueStruct().runtimeValue();
		}

		final String input =
				ValueStruct.STRING.cast(inputValue).getCompilerValue();
		final T result = byString(resolver, resolver, input);

		if (result == null) {
			return value().getValueStruct().falseValue();
		}

		@SuppressWarnings("unchecked")
		final ValueStruct<?, T> valueType =
				(ValueStruct<?, T>) value().getValueStruct();

		return valueType.compilerValue(result);
	}

	@Override
	public void resolveBuiltin(Resolver resolver) {
		input().resolve(resolver).resolveValue();
	}

	@Override
	public InlineValue inlineBuiltin(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct,
			Scope origin) {

		final InlineValue input = input().inline(normalizer, origin);

		if (input == null) {
			return null;
		}

		return new Inline(valueStruct, input);
	}

	@Override
	public ValOp writeBuiltin(ValDirs dirs, HostOp host) {

		final ValDirs inputDirs =
				dirs.dirs().value(ValueStruct.STRING, "input");
		final ValOp inputValue = input().op(host).writeValue(inputDirs);

		final ValDirs parseDirs = inputDirs.dirs().value(dirs);
		final ValOp result = parse(parseDirs, inputValue);

		parseDirs.done();
		inputDirs.done();

		return result;
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

		final Member member = field("input", Accessor.DECLARATION);
		final Path path = member.getKey().toPath().dereference();

		return this.input = path.bind(this, getScope()).target(distribute());
	}

	private final class Inline extends InlineValue {

		private InlineValue inputValue;

		Inline(ValueStruct<?, ?> valueStruct, InlineValue inputValue) {
			super(null, valueStruct);
			this.inputValue = inputValue;
		}

		@Override
		public ValOp writeValue(ValDirs dirs, HostOp host) {

			final ValDirs inputDirs =
					dirs.dirs().value(ValueStruct.STRING, "input");
			final ValOp inputValue =
					this.inputValue.writeValue(inputDirs, host);

			final ValDirs parseDirs = inputDirs.dirs().value(dirs);
			final ValOp result = parse(parseDirs, inputValue);

			parseDirs.done();
			inputDirs.done();

			return result;
		}

		@Override
		public String toString() {
			return "In-line[" + ByString.this + ']';
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

}

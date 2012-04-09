/*
    Modules Commons
    Copyright (C) 2012 Ruslan Lopatin

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
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.SingleValueType;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.fn.Cancelable;


public abstract class BuiltinConverter<F, T> extends AnnotatedBuiltin {

	private Ref object;

	public BuiltinConverter(MemberOwner owner, AnnotatedSources sources) {
		super(owner, sources);
	}

	public abstract SingleValueType<T> toValueType();

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {

		final Value<?> objectValue = object().value(resolver);

		if (objectValue.getKnowledge().isFalse()) {
			return value().getValueStruct().falseValue();
		}
		if (!objectValue.getKnowledge().isKnown()) {
			return value().getValueStruct().runtimeValue();
		}

		@SuppressWarnings("unchecked")
		final ValueStruct<?, F> valueStruct =
				(ValueStruct<?, F>) objectValue.getValueStruct();
		final F value = valueStruct.cast(objectValue).getCompilerValue();
		final T converted = convert(resolver, resolver, value);

		if (converted == null) {
			return toValueType().falseValue();
		}

		return toValueType().compilerValue(converted);
	}

	@Override
	public void resolveBuiltin(Resolver resolver) {
		object().resolve(resolver).resolveValue();
	}

	@Override
	public InlineValue inlineBuiltin(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct,
			Scope origin) {

		final InlineValue object = object().inline(normalizer, origin);

		if (object == null) {
			return null;
		}

		return new Inline(valueStruct, object);
	}

	@Override
	public ValOp writeBuiltin(ValDirs dirs, HostOp host) {

		final Ref object = object();
		final ValDirs valueDirs = dirs.dirs().value(
				object.valueStruct(object.getScope()),
				"value");
		final ValOp value = object().op(host).writeValue(valueDirs);

		final ValDirs targetDirs = valueDirs.dirs().value(dirs);
		final ValOp result = convert(targetDirs, value);

		targetDirs.done();
		valueDirs.done();

		return result;
	}

	protected abstract T convert(
			LocationInfo location,
			Resolver resolver,
			F value);

	protected abstract ValOp convert(ValDirs targetDirs, ValOp value);

	protected final Ref object() {
		if (this.object != null) {
			return this.object;
		}

		final Path path = getScope().getEnclosingScopePath();

		return this.object = path.bind(this, getScope()).target(distribute());
	}

	private final class Inline extends InlineValue {

		private InlineValue value;

		Inline(ValueStruct<?, ?> valueStruct, InlineValue value) {
			super(null, valueStruct);
			this.value = value;
		}

		@Override
		public ValOp writeValue(ValDirs dirs, HostOp host) {

			final Ref object = object();
			final ValDirs valueDirs = dirs.dirs().value(
					object.valueStruct(object.getScope()),
					"value");
			final ValOp value = this.value.writeValue(valueDirs, host);

			final ValDirs targetDirs = valueDirs.dirs().value(dirs);
			final ValOp result = convert(targetDirs, value);

			targetDirs.done();
			valueDirs.done();

			return result;
		}

		@Override
		public String toString() {
			return "In-line[" + BuiltinConverter.this + ']';
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}
}

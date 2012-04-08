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
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.fn.Cancelable;


public abstract class ToString<T> extends AnnotatedBuiltin {

	private Ref object;

	public ToString(MemberOwner owner, AnnotatedSources sources) {
		super(owner, sources);
		setValueStruct(ValueStruct.STRING);
	}

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
		final ValueStruct<?, T> valueStruct =
				(ValueStruct<?, T>) objectValue.getValueStruct();
		final T value = valueStruct.cast(objectValue).getCompilerValue();
		final String string = toString(resolver, resolver, value);

		if (string == null) {
			return ValueStruct.STRING.falseValue();
		}

		return ValueStruct.STRING.compilerValue(string);
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

		final ValDirs parseDirs = valueDirs.dirs().value(dirs);
		final ValOp result = print(parseDirs, value);

		parseDirs.done();
		valueDirs.done();

		return result;
	}

	protected abstract String toString(
			LocationInfo location,
			Resolver resolver,
			T value);

	protected abstract ValOp print(ValDirs stringDirs, ValOp value);

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

			final ValDirs parseDirs = valueDirs.dirs().value(dirs);
			final ValOp result = print(parseDirs, value);

			parseDirs.done();
			valueDirs.done();

			return result;
		}

		@Override
		public String toString() {
			return "In-line[" + ToString.this + ']';
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

}

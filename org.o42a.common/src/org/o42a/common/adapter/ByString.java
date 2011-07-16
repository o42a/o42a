/*
    Modules Commons
    Copyright (C) 2011 Ruslan Lopatin

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

import org.o42a.common.object.CompiledBuiltin;
import org.o42a.common.object.CompiledField;
import org.o42a.core.artifact.Accessor;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.Member;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public abstract class ByString<T> extends CompiledBuiltin {

	private Ref input;

	public ByString(CompiledField field) {
		super(field);
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {

		final Value<?> inputValue = input().value(resolver);

		if (inputValue.isFalse()) {
			return value().getValueType().falseValue();
		}
		if (!inputValue.isDefinite()) {
			return value().getValueType().runtimeValue();
		}

		final String input =
			ValueType.STRING.cast(inputValue).getDefiniteValue();
		final T result = byString(input(), resolver, input);

		if (result == null) {
			return value().getValueType().falseValue();
		}

		@SuppressWarnings("unchecked")
		final ValueType<T> valueType = (ValueType<T>) value().getValueType();

		return valueType.constantValue(result);
	}

	@Override
	public void resolveBuiltin(Obj object) {

		final Resolver resolver = object.value().valueResolver();

		input().resolveValues(resolver);
	}

	@Override
	public ValOp writeBuiltin(ValDirs dirs, HostOp host) {

		final ValDirs inputDirs = dirs.dirs().value(ValueType.STRING, "input");
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
		final Path path = member.getKey().toPath();

		return this.input = path.target(this, distribute());
	}

}

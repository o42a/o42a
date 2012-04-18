/*
    Intrinsics
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
package org.o42a.intrinsic.array;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.Int32op;
import org.o42a.common.object.AnnotatedBuiltin;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.object.array.Array;
import org.o42a.core.object.array.ArrayValueStruct;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.Path;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;
import org.o42a.util.fn.Cancelable;


abstract class AbstractArrayLength extends AnnotatedBuiltin {

	private Ref array;

	AbstractArrayLength(MemberOwner owner, AnnotatedSources sources) {
		super(owner, sources);
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {

		final Value<?> arrayValue = array().value(resolver);

		if (arrayValue.getKnowledge().isFalse()) {
			return ValueType.INTEGER.falseValue();
		}
		if (!arrayValue.getKnowledge().isKnownToCompiler()) {
			return ValueType.INTEGER.runtimeValue();
		}

		final Array array =
				valueStruct(resolver.getScope())
				.cast(arrayValue)
				.getCompilerValue();
		final int length = array.length();

		return ValueType.INTEGER.constantValue(Long.valueOf(length));
	}

	@Override
	public void resolveBuiltin(Resolver resolver) {
		array().resolve(resolver).resolveValue();
	}

	@Override
	public InlineValue inlineBuiltin(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct,
			Scope origin) {

		final InlineValue arrayValue = array().inline(normalizer, origin);

		if (arrayValue == null) {
			return null;
		}

		return new Inline(valueStruct, arrayValue);
	}

	@Override
	public ValOp writeBuiltin(ValDirs dirs, HostOp host) {
		return write(dirs, host, null);
	}

	private ArrayValueStruct valueStruct(Scope scope) {
		return (ArrayValueStruct) array().valueStruct(scope);
	}

	private Ref array() {
		if (this.array != null) {
			return this.array;
		}

		final Path path = getScope().getEnclosingScopePath();

		return this.array = path.bind(this, getScope()).target(distribute());
	}

	private ValOp write(ValDirs dirs, HostOp host, InlineValue inlineArray) {

		final ValDirs arrayDirs =
				dirs.dirs().value(valueStruct(getScope()), "array_val");
		final Code code = arrayDirs.code();

		final ValOp arrayVal;

		if (inlineArray != null) {
			arrayVal = inlineArray.writeValue(arrayDirs, host);
		} else {
			arrayVal = array().op(host).writeValue(arrayDirs);
		}

		final Int32op length = arrayVal.loadLength(code.id("array_len"), code);
		final ValOp result =
				dirs.value().store(code, length.toInt64(null, code));

		arrayDirs.done();

		return result;
	}

	private final class Inline extends InlineValue {

		private final InlineValue arrayValue;

		Inline(ValueStruct<?, ?> valueStruct, InlineValue arrayValue) {
			super(null, valueStruct);
			this.arrayValue = arrayValue;
		}

		@Override
		public ValOp writeValue(ValDirs dirs, HostOp host) {
			return write(dirs, host, this.arrayValue);
		}

		@Override
		public String toString() {
			if (this.arrayValue == null) {
				return super.toString();
			}
			return this.arrayValue + ":length";
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

}

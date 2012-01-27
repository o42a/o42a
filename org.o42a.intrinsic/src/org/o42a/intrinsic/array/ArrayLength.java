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
import org.o42a.core.artifact.array.Array;
import org.o42a.core.artifact.array.ArrayValueStruct;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.Path;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


abstract class ArrayLength extends AnnotatedBuiltin {

	private Ref array;

	ArrayLength(MemberOwner owner, AnnotatedSources sources) {
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
	public ValOp writeBuiltin(ValDirs dirs, HostOp host) {

		final ValDirs arrayDirs =
				dirs.dirs().value(valueStruct(getScope()), "array_val");
		final Code code = arrayDirs.code();

		final ValOp string = array().op(host).writeValue(arrayDirs);
		final Int32op length = string.loadLength(code.id("array_len"), code);
		final ValOp result =
				dirs.value().store(code, length.toInt64(null, code));

		arrayDirs.done();

		return result;
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

}

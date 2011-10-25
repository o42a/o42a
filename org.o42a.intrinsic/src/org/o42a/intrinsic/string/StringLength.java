/*
    Intrinsics
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
package org.o42a.intrinsic.string;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.Int32op;
import org.o42a.common.object.AnnotatedBuiltin;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.Path;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;


@SourcePath(relativeTo = StringValueTypeObject.class, value = "length.o42a")
final class StringLength extends AnnotatedBuiltin {

	private Ref string;

	public StringLength(MemberOwner owner, AnnotatedSources sources) {
		super(owner, sources);
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {

		final Value<?> stringValue = string().value(resolver);

		if (stringValue.isFalse()) {
			return ValueType.INTEGER.falseValue();
		}
		if (!stringValue.isDefinite()) {
			return ValueType.INTEGER.runtimeValue();
		}

		final String string =
				ValueType.STRING.cast(stringValue).getDefiniteValue();
		final int length = string.length();

		return ValueType.INTEGER.constantValue(Long.valueOf(length));
	}

	@Override
	public void resolveBuiltin(Resolver resolver) {
		string().resolveValues(resolver);
	}

	@Override
	public ValOp writeBuiltin(ValDirs dirs, HostOp host) {

		final ValDirs stringDirs =
				dirs.dirs().value(ValueStruct.STRING, "string_val");
		final Code code = stringDirs.code();

		final ValOp string = string().op(host).writeValue(stringDirs);
		final Int32op length = string.loadLength(code.id("str_len"), code);
		final ValOp result =
				dirs.value().store(code, length.toInt64(null, code));

		stringDirs.done();

		return result;
	}

	private Ref string() {
		if (this.string != null) {
			return this.string;
		}

		final Path path = getScope().getEnclosingScopePath();

		return this.string = path.bind(this, getScope()).target(distribute());
	}

}

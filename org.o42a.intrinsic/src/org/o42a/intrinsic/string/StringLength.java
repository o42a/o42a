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
package org.o42a.intrinsic.string;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.Int32op;
import org.o42a.common.object.AnnotatedBuiltin;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.ref.*;
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

		if (stringValue.getKnowledge().isFalse()) {
			return ValueType.INTEGER.falseValue();
		}
		if (!stringValue.getKnowledge().isKnown()) {
			return ValueType.INTEGER.runtimeValue();
		}

		final String string =
				ValueType.STRING.cast(stringValue).getCompilerValue();
		final int length = string.length();

		return ValueType.INTEGER.constantValue(Long.valueOf(length));
	}

	@Override
	public void resolveBuiltin(Resolver resolver) {
		string().resolve(resolver).resolveValue();
	}

	@Override
	public InlineValue inlineBuiltin(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct,
			Scope origin) {

		final InlineValue stringValue = string().inline(normalizer, origin);

		if (stringValue == null) {
			return null;
		}

		return new Inline(valueStruct, stringValue);
	}

	@Override
	public ValOp writeBuiltin(ValDirs dirs, HostOp host) {
		return write(dirs, host, string().op(host), null);
	}

	private Ref string() {
		if (this.string != null) {
			return this.string;
		}

		final Path path = getScope().getEnclosingScopePath();

		return this.string = path.bind(this, getScope()).target(distribute());
	}

	private static ValOp write(
			ValDirs dirs,
			HostOp host,
			RefOp str,
			InlineValue inlineStr) {

		final ValDirs stringDirs =
				dirs.dirs().value(ValueStruct.STRING, "string_val");
		final Code code = stringDirs.code();

		final ValOp stringVal;

		if (inlineStr != null) {
			stringVal = inlineStr.writeValue(stringDirs, host);
		} else {
			stringVal = str.writeValue(stringDirs);
		}

		final Int32op length = stringVal.loadLength(code.id("str_len"), code);
		final ValOp result =
				dirs.value().store(code, length.toInt64(null, code));

		stringDirs.done();

		return result;
	}

	private static final class Inline extends InlineValue {

		private final InlineValue stringValue;

		Inline(ValueStruct<?, ?> valueStruct, InlineValue stringValue) {
			super(valueStruct);
			this.stringValue = stringValue;
		}

		@Override
		public ValOp writeValue(ValDirs dirs, HostOp host) {
			return write(dirs, host, null, this.stringValue);
		}

		@Override
		public void cancel() {
			this.stringValue.cancel();
		}

		@Override
		public String toString() {
			if (this.stringValue == null) {
				return super.toString();
			}
			return this.stringValue + ":length";
		}

	}

}

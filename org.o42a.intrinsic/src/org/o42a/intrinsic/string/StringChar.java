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

import static java.lang.Integer.numberOfTrailingZeros;
import static org.o42a.core.ir.value.Val.ALIGNMENT_MASK;
import static org.o42a.core.ir.value.Val.CONDITION_FLAG;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.Int32op;
import org.o42a.codegen.code.op.Int64op;
import org.o42a.common.object.AnnotatedBuiltin;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.core.artifact.Accessor;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.Path;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;


@SourcePath(relativeTo = StringValueTypeObject.class, value = "char.o42a")
final class StringChar extends AnnotatedBuiltin {

	private Ref string;
	private Ref index;

	public StringChar(MemberOwner owner, AnnotatedSources sources) {
		super(owner, sources);
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {

		final Value<?> stringValue = string().value(resolver);
		final Value<?> indexValue = index().value(resolver);

		if (stringValue.getKnowledge().isFalse()
				|| indexValue.getKnowledge().isFalse()) {
			return ValueType.STRING.falseValue();
		}
		if (!stringValue.getKnowledge().isKnown()
				|| !indexValue.getKnowledge().isKnown()) {
			return ValueType.STRING.runtimeValue();
		}

		final String string =
				ValueType.STRING.cast(stringValue).getCompilerValue();
		final long index =
				ValueType.INTEGER.cast(indexValue).getCompilerValue();

		if (index < 0 || index >= string.length()) {
			resolver.getLogger().error(
					"invalid_char_index",
					resolver,
					"Invalid charater index: %d",
					index);
			return ValueType.STRING.falseValue();
		}

		return ValueType.STRING.constantValue(
				Character.toString(string.charAt((int) index)));
	}

	@Override
	public void resolveBuiltin(Resolver resolver) {
		string().resolve(resolver).resolveValue();
		index().resolve(resolver).resolveValue();
	}

	@Override
	public ValOp writeBuiltin(ValDirs dirs, HostOp host) {

		final ValDirs stringDirs =
				dirs.dirs().value(ValueStruct.STRING, "string");
		final ValOp stringVal = string().op(host).writeValue(stringDirs);

		final ValDirs indexDirs =
				stringDirs.dirs().value(ValueStruct.INTEGER, "index");
		final ValOp indexVal = index().op(host).writeValue(indexDirs);

		final Code code = indexDirs.code();

		final Int64op index =
				indexVal.rawValue(code.id("index"), code)
				.load(null, code);

		index.lt(null, code, code.int64(0L)).go(code, indexDirs.falseDir());

		final Int64op length =
				stringVal.loadLength(code.id("str_len"), code)
				.toInt64(null, code);

		index.ge(null, code, length).go(code, indexDirs.falseDir());

		final Int32op cmask = stringVal.loadCharMask(code.id("cmask"), code);
		final Int32op csizeShift =
				stringVal.loadAlignmentShift(code.id("csizeshft"), code);
		final Int32op dataOffset =
				index.toInt32(null, code)
				.shl(code.id("data_offset"), code, csizeShift);
		final AnyOp begin =
				stringVal.loadData(code.id("str_data"), code);
		final AnyOp charPtr =
				begin.offset(code.id("char_ptr"), code, dataOffset);
		final Int32op chr =
				charPtr.toInt32(null, code)
				.load(null, code)
				.and(code.id("char"), code, cmask);

		final ValOp result = dirs.value();
		final Int32op flags = code.int32(CONDITION_FLAG).or(
				code.id("flags"),
				code,
				csizeShift.shl(
						null,
						code,
						numberOfTrailingZeros(ALIGNMENT_MASK)));

		result.flags(null, code).store(code, flags);
		result.length(null, code).store(code, code.int32(1));
		result.rawValue(null, code).store(
				code,
				chr.toInt64(null, code));

		indexDirs.done();
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

	private Ref index() {
		if (this.index != null) {
			return this.index;
		}

		final MemberKey indexKey =
				field("index", Accessor.DECLARATION).getKey();

		return this.index =
				indexKey.toPath().bind(this, getScope()).target(distribute());
	}

}

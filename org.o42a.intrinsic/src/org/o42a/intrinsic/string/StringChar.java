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
import static org.o42a.core.ir.value.Val.VAL_ALIGNMENT_MASK;
import static org.o42a.core.ir.value.Val.VAL_CONDITION;
import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;
import static org.o42a.core.member.MemberId.fieldName;
import static org.o42a.util.string.Capitalization.CASE_INSENSITIVE;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.Int32op;
import org.o42a.codegen.code.op.Int64op;
import org.o42a.common.object.AnnotatedBuiltin;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValFlagsOp;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.MemberName;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.object.Accessor;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.Path;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;
import org.o42a.util.fn.Cancelable;


@SourcePath(relativeTo = StringValueTypeObject.class, value = "char.o42a")
final class StringChar extends AnnotatedBuiltin {

	private static final MemberName INDEX_MEMBER =
			fieldName(CASE_INSENSITIVE.canonicalName("index"));

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
	public void resolveBuiltin(FullResolver resolver) {
		string().resolveAll(resolver);
		index().resolveAll(resolver);
	}

	@Override
	public InlineEval inlineBuiltin(Normalizer normalizer, Scope origin) {

		final InlineValue inlineString = string().inline(normalizer, origin);
		final InlineValue inlineIndex = index().inline(normalizer, origin);

		if (inlineString == null || inlineIndex == null) {
			return null;
		}

		return new CharEval(this, inlineString, inlineIndex);
	}

	@Override
	public Eval evalBuiltin() {
		return new CharEval(this, null, null);
	}

	@Override
	public String toString() {
		if (this.string == null || this.index == null) {
			return super.toString();
		}
		return "(" + this.string + "):char[" + this.index+ ']';
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

		final Path path =
				member(INDEX_MEMBER, Accessor.DECLARATION)
				.getKey()
				.toPath()
				.dereference();

		return this.index = path.bind(this, getScope()).target(distribute());
	}

	private void write(
			DefDirs dirs,
			HostOp host,
			InlineValue inlineString,
			InlineValue inlineIndex) {

		final ValDirs stringDirs = dirs.dirs().nested().value(
				"string",
				ValueStruct.STRING,
				TEMP_VAL_HOLDER);
		final ValOp stringVal;

		if (inlineString != null) {
			stringVal = inlineString.writeValue(stringDirs, host);
		} else {
			stringVal = string().op(host).writeValue(stringDirs);
		}

		final ValDirs indexDirs = stringDirs.dirs().nested().value(
				"index",
				ValueStruct.INTEGER,
				TEMP_VAL_HOLDER);
		final ValOp indexVal;

		if (inlineIndex != null) {
			indexVal = inlineIndex.writeValue(indexDirs, host);
		} else {
			indexVal = index().op(host).writeValue(indexDirs);
		}

		final Block code = indexDirs.code();

		final Int64op index =
				indexVal.rawValue(code.id("index"), code)
				.load(null, code);

		index.lt(null, code, code.int64(0L)).go(code, indexDirs.falseDir());

		final Int64op length =
				stringVal.loadLength(code.id("str_len"), code)
				.toInt64(null, code);

		index.ge(null, code, length).go(code, indexDirs.falseDir());

		final ValFlagsOp stringFlags = stringVal.flags(code);
		final Int32op cmask = stringFlags.charMask(code.id("cmask"), code);
		final Int32op csizeShift =
				stringFlags.alignmentShift(code.id("csizeshft"), code);
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
		final Int32op flags = code.int32(VAL_CONDITION).or(
				code.id("flags"),
				code,
				csizeShift.shl(
						null,
						code,
						numberOfTrailingZeros(VAL_ALIGNMENT_MASK)));

		result.flags(code).store(code, flags);
		result.length(null, code).store(code, code.int32(1));
		result.rawValue(null, code).store(
				code,
				chr.toInt64(null, code));

		dirs.returnValue(code, result);
		indexDirs.done();
		stringDirs.done();
	}

	private static final class CharEval extends InlineEval {

		private final StringChar stringChar;
		private final InlineValue inlineString;
		private final InlineValue inlineIndex;

		CharEval(
				StringChar stringChar,
				InlineValue inlineString,
				InlineValue inlineValue) {
			super(null);
			this.stringChar = stringChar;
			this.inlineString = inlineString;
			this.inlineIndex = inlineValue;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {
			this.stringChar.write(
					dirs,
					host,
					this.inlineString,
					this.inlineIndex);
		}

		@Override
		public String toString() {
			if (this.stringChar == null) {
				return super.toString();
			}
			if (this.inlineIndex == null) {
				return this.stringChar.toString();
			}
			return "(" + this.inlineString
					+ "):char[" + this.inlineIndex + ']';
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

}

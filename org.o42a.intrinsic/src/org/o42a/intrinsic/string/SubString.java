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

import static org.o42a.intrinsic.string.SubStringFunc.SUB_STRING;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.common.object.AnnotatedBuiltin;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.object.Accessor;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.Path;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;


@SourcePath(relativeTo = StringValueTypeObject.class, value = "substring.o42a")
final class SubString extends AnnotatedBuiltin {

	private Ref string;
	private Ref from;
	private Ref to;

	public SubString(MemberOwner owner, AnnotatedSources sources) {
		super(owner, sources);
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {

		final Value<?> stringValue = string().value(resolver);
		final Value<?> fromValue = from().value(resolver);
		final Value<?> toValue = to().value(resolver);

		if (stringValue.getKnowledge().isFalse()
				|| fromValue.getKnowledge().isFalse()
				|| toValue.getKnowledge().isFalse()) {
			return ValueType.STRING.falseValue();
		}
		if (!stringValue.getKnowledge().isKnown()) {
			return ValueType.STRING.runtimeValue();
		}

		final String string =
				ValueType.STRING.cast(stringValue).getCompilerValue();
		final int length = string.length();
		final long from;
		final long to;
		boolean ok = true;

		if (fromValue.getKnowledge().isKnown()) {
			from = ValueType.INTEGER.cast(fromValue).getCompilerValue();
			if (from < 0 || from > length) {
				resolver.getLogger().error(
						"invalid_substr_from",
						resolver,
						"Invalid substring range start: %d",
						from);
				ok = false;
			}
		} else {
			from = -1L;
		}
		if (toValue.getKnowledge().isKnown()) {
			to = ValueType.INTEGER.cast(toValue).getCompilerValue();
			if (to < 0 || to > length) {
				resolver.getLogger().error(
						"invalid_substr_to",
						resolver,
						"Invalid substring range end: %d",
						to);
				ok = false;
			}
		} else {
			to = -1;
		}

		if (!ok) {
			return ValueType.STRING.falseValue();
		}
		if (from < 0 || to < 0) {
			return ValueType.STRING.runtimeValue();
		}
		if (from > to) {
			resolver.getLogger().error(
					"invalid_substr_range",
					resolver,
					"Invalid substring range: %d - %d",
					from,
					to);
			return ValueType.STRING.falseValue();
		}

		final String substring = string.substring((int) from, (int) to);

		return ValueType.STRING.constantValue(substring);
	}

	@Override
	public void resolveBuiltin(Resolver resolver) {
		string().resolve(resolver).resolveValue();
		from().resolve(resolver).resolveValue();
		to().resolve(resolver).resolveValue();
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

		final InlineValue fromValue = from().inline(normalizer, origin);

		if (fromValue == null) {
			stringValue.cancel();
			return null;
		}

		final InlineValue toValue = to().inline(normalizer, origin);

		if (toValue == null) {
			stringValue.cancel();
			fromValue.cancel();
			return null;
		}

		return new Inline(valueStruct, stringValue, fromValue, toValue);
	}

	@Override
	public ValOp writeBuiltin(ValDirs dirs, HostOp host) {
		return write(
				dirs,
				host,
				string().op(host),
				null,
				from().op(host),
				null,
				to().op(host),
				null);
	}

	private Ref string() {
		if (this.string != null) {
			return this.string;
		}

		final Path path = getScope().getEnclosingScopePath();

		return this.string = path.bind(this, getScope()).target(distribute());
	}

	private Ref from() {
		if (this.from != null) {
			return this.from;
		}

		final Path path =
				field("from", Accessor.DECLARATION)
				.getKey()
				.toPath()
				.dereference();

		return this.from = path.bind(this, getScope()).target(distribute());
	}

	private Ref to() {
		if (this.to != null) {
			return this.to;
		}

		final Path path =
				field("to", Accessor.DECLARATION)
				.getKey()
				.toPath()
				.dereference();

		return this.to = path.bind(this, getScope()).target(distribute());
	}

	private static ValOp write(
			ValDirs dirs,
			HostOp host,
			RefOp str,
			InlineValue inlineStr,
			RefOp from,
			InlineValue inlineFrom,
			RefOp to,
			InlineValue inlineTo) {

		final ValDirs stringDirs =
				dirs.dirs().value(ValueStruct.STRING, "string");
		final ValOp stringVal;

		if (inlineStr != null) {
			stringVal = inlineStr.writeValue(stringDirs, host);
		} else {
			stringVal = str.writeValue(stringDirs);
		}

		final ValDirs fromDirs =
				stringDirs.dirs().value(ValueStruct.INTEGER, "from");
		final ValOp fromVal;

		if (inlineFrom != null) {
			fromVal = inlineFrom.writeValue(fromDirs, host);
		} else {
			fromVal = from.writeValue(fromDirs);
		}

		final ValDirs toDirs =
				fromDirs.dirs().value(ValueStruct.INTEGER, "to");
		final ValOp toVal;

		if (inlineTo != null) {
			toVal = inlineTo.writeValue(toDirs, host);
		} else {
			toVal = to.writeValue(toDirs);
		}

		final ValDirs substringDirs = toDirs.dirs().value(dirs);

		final Code code = substringDirs.code();
		final FuncPtr<SubStringFunc> funcPtr =
				substringDirs.getGenerator()
				.externalFunction()
				.link("o42a_str_sub", SUB_STRING);
		final SubStringFunc func = funcPtr.op(null, code);

		final ValOp substring = func.substring(
				substringDirs,
				stringVal,
				fromVal.rawValue(null, code).load(code.id("from"), code),
				toVal.rawValue(null, code).load(code.id("to"), code));

		substringDirs.done();
		toDirs.done();
		fromDirs.done();
		stringDirs.done();

		return substring;
	}

	private static final class Inline extends InlineValue {

		private final InlineValue stringValue;
		private final InlineValue fromValue;
		private final InlineValue toValue;

		Inline(
				ValueStruct<?, ?> valueStruct,
				InlineValue stringValue,
				InlineValue fromValue,
				InlineValue toValue) {
			super(valueStruct);
			this.stringValue = stringValue;
			this.fromValue = fromValue;
			this.toValue = toValue;
		}

		@Override
		public ValOp writeValue(ValDirs dirs, HostOp host) {
			return write(
					dirs,
					host,
					null,
					this.stringValue,
					null,
					this.fromValue,
					null,
					this.toValue);
		}

		@Override
		public void cancel() {
			this.stringValue.cancel();
			this.fromValue.cancel();
			this.toValue.cancel();
		}

		@Override
		public String toString() {
			if (this.toValue == null) {
				return super.toString();
			}
			return (this.stringValue + ":substring["
					+ this.fromValue + ", " + this.toValue + ']');
		}
	}

}

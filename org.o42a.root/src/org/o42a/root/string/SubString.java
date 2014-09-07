/*
    Root Object Definition
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.root.string;

import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;
import static org.o42a.core.member.MemberIdKind.FIELD_NAME;
import static org.o42a.root.string.SubStringFn.SUB_STRING;
import static org.o42a.util.string.Capitalization.CASE_INSENSITIVE;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.common.builtin.AnnotatedBuiltin;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.core.Scope;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.op.HostOp;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.Accessor;
import org.o42a.core.member.MemberName;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.Path;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.util.fn.Cancelable;
import org.o42a.util.string.ID;


@SourcePath(relativeTo = StringValueTypeObject.class, value = "substring.o42a")
final class SubString extends AnnotatedBuiltin {

	private static final MemberName FROM_MEMBER =
			FIELD_NAME.memberName(CASE_INSENSITIVE.canonicalName("from"));
	private static final MemberName TO_MEMBER =
			FIELD_NAME.memberName(CASE_INSENSITIVE.canonicalName("to"));

	private static final ID FROM_ID = ID.id("from");
	private static final ID TO_ID = ID.id("to");

	private Ref string;
	private Ref from;
	private Ref to;

	public SubString(Obj owner, AnnotatedSources sources) {
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
			return type().getParameters().falseValue();
		}
		if (!stringValue.getKnowledge().isKnown()) {
			return type().getParameters().runtimeValue();
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
			return type().getParameters().falseValue();
		}
		if (from < 0 || to < 0) {
			return type().getParameters().runtimeValue();
		}
		if (from > to) {
			resolver.getLogger().error(
					"invalid_substr_range",
					resolver,
					"Invalid substring range: %d - %d",
					from,
					to);
			return type().getParameters().falseValue();
		}

		final String substring = string.substring((int) from, (int) to);

		return ValueType.STRING.cast(type().getParameters())
				.compilerValue(substring);
	}

	@Override
	public void resolveBuiltin(FullResolver resolver) {
		string().resolveAll(resolver);
		from().resolveAll(resolver);
		to().resolveAll(resolver);
	}

	@Override
	public InlineEval inlineBuiltin(Normalizer normalizer, Scope origin) {

		final InlineValue inlineString = string().inline(normalizer, origin);
		final InlineValue inlineFrom = from().inline(normalizer, origin);
		final InlineValue inlineTo = to().inline(normalizer, origin);

		if (inlineString == null || inlineFrom == null || inlineTo == null) {
			return null;
		}

		return new SubStringEval(this, inlineString, inlineFrom, inlineTo);
	}

	@Override
	public Eval evalBuiltin() {
		return new SubStringEval(this, null, null, null);
	}

	@Override
	public String toString() {
		if (this.string == null || this.from == null || this.to == null) {
			return super.toString();
		}
		return ("(" + this.string + "):substring["
				+ this.from + ", " + this.to + ']');
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
				member(FROM_MEMBER, Accessor.DECLARATION)
				.getMemberKey()
				.toPath()
				.dereference();

		return this.from = path.bind(this, getScope()).target(distribute());
	}

	private Ref to() {
		if (this.to != null) {
			return this.to;
		}

		final Path path =
				member(TO_MEMBER, Accessor.DECLARATION)
				.getMemberKey()
				.toPath()
				.dereference();

		return this.to = path.bind(this, getScope()).target(distribute());
	}

	private void write(
			DefDirs dirs,
			HostOp host,
			InlineValue inlineString,
			InlineValue inlineFrom,
			InlineValue inlineTo) {

		final ValDirs stringDirs = dirs.dirs().nested().value(
				"string",
				ValueType.STRING,
				TEMP_VAL_HOLDER);
		final ValOp stringVal;

		if (inlineString != null) {
			stringVal = inlineString.writeValue(stringDirs, host);
		} else {
			stringVal = string().op(host).writeValue(stringDirs);
		}

		final ValDirs fromDirs = stringDirs.dirs().nested().value(
				"from",
				ValueType.INTEGER,
				TEMP_VAL_HOLDER);
		final ValOp fromVal;

		if (inlineFrom != null) {
			fromVal = inlineFrom.writeValue(fromDirs, host);
		} else {
			fromVal = from().op(host).writeValue(fromDirs);
		}

		final ValDirs toDirs = fromDirs.dirs().nested().value(
				"to",
				ValueType.INTEGER,
				TEMP_VAL_HOLDER);
		final ValOp toVal;

		if (inlineTo != null) {
			toVal = inlineTo.writeValue(toDirs, host);
		} else {
			toVal = to().op(host).writeValue(toDirs);
		}

		final DefDirs substringDirs =
				toDirs.dirs().nested().value(dirs.valDirs()).def();

		final Block code = substringDirs.code();
		final FuncPtr<SubStringFn> funcPtr =
				substringDirs.getGenerator()
				.externalFunction()
				.link("o42a_str_sub", SUB_STRING);
		final SubStringFn func = funcPtr.op(null, code);

		func.substring(
				substringDirs,
				stringVal,
				fromVal.rawValue(null, code).load(FROM_ID, code),
				toVal.rawValue(null, code).load(TO_ID, code));

		substringDirs.done();
		toDirs.done();
		fromDirs.done();
		stringDirs.done();
	}

	private static final class SubStringEval extends InlineEval {

		private final SubString subString;
		private final InlineValue inlineString;
		private final InlineValue inlineFrom;
		private final InlineValue inlineTo;

		SubStringEval(
				SubString subString,
				InlineValue stringValue,
				InlineValue fromValue,
				InlineValue toValue) {
			super(null);
			this.subString = subString;
			this.inlineString = stringValue;
			this.inlineFrom = fromValue;
			this.inlineTo = toValue;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {
			this.subString.write(
					dirs,
					host,
					this.inlineString,
					this.inlineFrom,
					this.inlineTo);
		}

		@Override
		public String toString() {
			if (this.subString == null) {
				return super.toString();
			}
			if (this.inlineTo == null) {
				return this.subString.toString();
			}
			return (this.inlineString + ":substring["
					+ this.inlineFrom + ", " + this.inlineTo + ']');
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

}

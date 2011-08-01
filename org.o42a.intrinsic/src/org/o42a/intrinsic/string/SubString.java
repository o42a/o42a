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

import static org.o42a.intrinsic.string.SubStringFunc.SUB_STRING;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.common.object.AnnotatedBuiltin;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.core.artifact.Accessor;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.Path;
import org.o42a.core.value.Value;
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

		if (stringValue.isFalse()
				|| fromValue.isFalse()
				|| toValue.isFalse()) {
			return ValueType.STRING.falseValue();
		}
		if (!stringValue.isDefinite()) {
			return ValueType.STRING.runtimeValue();
		}

		final String string =
			ValueType.STRING.cast(stringValue).getDefiniteValue();
		final int length = string.length();
		final long from;
		final long to;
		boolean ok = true;

		if (fromValue.isDefinite()) {
			from = ValueType.INTEGER.cast(fromValue).getDefiniteValue();
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
		if (toValue.isDefinite()) {
			to = ValueType.INTEGER.cast(toValue).getDefiniteValue();
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
	public void resolveBuiltin(Obj object) {

		final Resolver resolver = object.value().proposition().resolver();

		string().resolveValues(resolver);
		from().resolveValues(resolver);
		to().resolveValues(resolver);
	}

	@Override
	public ValOp writeBuiltin(ValDirs dirs, HostOp host) {

		final ValDirs stringDirs =
			dirs.dirs().value(ValueType.STRING, "string");
		final ValOp stringVal = string().op(host).writeValue(stringDirs);

		final ValDirs fromDirs =
			stringDirs.dirs().value(ValueType.INTEGER, "from");
		final ValOp fromVal = from().op(host).writeValue(fromDirs);

		final ValDirs toDirs =
			fromDirs.dirs().value(ValueType.INTEGER, "to");
		final ValOp toVal = from().op(host).writeValue(fromDirs);

		final ValDirs substringDirs = toDirs.dirs().value(dirs);

		final Code code = substringDirs.code();
		final FuncPtr<SubStringFunc> funcPtr =
			substringDirs.getGenerator().externalFunction(
					"o42a_str_sub",
					SUB_STRING);
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

	private Ref string() {
		if (this.string != null) {
			return this.string;
		}

		final Path path = getScope().getEnclosingScopePath();

		return this.string = path.target(this, distribute());
	}

	private Ref from() {
		if (this.from != null) {
			return this.from;
		}

		final MemberKey fromKey = field("from", Accessor.DECLARATION).getKey();

		return this.from = fromKey.toPath().target(this, distribute());
	}

	private Ref to() {
		if (this.to != null) {
			return this.to;
		}

		final MemberKey toKey = field("to", Accessor.DECLARATION).getKey();

		return this.to = toKey.toPath().target(this, distribute());
	}

}

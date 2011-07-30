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
import org.o42a.codegen.code.FuncPtr;
import org.o42a.common.object.AnnotatedBuiltin;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.core.artifact.Accessor;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.ValuePart;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


@SourcePath(relativeTo = Strings.class, value = "concat.o42a")
final class ConcatStrings extends AnnotatedBuiltin {

	private Ref what;
	private Ref with;

	public ConcatStrings(MemberOwner owner, AnnotatedSources sources) {
		super(owner, sources);
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {

		final Value<?> whatValue = what().value(resolver);
		final Value<?> withValue = with().value(resolver);

		if (whatValue.isFalse() || withValue.isFalse()) {
			return ValueType.STRING.falseValue();
		}
		if (!whatValue.isDefinite() || !withValue.isDefinite()) {
			return ValueType.STRING.runtimeValue();
		}

		final String what =
			ValueType.STRING.cast(whatValue).getDefiniteValue();
		final String with =
			ValueType.STRING.cast(withValue).getDefiniteValue();

		return ValueType.STRING.constantValue(what + with);
	}

	@Override
	public void resolveBuiltin(Obj object) {

		final Resolver resolver =
				object.value().partResolver(ValuePart.PROPOSITION);

		what().resolveValues(resolver);
		with().resolveValues(resolver);
	}

	@Override
	public ValOp writeBuiltin(ValDirs dirs, HostOp host) {

		final ValDirs whatDirs = dirs.dirs().value(ValueType.STRING, "what");
		final ValOp whatVal = what().op(host).writeValue(whatDirs);

		final ValDirs withDirs =
			whatDirs.dirs().value(ValueType.STRING, "with");
		final ValOp withVal = with().op(host).writeValue(withDirs);

		final Code code = withDirs.code();
		final FuncPtr<ConcatFunc> funcPtr =
			code.getGenerator().externalFunction(
					"o42a_str_concat",
					ConcatFunc.CONCAT);
		final ConcatFunc func = funcPtr.op(null, code);
		final ValOp result = dirs.value();

		func.concat(code, result, whatVal, withVal);

		withDirs.done();
		whatDirs.done();

		return result;
	}

	private Ref what() {
		if (this.what != null) {
			return this.what;
		}

		final MemberKey fromKey = field("what", Accessor.DECLARATION).getKey();

		return this.what = fromKey.toPath().target(this, distribute());
	}

	private Ref with() {
		if (this.with != null) {
			return this.with;
		}

		final MemberKey toKey = field("with", Accessor.DECLARATION).getKey();

		return this.with = toKey.toPath().target(this, distribute());
	}

}

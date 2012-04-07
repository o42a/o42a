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

import static org.o42a.intrinsic.string.ConcatFunc.CONCAT;

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
import org.o42a.util.fn.Cancelable;


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

		if (whatValue.getKnowledge().isFalse()
				|| withValue.getKnowledge().isFalse()) {
			return ValueType.STRING.falseValue();
		}
		if (!whatValue.getKnowledge().isKnown()
				|| !withValue.getKnowledge().isKnown()) {
			return ValueType.STRING.runtimeValue();
		}

		final String what =
				ValueType.STRING.cast(whatValue).getCompilerValue();
		final String with =
				ValueType.STRING.cast(withValue).getCompilerValue();

		return ValueType.STRING.constantValue(what + with);
	}

	@Override
	public void resolveBuiltin(Resolver resolver) {
		what().resolve(resolver).resolveValue();
		with().resolve(resolver).resolveValue();
	}

	@Override
	public InlineValue inlineBuiltin(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct,
			Scope origin) {

		final InlineValue whatValue = what().inline(normalizer, origin);
		final InlineValue withValue = with().inline(normalizer, origin);

		if (whatValue == null || withValue == null) {
			return null;
		}

		return new Inline(valueStruct, whatValue, withValue);
	}

	@Override
	public ValOp writeBuiltin(ValDirs dirs, HostOp host) {
		return write(dirs, host, what().op(host), null, with().op(host), null);
	}

	private Ref what() {
		if (this.what != null) {
			return this.what;
		}

		final Path path =
				field("what", Accessor.DECLARATION)
				.getKey()
				.toPath()
				.dereference();

		return this.what = path.bind(this, getScope()).target(distribute());
	}

	private Ref with() {
		if (this.with != null) {
			return this.with;
		}

		final Path path =
				field("with", Accessor.DECLARATION)
				.getKey()
				.toPath()
				.dereference();

		return this.with = path.bind(this, getScope()).target(distribute());
	}

	private static ValOp write(
			ValDirs dirs,
			HostOp host,
			RefOp what,
			InlineValue inlineWhat,
			RefOp with,
			InlineValue inlineWith) {
		final ValDirs whatDirs = dirs.dirs().value(ValueStruct.STRING, "what");
		final ValOp whatVal;

		if (inlineWhat != null) {
			whatVal = inlineWhat.writeValue(whatDirs, host);
		} else {
			whatVal = what.writeValue(whatDirs);
		}

		final ValDirs withDirs =
				whatDirs.dirs().value(ValueStruct.STRING, "with");
		final ValOp withVal;

		if (inlineWith != null) {
			withVal = inlineWith.writeValue(withDirs, host);
		} else {
			withVal = with.writeValue(withDirs);
		}

		final Code code = withDirs.code();
		final FuncPtr<ConcatFunc> funcPtr =
				code.getGenerator()
				.externalFunction()
				.link("o42a_str_concat", CONCAT);
		final ConcatFunc func = funcPtr.op(null, code);
		final ValOp result = dirs.value();

		func.concat(code, result, whatVal, withVal);

		withDirs.done();
		whatDirs.done();

		return result;
	}

	private static final class Inline extends InlineValue {

		private final InlineValue whatValue;
		private final InlineValue withValue;

		Inline(
				ValueStruct<?, ?> valueStruct,
				InlineValue whatValue,
				InlineValue withValue) {
			super(null, valueStruct);
			this.whatValue = whatValue;
			this.withValue = withValue;
		}

		@Override
		public ValOp writeValue(ValDirs dirs, HostOp host) {
			return write(dirs, host, null, this.whatValue, null, this.withValue);
		}

		@Override
		public String toString() {
			if (this.withValue == null) {
				return super.toString();
			}
			return "(" + this.whatValue + "+" + this.withValue + ")";
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

}

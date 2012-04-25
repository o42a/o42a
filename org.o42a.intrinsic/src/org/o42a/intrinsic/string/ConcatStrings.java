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

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.FuncPtr;
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
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.object.Accessor;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
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
	public InlineEval inlineBuiltin(Normalizer normalizer, Scope origin) {

		final InlineValue inlineWhat = what().inline(normalizer, origin);
		final InlineValue inlineWith = with().inline(normalizer, origin);

		if (inlineWhat == null || inlineWith == null) {
			return null;
		}

		return new SubstringEval(this, inlineWhat, inlineWith);
	}

	@Override
	public Eval evalBuiltin() {
		return new SubstringEval(this, null, null);
	}

	@Override
	public String toString() {
		if (this.what == null || this.with == null) {
			return super.toString();
		}
		return "(" + this.what + " + " + this.with + ")";
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

	private void write(
			DefDirs dirs,
			HostOp host,
			InlineValue inlineWhat,
			InlineValue inlineWith) {

		final ValDirs whatDirs = dirs.dirs().value(ValueStruct.STRING, "what");
		final ValOp whatVal;

		if (inlineWhat != null) {
			whatVal = inlineWhat.writeValue(whatDirs, host);
		} else {
			whatVal = what().op(host).writeValue(whatDirs);
		}

		final ValDirs withDirs =
				whatDirs.dirs().value(ValueStruct.STRING, "with");
		final ValOp withVal;

		if (inlineWith != null) {
			withVal = inlineWith.writeValue(withDirs, host);
		} else {
			withVal = with().op(host).writeValue(withDirs);
		}

		final Block code = withDirs.code();
		final FuncPtr<ConcatFunc> funcPtr =
				code.getGenerator()
				.externalFunction()
				.link("o42a_str_concat", CONCAT);
		final ConcatFunc func = funcPtr.op(null, code);
		final ValOp result = dirs.value();

		func.concat(code, result, whatVal, withVal);

		result.go(code, withDirs);
		dirs.returnValue(code, result);
		withDirs.done();
		whatDirs.done();
	}

	private static final class SubstringEval extends InlineEval {

		private final ConcatStrings concat;
		private final InlineValue inlineWhat;
		private final InlineValue inlineWith;

		SubstringEval(
				ConcatStrings concat,
				InlineValue inlineWhat,
				InlineValue inlineWith) {
			super(null);
			this.concat = concat;
			this.inlineWhat = inlineWhat;
			this.inlineWith = inlineWith;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {
			this.concat.write(dirs, host, this.inlineWhat, this.inlineWith);
		}

		@Override
		public String toString() {
			if (this.concat == null) {
				return super.toString();
			}
			if (this.inlineWhat == null) {
				return this.concat.toString();
			}
			return "(" + this.inlineWhat + "+" + this.inlineWith + ")";
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

}

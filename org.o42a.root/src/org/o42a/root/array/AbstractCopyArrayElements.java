/*
    Root Object Definition
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.root.array;

import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;
import static org.o42a.core.member.MemberIdKind.FIELD_NAME;
import static org.o42a.root.array.CopyArrayElementsFn.COPY_ARRAY_ELEMENTS;
import static org.o42a.util.string.Capitalization.CASE_INSENSITIVE;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.common.builtin.AnnotatedBuiltin;
import org.o42a.common.object.AnnotatedSources;
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
import org.o42a.core.value.array.ArrayValueType;
import org.o42a.util.fn.Cancelable;


abstract class AbstractCopyArrayElements extends AnnotatedBuiltin {

	private static final MemberName FROM_MEMBER =
			FIELD_NAME.memberName(CASE_INSENSITIVE.canonicalName("from"));
	private static final MemberName TO_MEMBER =
			FIELD_NAME.memberName(CASE_INSENSITIVE.canonicalName("to"));
	private static final MemberName TARGET_MEMBER =
			FIELD_NAME.memberName(CASE_INSENSITIVE.canonicalName("target"));
	private static final MemberName TARGET_START_MEMBER =
			FIELD_NAME.memberName(
					CASE_INSENSITIVE.canonicalName("target start"));

	private Ref source;
	private Ref from;
	private Ref to;
	private Ref target;
	private Ref targetStart;

	AbstractCopyArrayElements(Obj owner, AnnotatedSources sources) {
		super(owner, sources);
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {
		return type().getParameters().runtimeValue();
	}

	@Override
	public void resolveBuiltin(FullResolver resolver) {
		source().resolveAll(resolver);
		from().resolveAll(resolver);
		to().resolveAll(resolver);
		target().resolveAll(resolver);
		targetStart().resolveAll(resolver);
	}

	@Override
	public InlineEval inlineBuiltin(Normalizer normalizer, Scope origin) {

		final InlineValue inlineSource = source().inline(normalizer, origin);
		final InlineValue inlineFrom = from().inline(normalizer, origin);
		final InlineValue inlineTo = to().inline(normalizer, origin);
		final InlineValue inlineTarget = target().inline(normalizer, origin);
		final InlineValue inlineTargetStart =
				targetStart().inline(normalizer, origin);

		if (inlineSource == null
				|| inlineFrom == null
				|| inlineTo == null
				|| inlineTarget == null
				|| inlineTargetStart == null) {
			return null;
		}

		return new CopyArrayElementsEval(
				this,
				inlineSource,
				inlineFrom,
				inlineTo,
				inlineTarget,
				inlineTargetStart);
	}

	@Override
	public Eval evalBuiltin() {
		return new CopyArrayElementsEval(this, null, null, null, null, null);
	}

	private final Ref source() {
		if (this.source != null) {
			return this.source;
		}

		final Path path = getScope().getEnclosingScopePath();

		return this.source = path.bind(this, getScope()).target(distribute());
	}

	private final Ref from() {
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

	private final Ref to() {
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

	private final Ref target() {
		if (this.target != null) {
			return this.target;
		}

		final Path path =
				member(TARGET_MEMBER, Accessor.DECLARATION)
				.getMemberKey()
				.toPath()
				.dereference();

		return this.target = path.bind(this, getScope()).target(distribute());
	}

	private final Ref targetStart() {
		if (this.targetStart != null) {
			return this.targetStart;
		}

		final Path path =
				member(TARGET_START_MEMBER, Accessor.DECLARATION)
				.getMemberKey()
				.toPath()
				.dereference();

		return this.targetStart =
				path.bind(this, getScope()).target(distribute());
	}

	private void write(
			DefDirs dirs,
			HostOp host,
			InlineValue inlineSource,
			InlineValue inlineFrom,
			InlineValue inlineTo,
			InlineValue inlineTarget,
			InlineValue inlineTargetStart) {

		final ValDirs sourceDirs =
				dirs.dirs()
				.nested()
				.value("source", source().getValueType(), TEMP_VAL_HOLDER);
		final ValOp sourceVal = eval(sourceDirs, host, source(), inlineSource);

		final ValDirs fromDirs =
				sourceDirs.dirs()
				.nested()
				.value("from", ValueType.INTEGER, TEMP_VAL_HOLDER);
		final ValOp fromVal = eval(fromDirs, host, from(), inlineFrom);

		final ValDirs toDirs =
				fromDirs.dirs()
				.nested()
				.value("to", ValueType.INTEGER, TEMP_VAL_HOLDER);
		final ValOp toVal = eval(toDirs, host, to(), inlineTo);

		final ValDirs targetDirs =
				toDirs.dirs()
				.nested()
				.value("target", ArrayValueType.ARRAY, TEMP_VAL_HOLDER);
		final ValOp targetVal = eval(targetDirs, host, target(), inlineTarget);

		final ValDirs targetStartDirs =
				targetDirs.dirs()
				.nested()
				.value("target_start", ValueType.INTEGER, TEMP_VAL_HOLDER);
		final ValOp targetStartVal = eval(
				targetStartDirs,
				host,
				targetStart(),
				inlineTargetStart);

		final Block code = targetStartDirs.code();
		final FuncPtr<CopyArrayElementsFn> func =
				dirs.getGenerator().externalFunction().link(
						"o42a_array_copy_elements",
						COPY_ARRAY_ELEMENTS);

		func.op(null, code).copyElements(
				targetStartDirs.dirs(),
				sourceVal,
				fromVal.rawValue(null, code).load(null, code),
				toVal.rawValue(null, code).load(null, code),
				targetVal,
				targetStartVal.rawValue(null, code).load(null, code));
		dirs.returnValue(code, dirs.getBuilder().voidVal(code));

		targetStartDirs.done();
		targetDirs.done();
		toDirs.done();
		fromDirs.done();
		sourceDirs.done();
	}

	private ValOp eval(
			ValDirs dirs,
			HostOp host,
			Ref value,
			InlineValue inlineValue) {
		if (inlineValue != null) {
			return inlineValue.writeValue(dirs, host);
		}
		return value.op(host).writeValue(dirs);
	}

	private static final class CopyArrayElementsEval extends InlineEval {

		private final AbstractCopyArrayElements copy;
		private final InlineValue inlineSource;
		private final InlineValue inlineFrom;
		private final InlineValue inlineTo;
		private final InlineValue inlineTarget;
		private final InlineValue inlineTargetStart;

		CopyArrayElementsEval(
				AbstractCopyArrayElements copy,
				InlineValue inlineSource,
				InlineValue inlineFrom,
				InlineValue inlineTo,
				InlineValue inlineTarget,
				InlineValue inlineTargetStart) {
			super(null);
			this.copy = copy;
			this.inlineSource = inlineSource;
			this.inlineFrom = inlineFrom;
			this.inlineTo = inlineTo;
			this.inlineTarget = inlineTarget;
			this.inlineTargetStart = inlineTargetStart;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {
			this.copy.write(
					dirs,
					host,
					this.inlineSource,
					this.inlineFrom,
					this.inlineTo,
					this.inlineTarget,
					this.inlineTargetStart);
		}

		@Override
		public String toString() {
			if (this.copy == null) {
				return super.toString();
			}
			return this.copy.toString();
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

}

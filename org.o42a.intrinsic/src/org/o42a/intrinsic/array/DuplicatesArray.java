/*
    Intrinsics
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.intrinsic.array;

import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;
import static org.o42a.core.member.MemberName.fieldName;
import static org.o42a.intrinsic.array.ArrayOfDuplicatesFunc.ARRAY_OF_DUPLICATES;
import static org.o42a.util.string.Capitalization.CASE_INSENSITIVE;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.Int32op;
import org.o42a.common.builtin.AnnotatedBuiltin;
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
import org.o42a.core.member.*;
import org.o42a.core.ref.*;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.intrinsic.root.Root;
import org.o42a.util.fn.Cancelable;


@SourcePath(relativeTo = Root.class, value = "duplicates_array.o42a")
public class DuplicatesArray extends AnnotatedBuiltin {

	private static final MemberName SIZE =
			fieldName(CASE_INSENSITIVE.canonicalName("size"));
	private static final MemberName DUPLICATE =
			fieldName(CASE_INSENSITIVE.canonicalName("duplicate"));

	private Ref size;
	private Ref duplicate;

	public DuplicatesArray(MemberOwner owner, AnnotatedSources sources) {
		super(owner, sources);
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {
		return type().getParameters()
				.upgradeScope(resolver.getScope())
				.runtimeValue();
	}

	@Override
	public void resolveBuiltin(FullResolver resolver) {
		size().resolveAll(resolver);
		duplicate().resolveAll(resolver);
	}

	@Override
	public InlineEval inlineBuiltin(Normalizer normalizer, Scope origin) {

		final InlineValue inlineSize = size().inline(normalizer, origin);
		final InlineValue inlineDuplicate =
				duplicate().inline(normalizer, origin);

		if (inlineSize == null || inlineDuplicate == null) {
			return null;
		}

		return new DuplicatesArrayEval(this, inlineSize, inlineDuplicate);
	}

	@Override
	public Eval evalBuiltin() {
		return new DuplicatesArrayEval(this, null, null);
	}

	private Ref size() {
		if (this.size != null) {
			return this.size;
		}

		final MemberKey key = SIZE.key(getScope());
		final Member member = member(key);

		return this.size =
				key.toPath()
				.dereference()
				.bind(member, getScope())
				.target(distribute());
	}

	private Ref duplicate() {
		if (this.duplicate != null) {
			return this.duplicate;
		}

		final MemberKey key = DUPLICATE.key(getScope());
		final Member member = member(key);

		return this.duplicate =
				key.toPath()
				.bind(member, getScope())
				.target(distribute());
	}

	private void write(
			DefDirs dirs,
			HostOp host,
			InlineValue inlineSize,
			InlineValue inlineDuplicate) {

		final ValDirs sizeDirs = dirs.dirs().nested().value(
				"size",
				ValueType.INTEGER,
				TEMP_VAL_HOLDER);
		final ValOp sizeVal;

		if (inlineSize != null) {
			sizeVal = inlineSize.writeValue(sizeDirs, host);
		} else {
			sizeVal = size().op(host).writeValue(sizeDirs);
		}

		final Block noDuplicate = sizeDirs.addBlock("no_duplicate");
		final ValDirs dupDirs =
				sizeDirs.dirs()
				.setFalseDir(noDuplicate.head())
				.value(
						"duplicate",
						duplicate().getValueType(),
						TEMP_VAL_HOLDER);
		final ValOp dupVal;

		if (inlineDuplicate != null) {
			dupVal = inlineDuplicate.writeValue(dupDirs, host);
		} else {
			dupVal = duplicate().op(host).writeValue(dupDirs);
		}

		final Block code = dupDirs.code();
		final AnyOp foundDuplicate =
				dupVal.value(null, code).toPtr(null, code).load(null, code);
		final AnyOp duplicate;

		if (!noDuplicate.exists()) {
			duplicate = foundDuplicate;
		} else {

			final AnyOp nullDuplicate = noDuplicate.nullPtr();

			noDuplicate.go(code.tail());

			duplicate = code.phi(null, foundDuplicate, nullDuplicate);
		}

		final FuncPtr<ArrayOfDuplicatesFunc> func =
				dirs.getGenerator().externalFunction().link(
						"o42a_array_of_duplicates",
						ARRAY_OF_DUPLICATES);
		final ValOp result = dirs.value();
		final Int32op size =
				sizeVal.rawValue(null, code)
				.load(null, code)
				.toInt32(null, code);

		func.op(null, code).create(
				dupDirs.dirs(),
				result,
				size,
				duplicate.toData(null, code));
		result.holder().set(code);

		dirs.returnValue(result);

		dupDirs.done();
		sizeDirs.done();
	}

	private static final class DuplicatesArrayEval extends InlineEval {

		private final DuplicatesArray array;
		private final InlineValue inlineSize;
		private final InlineValue inlineDuplicate;

		DuplicatesArrayEval(
				DuplicatesArray array,
				InlineValue inlineSize,
				InlineValue inlineDuplicate) {
			super(null);
			this.array = array;
			this.inlineSize = inlineSize;
			this.inlineDuplicate = inlineDuplicate;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {
			this.array.write(dirs, host, this.inlineSize, this.inlineDuplicate);
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

}

/*
    Root Object Definition
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import static org.o42a.common.macro.Macros.expandMacro;
import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;
import static org.o42a.core.member.MemberIdKind.FIELD_NAME;
import static org.o42a.core.ref.RefUsage.TYPE_PARAMETER_REF_USAGE;
import static org.o42a.core.value.array.ArrayValueType.ARRAY;
import static org.o42a.root.array.ArrayOfDuplicatesFunc.ARRAY_OF_DUPLICATES;
import static org.o42a.util.string.Capitalization.CASE_INSENSITIVE;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.Int32op;
import org.o42a.common.builtin.AnnotatedBuiltin;
import org.o42a.common.macro.type.TypeParamMacroDep;
import org.o42a.common.macro.type.TypeParameterMemberKey;
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
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.MemberName;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.array.Array;
import org.o42a.root.Root;
import org.o42a.util.fn.Cancelable;


@SourcePath(relativeTo = Root.class, value = "duplicates.o42a")
public class Duplicates extends AnnotatedBuiltin {

	private static final MemberName SIZE =
			FIELD_NAME.memberName(CASE_INSENSITIVE.canonicalName("size"));
	private static final MemberName DUPLICATE =
			FIELD_NAME.memberName(CASE_INSENSITIVE.canonicalName("duplicate"));

	private Ref size;
	private Ref duplicate;
	private TypeParameters<Array> typeParameters;

	public Duplicates(Obj owner, AnnotatedSources sources) {
		super(owner, sources);
	}

	@Override
	public TypeParameters<Array> getBuiltinTypeParameters() {
		if (this.typeParameters != null) {
			return this.typeParameters;
		}

		final MemberKey itemTypeKey =
				ARRAY.itemTypeKey(getContext().getIntrinsics());
		final BoundPath itemTypePath =
				expandMacro(itemTypeKey.toPath().bind(this, getScope()));
		final TypeParamMacroDep macroDep = new TypeParamMacroDep(
				null,
				new TypeParameterMemberKey(itemTypeKey),
				0);
		final TypeRef itemTypeRef =
				itemTypePath.target(distribute()).consume(macroDep).toTypeRef();

		return this.typeParameters = ARRAY.typeParameters(itemTypeRef);
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {
		return getBuiltinTypeParameters()
				.upgradeScope(resolver.getScope())
				.runtimeValue();
	}

	@Override
	public void resolveBuiltin(FullResolver resolver) {
		getBuiltinTypeParameters().resolveAll(
				resolver.setRefUsage(TYPE_PARAMETER_REF_USAGE));
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
		final ValOp sizeVal = eval(sizeDirs, host, size(), inlineSize);

		final ValDirs dupDirs =
				sizeDirs.dirs()
				.nested()
				.value(
						"duplicate",
						duplicate().getValueType(),
						TEMP_VAL_HOLDER);
		final Block code = dupDirs.code();
		final AnyOp duplicate = evalDup(dupDirs, host, inlineDuplicate);

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
		result.holder().hold(code);

		dirs.returnValue(code, result);

		dupDirs.done();
		sizeDirs.done();
	}

	private AnyOp evalDup(
			ValDirs dupDirs,
			HostOp host,
			InlineValue inlineDuplicate) {

		final Block noDuplicate = dupDirs.addBlock("no_duplicate");
		final ValDirs dirs =
				dupDirs.dirs()
				.setFalseDir(noDuplicate.head())
				.value(dupDirs);
		final ValOp dupVal = eval(
				dirs,
				host,
				duplicate(),
				inlineDuplicate);

		final Block code = dirs.code();
		final AnyOp foundDuplicate =
				dupVal.value(null, code)
				.toRec(null, code)
				.load(null, code);
		final AnyOp duplicate;

		if (!noDuplicate.exists()) {
			duplicate = foundDuplicate;
		} else {

			final AnyOp nullDuplicate = noDuplicate.nullPtr();

			noDuplicate.go(code.tail());
			duplicate = code.phi(null, foundDuplicate, nullDuplicate);
		}

		dirs.done();

		return duplicate;
	}

	private static ValOp eval(
			ValDirs dirs,
			HostOp host,
			Ref value,
			InlineValue inlineValue) {
		if (inlineValue != null) {
			return inlineValue.writeValue(dirs, host);
		}
		return value.op(host).writeValue(dirs);
	}

	private static final class DuplicatesArrayEval extends InlineEval {

		private final Duplicates array;
		private final InlineValue inlineSize;
		private final InlineValue inlineDuplicate;

		DuplicatesArrayEval(
				Duplicates array,
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

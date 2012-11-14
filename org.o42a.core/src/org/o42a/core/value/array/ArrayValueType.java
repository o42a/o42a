/*
    Compiler Core
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
package org.o42a.core.value.array;

import static org.o42a.core.ref.RefUsage.TYPE_REF_USAGE;

import org.o42a.codegen.Generator;
import org.o42a.core.ir.value.array.ArrayValueTypeIR;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.FullResolver;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.Intrinsics;
import org.o42a.core.value.*;
import org.o42a.core.value.link.LinkValueType;


public class ArrayValueType extends ValueType<ArrayValueStruct, Array> {

	public static final ArrayValueType ROW = new ArrayValueType(false);
	public static final ArrayValueType ARRAY = new ArrayValueType(true);

	private final boolean variable;
	private ArrayValueTypeIR ir;

	private ArrayValueType(boolean variable) {
		super(variable ? "array" : "row", Array.class);
		this.variable = variable;
	}

	@Override
	public boolean isStateful() {
		return isVariable();
	}

	@Override
	public final boolean isVariable() {
		return this.variable;
	}

	public final MemberKey itemTypeKey(Intrinsics intrinsics) {
		return ROW.typeObject(intrinsics).toMember().getMemberKey();
	}

	public final TypeParameters<Array> typeParameters(TypeRef itemTypeRef) {

		final MemberKey itemTypeKey =
				itemTypeKey(itemTypeRef.getContext().getIntrinsics());

		return TypeParameters.typeParameters(itemTypeRef, this)
				.add(itemTypeKey, itemTypeRef);
	}

	public final TypeRef itemTypeRef(TypeParameters<?> parameters) {

		final TypeParameters<Array> arrayParameters = cast(parameters);
		final MemberKey itemTypeKey = itemTypeKey(
				parameters.getContext().getIntrinsics());

		return arrayParameters.typeRef(itemTypeKey);
	}

	public final ArrayValueStruct arrayStruct(TypeRef itemTypeRef) {
		return new ArrayValueStruct(this, itemTypeRef);
	}

	@Override
	public Obj typeObject(Intrinsics intrinsics) {
		if (isVariable()) {
			return intrinsics.getArray();
		}
		return intrinsics.getRow();
	}

	@Override
	public Path path(Intrinsics intrinsics) {

		final Obj array = typeObject(intrinsics);

		return Path.ROOT_PATH.append(array.getScope().toField().getKey());
	}

	@Override
	public boolean convertibleFrom(ValueType<?, ?> other) {
		return other.isArray();
	}

	public ArrayValueType setVariable(boolean variable) {
		if (isVariable() == variable) {
			return this;
		}
		return variable ? ArrayValueType.ARRAY : ArrayValueType.ROW;
	}

	@Override
	public final LinkValueType toLinkType() {
		return null;
	}

	@Override
	public final ArrayValueType toArrayType() {
		return this;
	}

	public final ArrayValueTypeIR ir(Generator generator) {
		if (this.ir != null && this.ir.getGenerator() == generator) {
			return this.ir;
		}
		return this.ir = new ArrayValueTypeIR(generator, this);
	}

	@Override
	protected ValueKnowledge valueKnowledge(Array value) {
		return value.getValueKnowledge();
	}

	@Override
	protected Value<Array> prefixValueWith(
			Value<Array> value,
			PrefixPath prefix) {
		if (value.getKnowledge().hasCompilerValue()) {

			final Array array = value.getCompilerValue();

			if (prefix.emptyFor(array)) {
				return value;
			}

			return array.prefixWith(prefix).toValue();
		}

		final ArrayValueStruct initialStruct =
				(ArrayValueStruct) value.getValueStruct();
		final ArrayValueStruct rescopedStruct =
				initialStruct.prefixWith(prefix);

		if (initialStruct == rescopedStruct) {
			return value;
		}
		if (!value.getKnowledge().isKnownToCompiler()) {
			return rescopedStruct.runtimeValue();
		}

		return rescopedStruct.falseValue();
	}

	@Override
	protected void resolveAll(Value<Array> value, FullResolver resolver) {
		itemTypeRef(value.getTypeParameters())
		.resolveAll(resolver.setRefUsage(TYPE_REF_USAGE));
		if (value.getKnowledge().hasCompilerValue()) {

			final ArrayItem[] items =
					value.getCompilerValue().items(resolver.getScope());

			for (ArrayItem item : items) {
				item.resolveAll(resolver);
			}
		}
	}

}

/*
    Compiler Core
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
package org.o42a.core.value.array;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.ref.RefUsage.TYPE_REF_USAGE;
import static org.o42a.core.source.Intrinsic.intrInit;
import static org.o42a.core.st.sentence.BlockBuilder.valueBlock;
import static org.o42a.core.value.ValueEscapeMode.VALUE_ESCAPE_POSSIBLE;
import static org.o42a.core.value.array.impl.ArrayValueIRDesc.ARRAY_VALUE_IR_DESC;
import static org.o42a.util.string.Capitalization.CASE_INSENSITIVE;

import org.o42a.codegen.Generator;
import org.o42a.core.ir.value.array.ArrayIRGenerator;
import org.o42a.core.ir.value.type.ValueIRDesc;
import org.o42a.core.ir.value.type.ValueTypeIR;
import org.o42a.core.member.MemberIdKind;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.MemberName;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;
import org.o42a.core.object.value.Statefulness;
import org.o42a.core.ref.FullResolver;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.Call;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.Intrinsic;
import org.o42a.core.source.Intrinsics;
import org.o42a.core.value.*;
import org.o42a.core.value.array.impl.ArrayStaticsIR;
import org.o42a.core.value.array.impl.ArrayValueTypeIR;
import org.o42a.core.value.link.LinkValueType;
import org.o42a.util.fn.CondInit;


public class ArrayValueType extends ValueType<Array> {

	public static final ArrayValueType ROW = new ArrayValueType(false);
	public static final ArrayValueType ARRAY = new ArrayValueType(true);

	private static final MemberName INDEXED_ID =
			MemberIdKind.FIELD_NAME.memberName(
					CASE_INSENSITIVE.canonicalName("indexed"));
	private static final MemberName ITEM_TYPE_ID =
			MemberIdKind.FIELD_NAME.memberName(
					CASE_INSENSITIVE.canonicalName("item type"));

	private final boolean variable;
	private final ArrayValueConverter converter;
	private final CondInit<Intrinsics, Intrinsic<MemberKey>> itemTypeKey =
			intrInit(this::findItemKey);

	private ArrayValueType(boolean variable) {
		super(variable ? "array" : "row", Array.class);
		this.variable = variable;
		this.converter = new ArrayValueConverter(this);
	}

	@Override
	public Statefulness getDefaultStatefulness() {
		return isVariable() ? Statefulness.STATEFUL : Statefulness.STATELESS;
	}

	@Override
	public final boolean isVariable() {
		return this.variable;
	}

	@Override
	public ValueEscapeMode valueEscapeMode() {
		return VALUE_ESCAPE_POSSIBLE;
	}

	public final MemberKey itemTypeKey(Intrinsics intrinsics) {
		return this.itemTypeKey.get(intrinsics).get();
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

	@Override
	public final ValueIRDesc irDesc() {
		return ARRAY_VALUE_IR_DESC;
	}

	public final ArrayIRGenerator irGenerator(Generator generator) {
		return (ArrayStaticsIR) ir(generator).staticsIR();
	}

	@Override
	protected ValueConverter<Array> getConverter() {
		return this.converter;
	}

	@Override
	protected ValueAdapter defaultAdapter(
			Ref ref,
			TypeParameters<Array> parameters,
			ValueRequest request) {
		if (request.getExpectedParameters().convertibleFrom(parameters)) {
			return new ArrayValueAdapter(
					ref,
					request.getExpectedParameters().toArrayParameters());
		}
		return super.defaultAdapter(ref, parameters, request);
	}

	@Override
	protected Ref adapterRef(
			Ref ref,
			TypeRef expectedTypeRef,
			CompilerLogger logger) {

		final ArrayValueType expectedType =
				expectedTypeRef.getType().type().getValueType().toArrayType();

		if (expectedType == null || !expectedType.convertibleFrom(this)) {
			return super.adapterRef(ref, expectedTypeRef, logger);
		}
		if (expectedType.is(this)) {
			return super.adapterRef(ref, expectedTypeRef, logger);
		}

		final AscendantsDefinition ascendants = new AscendantsDefinition(
				ref,
				ref.distribute(),
				expectedType.typeRef(ref, ref.getScope()));
		final ObjectTypeParameters convertedParameters =
				ref.typeParameters(ref.getScope())
				.toArrayParameters()
				.convertTo(expectedType)
				.toObjectTypeParameters();
		final Call arrayConstructor = new Call(
				ref,
				ref.distribute(),
				ascendants.setTypeParameters(convertedParameters),
				valueBlock(ref)::definitions);

		return arrayConstructor.toRef();
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

		final TypeParameters<Array> initialParams =
				value.getTypeParameters().toArrayParameters();
		final TypeParameters<Array> rescopedParams =
				initialParams.prefixWith(prefix);

		if (initialParams == rescopedParams) {
			return value;
		}
		if (!value.getKnowledge().isKnownToCompiler()) {
			return rescopedParams.runtimeValue();
		}

		return rescopedParams.falseValue();
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

	@Override
	protected ValueTypeIR<Array> createIR(Generator generator) {
		return new ArrayValueTypeIR(generator, this);
	}

	private MemberKey findItemKey(Intrinsics intrinsics) {

		final Field indexed =
				intrinsics.getRoot()
				.member(INDEXED_ID)
				.toField()
				.field(dummyUser());

		return ITEM_TYPE_ID.key(indexed);
	}

}

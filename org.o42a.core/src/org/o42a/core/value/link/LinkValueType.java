/*
    Compiler Core
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
package org.o42a.core.value.link;

import static org.o42a.core.ref.RefUsage.TYPE_REF_USAGE;
import static org.o42a.core.ref.path.Path.ROOT_PATH;
import static org.o42a.core.source.Intrinsic.intrInit;
import static org.o42a.core.value.ValueEscapeMode.VALUE_ESCAPE_POSSIBLE;
import static org.o42a.core.value.link.impl.LinkTargetEscapeMode.LINK_TARGET_ESCAPE_MODE;
import static org.o42a.core.value.link.impl.LinkValueIRDesc.LINK_VALUE_IR_DESC;
import static org.o42a.util.string.Capitalization.CASE_INSENSITIVE;

import org.o42a.codegen.Generator;
import org.o42a.core.ir.value.type.ValueIRDesc;
import org.o42a.core.ir.value.type.ValueTypeIR;
import org.o42a.core.member.MemberIdKind;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.MemberName;
import org.o42a.core.object.Obj;
import org.o42a.core.object.value.Statefulness;
import org.o42a.core.ref.FullResolver;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.Intrinsic;
import org.o42a.core.source.Intrinsics;
import org.o42a.core.value.*;
import org.o42a.core.value.array.ArrayValueType;
import org.o42a.core.value.link.impl.LinkByValueAdapter;
import org.o42a.core.value.link.impl.LinkValueConverter;
import org.o42a.core.value.link.impl.LinkValueTypeIR;
import org.o42a.util.fn.CondInit;


public abstract class LinkValueType extends ValueType<KnownLink> {

	public static final LinkValueType LINK = new LinkValueType("link") {

		@Override
		public ValueEscapeMode valueEscapeMode() {
			return LINK_TARGET_ESCAPE_MODE;
		}

		@Override
		public Obj typeObject(Intrinsics intrinsics) {
			return intrinsics.getLink();
		}

	};

	public static final LinkValueType VARIABLE = new LinkValueType("variable") {

		@Override
		public ValueEscapeMode valueEscapeMode() {
			return VALUE_ESCAPE_POSSIBLE;
		}

		@Override
		public Obj typeObject(Intrinsics intrinsics) {
			return intrinsics.getVariable();
		}

	};

	private static final MemberName INTERFACE_ID =
			MemberIdKind.FIELD_NAME.memberName(
					CASE_INSENSITIVE.canonicalName("interface"));

	private final LinkValueConverter converter;
	private final CondInit<Intrinsics, Intrinsic<MemberKey>> interfaceKey =
			intrInit(ics -> INTERFACE_ID.key(typeObject(ics).getScope()));

	private LinkValueType(String systemId) {
		super(systemId, KnownLink.class);
		this.converter = new LinkValueConverter(this);
	}

	@Override
	public Statefulness getDefaultStatefulness() {
		return isVariable() ? Statefulness.VARIABLE : Statefulness.STATELESS;
	}

	@Override
	public final boolean isVariable() {
		return is(VARIABLE);
	}

	public final MemberKey interfaceKey(Intrinsics intrinsics) {
		return this.interfaceKey.get(intrinsics).get();
	}

	public TypeParameters<KnownLink> typeParameters(TypeRef interfaceRef) {

		final MemberKey interfaceKey =
				interfaceKey(interfaceRef.getContext().getIntrinsics());

		return TypeParameters.typeParameters(interfaceRef, this)
				.add(interfaceKey, interfaceRef);
	}

	public TypeRef interfaceRef(TypeParameters<?> parameters) {

		final TypeParameters<KnownLink> linkParameters = cast(parameters);
		final MemberKey interfaceKey =
				interfaceKey(parameters.getContext().getIntrinsics());

		return linkParameters.typeRef(interfaceKey);
	}

	@Override
	public Path path(Intrinsics intrinsics) {

		final Obj link = typeObject(intrinsics);

		return ROOT_PATH.append(link.getScope().toField().getKey());
	}

	@Override
	public final LinkValueType toLinkType() {
		return this;
	}

	@Override
	public final ArrayValueType toArrayType() {
		return null;
	}

	@Override
	public final ValueIRDesc irDesc() {
		return LINK_VALUE_IR_DESC;
	}

	@Override
	protected ValueConverter<KnownLink> getConverter() {
		return this.converter;
	}

	@Override
	protected ValueAdapter defaultAdapter(
			Ref ref,
			TypeParameters<KnownLink> parameters,
			ValueRequest request) {

		final TypeParameters<?> expectedParameters =
				request.getExpectedParameters();

		if (expectedParameters.convertibleFrom(parameters)) {
			return ref.dereference().valueAdapter(request.noLinkToLink());
		}
		if (request.isLinkToLinkAllowed()) {

			final Ref adapterRef = adapterRef(
					ref,
					expectedParameters.getValueType()
					.typeRef(ref, ref.getScope()),
					request.getLogger());

			if (adapterRef != null) {
				return adapterRef.valueAdapter(request.noLinkToLink());
			}
		}

		final int depsDiff =
				expectedParameters.getLinkDepth()
				- parameters.getLinkDepth();

		if (depsDiff == 1) {

			final TypeParameters<KnownLink> expectedLinkParameters =
					expectedParameters.toLinkParameters();
			final Ref adapter = adapterRef(
					ref,
					expectedLinkParameters.getValueType()
					.toLinkType()
					.interfaceRef(expectedLinkParameters),
					request.getLogger());

			if (adapter != null) {
				return new LinkByValueAdapter(
						adapter,
						expectedLinkParameters);
			}
		}

		return ref.dereference().valueAdapter(request.noLinkToLink());
	}

	@Override
	protected ValueKnowledge valueKnowledge(KnownLink value) {
		return value.getKnowledge();
	}

	@Override
	protected Value<KnownLink> prefixValueWith(
			Value<KnownLink> value,
			PrefixPath prefix) {
		if (value.getKnowledge().hasCompilerValue()) {

			final KnownLink link = value.getCompilerValue();

			if (prefix.emptyFor(link)) {
				return value;
			}

			return link.prefixWith(prefix).toValue();
		}

		final TypeParameters<KnownLink> initialParams =
				cast(value).getTypeParameters();
		final TypeParameters<KnownLink> rescopedParams =
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
	protected void resolveAll(Value<KnownLink> value, FullResolver resolver) {
		interfaceRef(value.getTypeParameters())
		.resolveAll(resolver.setRefUsage(TYPE_REF_USAGE));
		if (value.getKnowledge().hasCompilerValue()) {
			value.getCompilerValue().resolveAll(resolver);
		}
	}

	@Override
	protected ValueTypeIR<KnownLink> createIR(Generator generator) {
		return new LinkValueTypeIR(generator, this);
	}

}

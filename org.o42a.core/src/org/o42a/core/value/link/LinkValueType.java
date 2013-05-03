/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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

import static org.o42a.core.member.MemberName.fieldName;
import static org.o42a.core.ref.RefUsage.TYPE_REF_USAGE;
import static org.o42a.core.ref.path.Path.ROOT_PATH;
import static org.o42a.core.value.link.impl.LinkValueIRDesc.LINK_VALUE_IR_DESC;
import static org.o42a.util.string.Capitalization.CASE_INSENSITIVE;

import org.o42a.codegen.Generator;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.ir.object.state.KeeperIR;
import org.o42a.core.ir.value.type.ValueIRDesc;
import org.o42a.core.ir.value.type.ValueTypeIR;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.MemberName;
import org.o42a.core.object.Obj;
import org.o42a.core.object.state.Keeper;
import org.o42a.core.ref.FullResolver;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.Intrinsics;
import org.o42a.core.value.*;
import org.o42a.core.value.array.ArrayValueType;
import org.o42a.core.value.link.impl.*;


public abstract class LinkValueType extends ValueType<KnownLink> {

	public static final LinkValueType LINK = new LinkValueType("link") {

		@Override
		public Obj typeObject(Intrinsics intrinsics) {
			return intrinsics.getLink();
		}

	};

	public static final LinkValueType VARIABLE = new LinkValueType("variable") {

		@Override
		public Obj typeObject(Intrinsics intrinsics) {
			return intrinsics.getVariable();
		}

	};

	private static final MemberName INTERFACE_ID =
			fieldName(CASE_INSENSITIVE.canonicalName("interface"));

	private final LinkValueConverter converter;
	private Intrinsics intrinsics;
	private MemberKey interfaceKey;

	private LinkValueType(String systemId) {
		super(systemId, KnownLink.class);
		this.converter = new LinkValueConverter(this);
	}

	@Override
	public boolean isStateful() {
		return isVariable();
	}

	@Override
	public final boolean isVariable() {
		return is(VARIABLE);
	}

	public final MemberKey interfaceKey(Intrinsics intrinsics) {
		if (this.interfaceKey != null && this.intrinsics == intrinsics) {
			return this.interfaceKey;
		}

		this.intrinsics = intrinsics;

		return this.interfaceKey =
				INTERFACE_ID.key(typeObject(intrinsics).getScope());
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

		if (!request.isLinkToLinkAllowed()
				|| expectedParameters.convertibleFrom(parameters)) {
			return new LinkValueAdapter(
					ref,
					expectedParameters != null
					? expectedParameters.toLinkParameters()
					: ref.typeParameters(ref.getScope()).toLinkParameters());
		}

		final int depsDiff =
				expectedParameters.getLinkDepth()
				- parameters.getLinkDepth();

		if (depsDiff == 0) {
			return ref.dereference().valueAdapter(request.noLinkToLink());
		}
		if (depsDiff == 1) {

			final TypeParameters<KnownLink> expectedLinkParameters =
					expectedParameters.toLinkParameters();

			return new LinkByValueAdapter(
					adapterRef(
							ref,
							expectedLinkParameters.getValueType()
							.toLinkType()
							.interfaceRef(expectedLinkParameters),
							request.getLogger()),
					expectedLinkParameters);
		}

		final Ref adapter = adapterRef(
				ref,
				expectedParameters.getValueType().typeRef(ref, ref.getScope()),
				request.getLogger());

		return adapter.valueAdapter(request.noLinkToLink());
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

	@Override
	protected KeeperIR<?, ?> createKeeperIR(
			TypeParameters<KnownLink> parameters,
			ObjectIRBody bodyIR,
			Keeper keeper) {
		return new LinkKeeperIR(parameters, bodyIR, keeper);
	}

}

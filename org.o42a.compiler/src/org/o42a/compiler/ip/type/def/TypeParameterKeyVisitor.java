/*
    Compiler
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
package org.o42a.compiler.ip.type.def;

import static org.o42a.compiler.ip.access.AccessRules.ACCESS_FROM_PLACEMENT;
import static org.o42a.compiler.ip.ref.RefInterpreter.PLAIN_REF_IP;
import static org.o42a.core.member.MemberKey.brokenMemberKey;
import static org.o42a.core.member.MemberName.fieldName;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.field.AbstractDeclarableVisitor;
import org.o42a.ast.field.DeclarableNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.core.Contained;
import org.o42a.core.member.*;
import org.o42a.core.object.Obj;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.object.type.Sample;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;


final class TypeParameterKeyVisitor
		extends AbstractDeclarableVisitor<MemberKey, TypeDefinitionBuilder> {

	private static final TypeParameterKeyVisitor
	DECLARED_TYPE_PARAMETER_KEY_VISITOR = new TypeParameterKeyVisitor(false);
	private static final TypeParameterKeyVisitor
	OVERRIDDEN_TYPE_PARAMETER_KEY_VISITOR = new TypeParameterKeyVisitor(true);

	public static TypeParameterKeyVisitor typeParameterKeyVisitor(
			boolean override) {
		if (override) {
			return OVERRIDDEN_TYPE_PARAMETER_KEY_VISITOR;
		}
		return DECLARED_TYPE_PARAMETER_KEY_VISITOR;
	}

	private final boolean overridden;

	private TypeParameterKeyVisitor(boolean overridden) {
		this.overridden = overridden;
	}

	@Override
	public MemberKey visitMemberRef(
			MemberRefNode ref,
			TypeDefinitionBuilder p) {

		final NameNode name = ref.getName();

		if (name == null) {
			return super.visitMemberRef(ref, p);
		}
		if (ref.getOwner() != null) {
			p.getLogger().error(
					"prohibited_type_parameter_visibility",
					ref.getOwner(),
					"Type parameter visibility is always public");
		}

		final StaticTypeRef declaredInRef = declaredIn(ref, p);

		if (declaredInRef != null && !declaredInRef.isValid()) {
			return brokenMemberKey();
		}

		final Obj declaredIn =
				declaredInRef != null ? declaredInRef.getType() : null;

		final MemberName fieldName = fieldName(name.getName());
		final Contained location =
				new Contained(p.getContext(), ref, p.distribute());

		if (!this.overridden) {
			return fieldName.key(p.getScope());
		}

		final Obj object = p.toObject();
		final Ascendants ascendants = object.type().getAscendants();

		for (Sample sample : ascendants.getSamples()) {

			final Member member = sample.getObject().objectMember(
					Accessor.PUBLIC,
					fieldName,
					declaredIn);

			if (member != null) {
				return member.getMemberKey();
			}
		}

		final TypeRef ancestor = ascendants.getAncestor();

		if (ancestor != null) {

			final Member member = ancestor.getType().objectMember(
					Accessor.PUBLIC,
					fieldName,
					declaredIn);

			if (member != null) {
				return member.getMemberKey();
			}
		}

		p.getLogger().unresolved(location.getLocation(), name.getName());

		return brokenMemberKey();
	}

	@Override
	protected MemberKey visitDeclarable(
			DeclarableNode declarable,
			TypeDefinitionBuilder p) {
		p.getLogger().error(
				"invalid_type_parameter_name",
				declarable,
				"Invalid type parameter name");
		return brokenMemberKey();
	}

	private StaticTypeRef declaredIn(
			MemberRefNode ref,
			TypeDefinitionBuilder p) {

		final RefNode declaredInNode = ref.getDeclaredIn();

		if (declaredInNode == null) {
			return null;
		}
		if (this.overridden) {
			p.getLogger().prohibitedDeclaredIn(declaredInNode);
			return null;
		}

		final Ref declaredIn = declaredInNode.accept(
				PLAIN_REF_IP.bodyRefVisitor(),
				ACCESS_FROM_PLACEMENT.distribute(p.distribute()));

		if (declaredIn == null) {
			return null;
		}

		return declaredIn.toStaticTypeRef();
	}

}

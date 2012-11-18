/*
    Compiler
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
package org.o42a.compiler.ip.type.def;

import static org.o42a.compiler.ip.ref.RefInterpreter.PLAIN_REF_IP;
import static org.o42a.core.member.MemberKey.brokenMemberKey;
import static org.o42a.core.member.MemberName.fieldName;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.field.AbstractDeclarableVisitor;
import org.o42a.ast.field.DeclarableNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.core.Distributor;
import org.o42a.core.Placed;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.MemberName;
import org.o42a.core.object.Accessor;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.StaticTypeRef;


final class TypeParameterNameVisitor
		extends AbstractDeclarableVisitor<MemberKey, Distributor> {

	private static final TypeParameterNameVisitor
	DECLARED_TYPE_PARAMETER_NAME_VISITOR = new TypeParameterNameVisitor(false);
	private static final TypeParameterNameVisitor
	OVERRIDDEN_TYPE_PARAMETER_NAME_VISITOR = new TypeParameterNameVisitor(true);

	public static TypeParameterNameVisitor typeParameterNameVisitor(
			boolean override) {
		if (override) {
			return OVERRIDDEN_TYPE_PARAMETER_NAME_VISITOR;
		}
		return DECLARED_TYPE_PARAMETER_NAME_VISITOR;
	}

	private final boolean overridden;

	private TypeParameterNameVisitor(boolean overridden) {
		this.overridden = overridden;
	}

	@Override
	public MemberKey visitMemberRef(MemberRefNode ref, Distributor p) {

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

		final StaticTypeRef declaredIn = declaredIn(ref, p);

		if (declaredIn != null && !declaredIn.isValid()) {
			return brokenMemberKey();
		}

		final MemberName fieldName = fieldName(name.getName());
		final Placed location = new Placed(p.getContext(), ref, p);

		if (!this.overridden) {
			return fieldName.key(p.getScope());
		}

		final Obj object = p.getScope().toObject();
		final Member member = object.objectMember(
				Accessor.PUBLIC,
				fieldName,
				declaredIn != null ? declaredIn.getType() : null);

		if (member == null) {
			p.getLogger().unresolved(location, name.getName());
			return brokenMemberKey();
		}

		return member.getMemberKey();
	}

	@Override
	protected MemberKey visitDeclarable(
			DeclarableNode declarable,
			Distributor p) {
		p.getLogger().error(
				"invalid_type_parameter_name",
				declarable,
				"Invalid type parameter name");
		return brokenMemberKey();
	}

	private StaticTypeRef declaredIn(MemberRefNode ref, Distributor p) {

		final RefNode declaredInNode = ref.getDeclaredIn();

		if (declaredInNode == null) {
			return null;
		}
		if (this.overridden) {
			p.getLogger().prohibitedDeclaredIn(declaredInNode);
			return null;
		}

		final Ref declaredIn =
				declaredInNode.accept(PLAIN_REF_IP.bodyRefVisitor(), p);

		if (declaredIn == null) {
			return null;
		}

		return declaredIn.toStaticTypeRef();
	}

}

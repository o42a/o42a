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

import static org.o42a.analysis.use.User.dummyUser;

import org.o42a.compiler.ip.type.TypeConsumer;
import org.o42a.core.*;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.member.type.MemberTypeParameter;
import org.o42a.core.object.Accessor;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathResolution;
import org.o42a.core.ref.path.PathResolver;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.ArrayUtil;


public class TypeDefinitionBuilder
		extends AbstractContainer
		implements PlaceInfo{

	private static final TypeParameterDeclaration[] NO_PARAMETERS =
			new TypeParameterDeclaration[0];

	private final Container enclosing;
	private final ScopePlace place;
	private final Path objectPath;
	private final TypeConsumer consumer;
	private Obj object;
	private TypeParameterDeclaration[] parameters = NO_PARAMETERS;

	public TypeDefinitionBuilder(
			LocationInfo location,
			Distributor enclosing,
			Path objectPath,
			TypeConsumer consumer) {
		super(location);
		this.enclosing = enclosing.getContainer();
		this.place = enclosing.getPlace();
		this.objectPath = objectPath;
		this.consumer = consumer;
	}

	@Override
	public final Scope getScope() {
		return getEnclosingContainer().getScope();
	}

	@Override
	public final ScopePlace getPlace() {
		return this.place;
	}

	@Override
	public final Container getContainer() {
		return this;
	}

	@Override
	public final Container getEnclosingContainer() {
		return this.enclosing;
	}

	public final TypeConsumer getConsumer() {
		return this.consumer;
	}

	public final Obj getObject() {
		if (this.object != null) {
			return this.object;
		}

		final PathResolution resolution =
				this.objectPath.bind(this, getScope())
				.resolve(PathResolver.pathResolver(getScope(), dummyUser()));

		return this.object = resolution.getResult().toObject();
	}

	public final TypeParameterDeclaration[] getParameters() {
		return this.parameters;
	}

	@Override
	public final Member toMember() {
		return getEnclosingContainer().toMember();
	}

	@Override
	public final Obj toObject() {
		return getEnclosingContainer().toObject();
	}

	@Override
	public final Clause toClause() {
		return getEnclosingContainer().toClause();
	}

	@Override
	public final LocalScope toLocal() {
		return getEnclosingContainer().toLocal();
	}

	@Override
	public Namespace toNamespace() {
		return null;
	}

	@Override
	public Member member(MemberKey memberKey) {
		return getEnclosingContainer().member(memberKey);
	}

	@Override
	public Path member(
			PlaceInfo user,
			Accessor accessor,
			MemberId memberId,
			Obj declaredIn) {
		return getEnclosingContainer()
				.member(user, accessor, memberId, declaredIn);
	}

	@Override
	public Path findMember(
			PlaceInfo user,
			Accessor accessor,
			MemberId memberId,
			Obj declaredIn) {

		final MemberTypeParameter typeParameter =
				findTypeParameter(memberId, declaredIn);

		if (typeParameter != null) {
			return this.objectPath.append(typeParameter.getMemberKey());
		}

		return getEnclosingContainer()
				.findMember(user, accessor, memberId, declaredIn);
	}

	public final void addParameter(TypeParameterDeclaration parameter) {
		this.parameters = ArrayUtil.append(this.parameters, parameter);
	}

	public final TypeDefinition buildDefinition() {
		return new TypeDefinition(this);
	}

	@Override
	public final Distributor distribute() {
		return Placed.distribute(this);
	}

	@Override
	public final Distributor distributeIn(Container container) {
		return Placed.distributeIn(this, container);
	}

	private MemberTypeParameter findTypeParameter(
			MemberId memberId,
			Obj declaredIn) {

		final Member objectMember =
				getObject().objectMember(Accessor.PUBLIC, memberId, declaredIn);

		if (objectMember == null) {
			return null;
		}

		return objectMember.toTypeParameter();
	}

}

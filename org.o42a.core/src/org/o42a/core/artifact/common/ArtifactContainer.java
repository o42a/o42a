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
package org.o42a.core.artifact.common;

import static java.util.Collections.emptyList;

import java.util.Collection;

import org.o42a.core.*;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.ArtifactScope;
import org.o42a.core.member.*;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.object.Accessor;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.path.Path;


final class ArtifactContainer<A extends Artifact<A>>
		extends AbstractContainer
		implements MemberContainer {

	private final ArtifactScope<A> scope;

	ArtifactContainer(ArtifactScope<A> scope) {
		super(scope);
		this.scope = scope;
	}

	@Override
	public Scope getScope() {
		return this.scope;
	}

	@Override
	public Container getEnclosingContainer() {
		return this.scope.getEnclosingContainer();
	}

	@Override
	public Collection<? extends Member> getMembers() {
		return emptyList();
	}

	@Override
	public Member toMember() {
		return null;
	}

	@Override
	public A toArtifact() {
		return this.scope.getArtifact();
	}

	@Override
	public Obj toObject() {
		return null;
	}

	@Override
	public Clause toClause() {
		return null;
	}

	@Override
	public LocalScope toLocal() {
		return null;
	}

	@Override
	public Namespace toNamespace() {
		return null;
	}

	@Override
	public Member member(MemberKey memberKey) {
		return null;
	}

	@Override
	public Path member(
			PlaceInfo user,
			Accessor accessor,
			MemberId memberId,
			Obj declaredIn) {
		return null;
	}

	@Override
	public Path findMember(
			PlaceInfo user,
			Accessor accessor,
			MemberId memberId,
			Obj declaredIn) {
		return null;
	}

}

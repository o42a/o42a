/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core.member.field;


import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.MemberOwner;
import org.o42a.util.use.UserInfo;


abstract class FieldWrap<A extends Artifact<A>> extends Field<A> {

	private final Field<A> iface;
	private final Field<A> wrapped;

	@SuppressWarnings("unchecked")
	public FieldWrap(MemberOwner owner, Field<?> type, Field<?> wrapped) {
		super(new MemberFieldWrap(
				owner,
				new FieldDeclaration(
						wrapped,
						wrapped.distributeIn(owner.getContainer()),
						wrapped.getDeclaration())
				.override()));
		((MemberFieldWrap) toMember()).init(this);
		this.iface = (Field<A>) type;
		this.wrapped = (Field<A>) wrapped;
		setScopeArtifact(wrapArtifact());
	}

	protected FieldWrap(MemberOwner owner, FieldWrap<A> overridden) {
		super(owner, overridden, false);

		final Obj inherited = owner.getContainer().toObject();
		final UserInfo user = resolverFactory();

		this.iface = inherited.member(
				overridden.iface.getKey()).toField(user).toKind(
						overridden.iface.getArtifactKind());
		this.wrapped = inherited.getWrapped().member(
				overridden.iface.getKey()).toField(user).toKind(
						overridden.iface.getArtifactKind());
		setScopeArtifact(overridden.getArtifact());
	}

	public final Field<A> getInterface() {
		return this.iface;
	}

	public final Field<A> getWrapped() {
		return this.wrapped;
	}

	@Override
	public final A getArtifact() {
		return getScopeArtifact();
	}

	protected abstract A wrapArtifact();

}

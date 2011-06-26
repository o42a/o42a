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

import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.OverrideMode;


abstract class FieldWrap<A extends Artifact<A>> extends Field<A> {

	private final Field<A> iface;
	private final Field<A> wrapped;

	public FieldWrap(MemberOwner owner, Field<A> type, Field<A> wrapped) {
		super(owner, wrapped, wrapped, OverrideMode.WRAP);
		this.iface = type;
		this.wrapped = wrapped;
		setFieldArtifact(wrapArtifact());
	}

	protected FieldWrap(MemberOwner owner, FieldWrap<A> overridden) {
		this(
				owner,
				overridden,
				owner.getContainer().toObject().getWrapped()
				.member(overridden.iface.getKey()).toField(dummyUser())
				.toKind(overridden.iface.getArtifactKind()));
	}

	private FieldWrap(
			MemberOwner owner,
			FieldWrap<A> overridden,
			FieldWrap<A> wrapped) {
		super(owner, overridden, wrapped, OverrideMode.WRAP);

		final Obj inherited = owner.getContainer().toObject();

		this.iface = inherited.member(
				overridden.iface.getKey()).toField(dummyUser()).toKind(
						overridden.iface.getArtifactKind());
		this.wrapped = wrapped;

		setFieldArtifact(overridden.getArtifact());
	}

	public final Field<A> getInterface() {
		return this.iface;
	}

	public final Field<A> getWrapped() {
		return this.wrapped;
	}

	@Override
	public final A getArtifact() {
		return getFieldArtifact();
	}

	protected abstract A wrapArtifact();

}

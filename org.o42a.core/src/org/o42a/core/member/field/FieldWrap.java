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

import static org.o42a.core.source.CompilerLogger.logDeclaration;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.artifact.Artifact;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.OverrideMode;
import org.o42a.core.source.Location;


abstract class FieldWrap<A extends Artifact<A>> extends Field<A> {

	private static <A extends Artifact<A>> Field<A> wrapped(
			MemberOwner owner,
			FieldWrap<A> overridden) {
		return owner.toObject().getWrapped()
				.member(overridden.getInterface().getKey())
				.toField(dummyUser())
				.toKind(overridden.getInterface().getArtifactKind());
	}

	private final Field<A> iface;
	private final Field<A> wrapped;

	public FieldWrap(MemberOwner owner, Field<A> type, Field<A> wrapped) {
		super(wrapped, owner, wrapped, wrapped, OverrideMode.WRAP);
		this.iface = type;
		this.wrapped = wrapped;
		setFieldArtifact(wrapArtifact());
	}

	protected FieldWrap(MemberOwner owner, FieldWrap<A> overridden) {
		super(
				new Location(
						owner.getContext(),
						owner.getLoggable().setReason(
								logDeclaration(
										overridden.getLastDefinition()))),
				owner,
				overridden,
				wrapped(owner, overridden),
				OverrideMode.WRAP);
		this.iface =
				owner.toObject()
				.member(overridden.getInterface().getKey())
				.toField(dummyUser())
				.toKind(overridden.getInterface().getArtifactKind());
		this.wrapped = wrapped(owner, overridden);
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

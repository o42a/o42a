/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.core.member.impl.field;

import static org.o42a.core.source.CompilerLogger.logDeclaration;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.source.Location;


public abstract class OverriddenMemberField<F extends Field<?>>
	extends MemberField {

	private final MemberField propagatedFrom;

	public OverriddenMemberField(
			MemberOwner owner,
			MemberField propagatedFrom) {
		super(
				new Location(
						owner.getContext(),
						owner.getLoggable().setReason(
								logDeclaration(
										propagatedFrom.getLastDefinition()))),
				owner,
				propagatedFrom);
		this.propagatedFrom = propagatedFrom;
	}

	@Override
	public final ArtifactKind<?> getArtifactKind() {
		return getPropagatedFrom().getArtifactKind();
	}

	@Override
	public final MemberField getPropagatedFrom() {
		return this.propagatedFrom;
	}

	@Override
	public abstract OverriddenMemberField<F> propagateTo(MemberOwner owner);

	@Override
	protected final F createField() {

		@SuppressWarnings("unchecked")
		final F propagatedFrom =
				(F) getPropagatedFrom().toField().field(dummyUser());

		return propagateField(propagatedFrom);
	}

	protected abstract F propagateField(F propagatedFrom);

}

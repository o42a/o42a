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
package org.o42a.core.ref;

import static org.o42a.core.ref.path.Path.SELF_PATH;

import org.o42a.core.PlaceInfo;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.path.Path;
import org.o42a.util.use.UserInfo;


final class LocalResolution extends Resolution {

	LocalResolution(LocalScope local) {
		super(local);
	}

	@Override
	public final LocalScope toContainer() {
		return (LocalScope) getResolved();
	}

	@Override
	public final Artifact<?> toArtifact() {
		return null;
	}

	@Override
	public final Obj materialize() {
		return null;
	}

	@Override
	public Path materializationPath() {
		return SELF_PATH;
	}

	@Override
	public Path member(PlaceInfo user, MemberId memberId, Obj declaredIn) {

		final Path found =
			toContainer().member(user, memberId, declaredIn);

		if (found == null) {
			user.getContext().getLogger().unresolved(user, memberId);
			return null;
		}

		return found;
	}

	@Override
	public void resolveAll() {
		toContainer().resolveAll();
	}

	@Override
	public void resolveValues(UserInfo user) {
	}

}

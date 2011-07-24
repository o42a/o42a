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
package org.o42a.core.ref.impl.resolution;

import static org.o42a.core.ref.path.Path.materializePath;

import org.o42a.core.Container;
import org.o42a.core.PlaceInfo;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.MemberId;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.path.Path;
import org.o42a.util.use.UserInfo;


public final class ArtifactResolution extends Resolution {

	public ArtifactResolution(Artifact<?> resolved) {
		super(resolved);
	}

	@Override
	public final Container toContainer() {
		return toArtifact().getContainer();
	}

	@Override
	public final Artifact<?> toArtifact() {
		return (Artifact<?>) getResolved();
	}

	@Override
	public final Obj materialize() {
		return toArtifact().materialize();
	}

	@Override
	public final Path materializationPath() {
		return materializePath();
	}

	@Override
	public Path member(PlaceInfo user, MemberId memberId, Obj declaredIn) {

		final Obj owner = materialize();
		final Path found = owner.member(user, memberId, declaredIn);

		if (found == null) {
			user.getContext().getLogger().unresolved(user, memberId);
			return null;
		}

		return materializationPath().append(found);
	}

	@Override
	public void resolveAll() {
		toArtifact().resolveAll();
	}

	@Override
	public void resolveValues(UserInfo user) {

		final Obj materialized = materialize();

		materialized.value().resolveAll(user);
	}

}

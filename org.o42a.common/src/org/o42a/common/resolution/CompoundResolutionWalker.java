/*
    Modules Commons
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
package org.o42a.common.resolution;

import static org.o42a.core.ref.path.PathWalker.DUMMY_PATH_WALKER;

import org.o42a.core.ScopeInfo;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ref.ResolutionWalker;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.PathWalker;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.ArrayUtil;


public class CompoundResolutionWalker implements ResolutionWalker {

	private final ResolutionWalker[] walkers;

	public CompoundResolutionWalker(ResolutionWalker... walkers) {
		this.walkers = walkers;
	}

	public final ResolutionWalker[] getWalkers() {
		return this.walkers;
	}

	@Override
	public PathWalker path(LocationInfo location, BoundPath path) {

		final ResolutionWalker[] walkers = getWalkers();
		final PathWalker[] pathWalkers = new PathWalker[walkers.length];
		int idx = 0;

		for (ResolutionWalker walker : walkers) {

			final PathWalker pathWalker = walker.path(location, path);

			if (pathWalker != null && pathWalker != DUMMY_PATH_WALKER) {
				pathWalkers[idx++] = pathWalker;
			}
		}

		if (idx <= 1) {
			if (idx == 0) {
				return PathWalker.DUMMY_PATH_WALKER;
			}
			return pathWalkers[0];
		}

		return new CompoundPathWalker(ArrayUtil.clip(pathWalkers, idx));
	}

	@Override
	public boolean newObject(ScopeInfo location, Obj object) {

		boolean proceed = true;

		for (ResolutionWalker walker : getWalkers()) {
			proceed = walker.newObject(location, object) & proceed;
		}

		return proceed;
	}

	@Override
	public boolean artifactPart(
			LocationInfo location,
			Artifact<?> artifact,
			Artifact<?> part) {

		boolean proceed = true;

		for (ResolutionWalker walker : getWalkers()) {
			proceed = walker.artifactPart(location, artifact, part) & proceed;
		}

		return proceed;
	}

	@Override
	public boolean staticArtifact(LocationInfo location, Artifact<?> artifact) {

		boolean proceed = true;

		for (ResolutionWalker walker : getWalkers()) {
			proceed = walker.staticArtifact(location, artifact) & proceed;
		}

		return proceed;
	}

}

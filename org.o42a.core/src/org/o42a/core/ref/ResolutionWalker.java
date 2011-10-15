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

import org.o42a.core.ScopeInfo;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.PathWalker;
import org.o42a.core.source.LocationInfo;


public interface ResolutionWalker {

	ResolutionWalker DUMMY_RESOLUTION_WALKER = new DummyResolutionWalker();

	PathWalker path(BoundPath path);

	boolean newObject(ScopeInfo location, Obj object);

	boolean artifactPart(
			LocationInfo location,
			Artifact<?> artifact,
			Artifact<?> part);

	boolean staticArtifact(LocationInfo location, Artifact<?> artifact);

}

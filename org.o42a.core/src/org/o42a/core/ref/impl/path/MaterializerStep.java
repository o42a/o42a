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
package org.o42a.core.ref.impl.path;

import static org.o42a.core.ref.path.PathReproduction.reproducedPath;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ref.path.*;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;


public final class MaterializerStep extends Step {

	public static final MaterializerStep INSTANCE =
			new MaterializerStep();

	private MaterializerStep() {
	}

	@Override
	public PathKind getPathKind() {
		return PathKind.RELATIVE_PATH;
	}

	@Override
	public boolean isMaterializer() {
		return true;
	}

	@Override
	public boolean isArtifact() {
		return false;
	}

	@Override
	public Step materialize() {
		return null;
	}

	@Override
	public Container resolve(
			PathResolver resolver,
			Path path,
			int index,
			Scope start,
			PathWalker walker) {

		final Artifact<?> artifact = start.getArtifact();

		assert artifact != null :
			"Can not materialize " + start;
		assert artifact.getKind() != ArtifactKind.OBJECT :
			"An attempt to materialize object " + start;

		final Obj result = artifact.materialize();

		walker.materialize(artifact, this, result);

		return result;
	}

	@Override
	public PathReproduction reproduce(
			LocationInfo location,
			Reproducer reproducer,
			Scope scope) {
		return reproducedPath(toPath());
	}

	@Override
	public HostOp write(CodeDirs dirs, HostOp start) {
		return start.materialize(dirs);
	}

	@Override
	public String toString() {
		return "*";
	}

}

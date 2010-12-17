/*
    Compiler Core
    Copyright (C) 2010 Ruslan Lopatin

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
package org.o42a.core.ref.path;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.Container;
import org.o42a.core.LocationSpec;
import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;


final class MaterializeFragment extends PathFragment {

	@Override
	public Container resolve(
			LocationSpec location,
			Path path,
			int index,
			Scope start,
			PathWalker walker) {

		final Artifact<?> artifact = start.getContainer().toArtifact();

		assert artifact != null :
			"Can not materialize " + start;
		assert artifact.getKind() != ArtifactKind.OBJECT :
			"An attempt to materialize object " + start;

		final Obj result = artifact.materialize();

		walker.materialize(artifact, this, result);

		return result;
	}

	@Override
	public Reproduction reproduce(LocationSpec location, Scope scope) {
		return reproduced(this);
	}

	@Override
	public HostOp write(Code code, CodePos exit, HostOp start) {
		return start.materialize(code, exit);
	}

	@Override
	public String toString() {
		return "*";
	}

}

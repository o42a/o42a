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
package org.o42a.core.ref.impl.path;

import static org.o42a.core.ref.path.PathReproduction.unchangedPath;

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.PathOp;
import org.o42a.core.ir.op.StepOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.path.*;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;


public class VoidStep extends Step {

	@Override
	public PathKind getPathKind() {
		return PathKind.ABSOLUTE_PATH;
	}

	@Override
	public boolean isMaterial() {
		return true;
	}

	@Override
	public Container resolve(
			PathResolver resolver,
			BoundPath path,
			int index,
			Scope start,
			PathWalker walker) {

		final Obj voidObject = start.getContext().getVoid();

		walker.object(this, voidObject);

		return voidObject;
	}

	@Override
	public PathReproduction reproduce(
			LocationInfo location,
			Reproducer reproducer) {
		return unchangedPath(toPath());
	}

	@Override
	public PathOp op(PathOp start) {
		return new Op(start, this);
	}

	@Override
	public String toString() {
		return "VOID";
	}

	@Override
	protected FieldDefinition fieldDefinition(
			BoundPath path,
			Distributor distributor) {
		return objectFieldDefinition(path, distributor);
	}

	private static final class Op extends StepOp<VoidStep> {

		Op(PathOp start, VoidStep step) {
			super(start, step);
		}

		@Override
		public HostOp target(CodeDirs dirs) {

			final Obj voidObject = getContext().getVoid();

			return voidObject.ir(getGenerator()).op(getBuilder(), dirs.code());
		}

	}

}

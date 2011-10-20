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

import static org.o42a.core.ref.path.PathReproduction.reproducedPath;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ref.path.*;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;


public class ObjectConstructorStep extends Step {

	private final ObjectConstructor constructor;

	public ObjectConstructorStep(ObjectConstructor constructor) {
		this.constructor = constructor;
	}

	@Override
	public StepKind getStepKind() {
		return StepKind.ARTIFACT_STEP;
	}

	@Override
	public PathKind getPathKind() {
		return PathKind.RELATIVE_PATH;
	}

	@Override
	public Step materialize() {
		return null;
	}

	@Override
	public Container resolve(
			PathResolver resolver,
			BoundPath path,
			int index,
			Scope start,
			PathWalker walker) {

		final Obj object = this.constructor.resolve(start);

		if (object == null) {
			return null;
		}

		walker.newObject(this, object);

		return object;
	}

	@Override
	public PathReproduction reproduce(
			LocationInfo location,
			Reproducer reproducer,
			Scope scope) {
		this.constructor.assertCompatible(reproducer.getReproducingScope());

		final ObjectConstructor reproduced =
				this.constructor.reproduce(reproducer);

		if (reproduced == null) {
			return null;
		}

		return reproducedPath(reproduced.toPath());
	}

	@Override
	public HostOp write(CodeDirs dirs, HostOp start) {
		return this.constructor.write(dirs, start);
	}

}

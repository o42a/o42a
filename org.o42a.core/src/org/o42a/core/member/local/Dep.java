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
package org.o42a.core.member.local;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.*;


public abstract class Dep extends Step {

	private final Obj object;
	private final DepKind kind;

	public Dep(Obj object, DepKind kind) {
		this.object = object;
		this.kind = kind;
	}

	@Override
	public final StepKind getStepKind() {
		return getDepKind().getStepKind();
	}

	@Override
	public final PathKind getPathKind() {
		return getDepKind().getPathKind();
	}

	public final DepKind getDepKind() {
		return this.kind;
	}

	public final Obj getObject() {
		return this.object;
	}

	public abstract Object getDepKey();

	public abstract Artifact<?> getDepTarget();

	public abstract Ref getDepRef();

	@Override
	public Container resolve(
			PathResolver resolver,
			BoundPath path,
			int index,
			Scope start,
			PathWalker walker) {

		final Obj object = start.toObject();

		assert object != null :
			"Dependency " + path.toString(index + 1)
			+ " should be resolved against object, but were not: " + start;

		final LocalScope enclosingLocal =
				object.getScope().getEnclosingContainer().toLocal();

		assert enclosingLocal != null :
			object + " is inside " + object.getScope().getEnclosingContainer()
			+ ", which is not a local scope";

		return resolveDep(
				resolver,
				path,
				index,
				object,
				enclosingLocal,
				walker);
	}

	@Override
	public HostOp write(CodeDirs dirs, HostOp start) {

		final ObjectOp object = start.toObject(dirs);

		assert object != null :
			"Not an object: " + start;

		return object.dep(dirs, this);
	}

	protected abstract Container resolveDep(
			PathResolver resolver,
			BoundPath path,
			int index,
			Obj object,
			LocalScope enclosingLocal,
			PathWalker walker);

}

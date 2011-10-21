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
package org.o42a.core.ref.path;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.MemberKey;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.impl.path.MaterializerStep;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;


public abstract class Step {

	public static final MaterializerStep MATERIALIZE =
			MaterializerStep.INSTANCE;

	public abstract StepKind getStepKind();

	public abstract PathKind getPathKind();

	public String getName() {
		return null;
	}

	public abstract Step materialize();

	public abstract Container resolve(
			PathResolver resolver,
			BoundPath path,
			int index,
			Scope start,
			PathWalker walker);

	public abstract PathReproduction reproduce(
			LocationInfo location,
			Reproducer reproducer);

	public Step combineWithMember(MemberKey memberKey) {
		return null;
	}

	public Step combineWithLocalOwner(Obj owner) {
		return null;
	}

	public Step combineWithRef(Ref ref) {
		return null;
	}

	public final Path toPath() {
		return new Path(
				getPathKind(),
				getStepKind() == StepKind.STATIC_STEP,
				this);
	}

	public abstract HostOp write(CodeDirs dirs, HostOp start);

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	protected PathFragment getPathFragment() {
		return null;
	}

	protected Step rebuild(Step prev) {
		return null;
	}

}

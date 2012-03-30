/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.PathOp;
import org.o42a.core.ir.op.StepOp;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.path.*;


public abstract class Dep extends Step {

	private final Obj object;
	private final DepKind kind;
	private boolean disabled;

	public Dep(Obj object, DepKind kind) {
		this.object = object;
		this.kind = kind;
	}

	public final boolean isDisabled() {
		return this.disabled;
	}

	public final void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	@Override
	public final PathKind getPathKind() {
		return PathKind.RELATIVE_PATH;
	}

	@Override
	public RefUsage getObjectUsage() {
		return RefUsage.CONTAINER_REF_USAGE;
	}

	public final DepKind getDepKind() {
		return this.kind;
	}

	public final Obj getObject() {
		return this.object;
	}

	public abstract Object getDepKey();

	public abstract Obj getDepTarget();

	public abstract Ref getDepRef();

	@Override
	protected Container resolve(
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

	protected abstract Container resolveDep(
			PathResolver resolver,
			BoundPath path,
			int index,
			Obj object,
			LocalScope enclosingLocal,
			PathWalker walker);

	@Override
	protected final void normalize(PathNormalizer normalizer) {

		final Obj object = normalizer.lastPrediction().getScope().toObject();
		final Scope objectScope = object.getScope();
		final LocalScope enclosingLocal =
				objectScope.getEnclosingContainer().toLocal();

		if (!normalizer.up(
				enclosingLocal,
				objectScope.getEnclosingScopePath())) {
			return;
		}

		normalizeDep(normalizer, enclosingLocal);
	}

	@Override
	protected void normalizeStatic(PathNormalizer normalizer) {
		throw new UnsupportedOperationException(
				"Dep can not be a part of static path " + normalizer.getPath());
	}

	@Override
	protected abstract Path nonNormalizedRemainder(PathNormalizer normalizer);

	protected abstract void normalizeDep(
			PathNormalizer normalizer,
			LocalScope local);

	@Override
	protected final PathOp op(PathOp start) {
		assert !isDisabled() :
			this + " is disabled";
		return new Op(start, this);
	}

	private static final class Op extends StepOp<Dep> {

		Op(PathOp start, Dep step) {
			super(start, step);
		}

		@Override
		public HostOp target(CodeDirs dirs) {
			return start().materialize(dirs).dep(dirs, getStep());
		}

	}

}

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
package org.o42a.core.member.impl.local;

import static org.o42a.core.ref.path.PathReproduction.reproducedPath;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.local.LocalOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.path.*;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;


public final class LocalOwnerStep extends Step {

	private final LocalScope local;

	public LocalOwnerStep(LocalScope local) {
		this.local = local;
	}

	@Override
	public PathKind getPathKind() {
		return PathKind.RELATIVE_PATH;
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

		final LocalScope local = start.toLocal();

		local.assertDerivedFrom(this.local);

		final Obj owner = local.getOwner();

		walker.up(local, this, owner);

		return owner;
	}

	@Override
	public PathReproduction reproduce(
			LocationInfo location,
			Reproducer reproducer,
			Scope scope) {
		return reproducedPath(scope.toLocal().getEnclosingScopePath());
	}

	@Override
	public HostOp write(CodeDirs dirs, HostOp start) {

		final LocalOp local = start.toLocal();

		assert local != null :
			start + " is not local";

		return local.getBuilder().owner();
	}

	@Override
	public String toString() {
		return "Owner[" + this.local + ']';
	}

	@Override
	protected Step rebuild(Step prev) {
		return prev.combineWithLocalOwner(this.local.getOwner());
	}

}

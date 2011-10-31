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

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.ir.op.PathOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.path.*;
import org.o42a.core.source.LocationInfo;


public final class PathFragmentStep extends Step {

	private final PathFragment fragment;

	public PathFragmentStep(PathFragment fragment) {
		this.fragment = fragment;
	}

	@Override
	public PathKind getPathKind() {
		return PathKind.RELATIVE_PATH;
	}

	@Override
	public boolean isMaterial() {
		return false;
	}

	@Override
	public Container resolve(
			PathResolver resolver,
			BoundPath path,
			int index,
			Scope start,
			PathWalker walker) {
		throw unresolved();
	}

	@Override
	public PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer) {
		throw unresolved();
	}

	@Override
	public PathOp op(PathOp start) {
		throw unresolved();
	}

	@Override
	public String toString() {
		if (this.fragment == null) {
			return "(?)";
		}
		return '(' + this.fragment.toString() + ')';
	}

	@Override
	protected PathFragment getPathFragment() {
		return this.fragment;
	}

	@Override
	protected FieldDefinition fieldDefinition(
			BoundPath path,
			Distributor distributor) {
		return this.fragment.fieldDefinition(path, distributor);
	}

	private IllegalStateException unresolved() {
		return new IllegalStateException(
				"Path fragment not resolved yet: " + this);
	}

}

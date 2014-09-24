/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.core.ref.path.impl;

import org.o42a.core.Container;
import org.o42a.core.ir.op.HostOp;
import org.o42a.core.ir.op.RefIR;
import org.o42a.core.ir.op.RefTargetIR;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.LocalRegistry;


public final class PathFragmentStep extends Step {

	private final AbstractPathFragment fragment;

	public PathFragmentStep(AbstractPathFragment fragment) {
		this.fragment = fragment;
	}

	@Override
	public PathKind getPathKind() {
		return PathKind.RELATIVE_PATH;
	}

	@Override
	public AbstractPathFragment getPathFragment() {
		return this.fragment;
	}

	@Override
	public RefUsage getObjectUsage() {
		return null;
	}

	@Override
	public String toString() {
		if (this.fragment == null) {
			return "(?)";
		}
		return '(' + this.fragment.toString() + ')';
	}

	@Override
	protected FieldDefinition fieldDefinition(Ref ref) {
		return this.fragment.fieldDefinition(ref);
	}

	@Override
	protected void localMember(LocalRegistry registry) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected TypeRef ancestor(LocationInfo location, Ref ref) {
		return defaultAncestor(location, ref);
	}

	@Override
	protected TypeRef iface(Ref ref) {
		return this.fragment.iface(ref);
	}

	@Override
	protected Container resolve(StepResolver resolver) {
		throw unresolved();
	}

	@Override
	protected void normalize(PathNormalizer normalizer) {
		throw unresolved();
	}

	@Override
	protected void normalizeStatic(PathNormalizer normalizer) {
		throw unresolved();
	}

	@Override
	protected PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer) {
		throw unresolved();
	}

	@Override
	protected HostOp op(HostOp host) {
		throw unresolved();
	}

	@Override
	protected RefTargetIR targetIR(RefIR refIR) {
		throw unresolved();
	}

	private IllegalStateException unresolved() {
		return new IllegalStateException(
				"Path fragment not resolved yet: " + this);
	}

}

/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.core.object.impl;

import static org.o42a.core.ref.path.PathReproduction.reproducedPath;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.HostValueOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.PathOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.ReversePath;
import org.o42a.core.ref.path.*;
import org.o42a.core.source.LocationInfo;


public final class ParentLocalStep extends Step implements ReversePath {

	private final Obj object;

	public ParentLocalStep(Obj object) {
		this.object = object;
	}

	@Override
	public PathKind getPathKind() {
		return PathKind.RELATIVE_PATH;
	}

	@Override
	public RefUsage getObjectUsage() {
		return null;
	}

	@Override
	public int hashCode() {
		return this.object.getScope().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		final ParentLocalStep other = (ParentLocalStep) obj;

		return this.object.getScope().equals(other.object.getScope());
	}

	@Override
	public Scope revert(Scope target) {
		return this.object.meta().findIn(target).getScope();
	}

	@Override
	public String toString() {
		return "ParentLocal[" + this.object + ']';
	}

	@Override
	protected void combineWith(PathRebuilder rebuilder, Step next) {
		if (rebuilder.isStatic()) {
			return;
		}

		final Container enclosingContainer =
				this.object.getEnclosingContainer();
		final Ref ref =
				rebuilder.restPath(enclosingContainer.getScope())
				.target(this.object.distributeIn(enclosingContainer));

		rebuilder.replaceRest(this.object.deps().addDep(ref));
	}

	@Override
	protected FieldDefinition fieldDefinition(Ref ref) {
		return defaultFieldDefinition(ref);
	}

	@Override
	protected Container resolve(StepResolver resolver) {

		final Obj object = resolver.getStart().toObject();

		object.assertDerivedFrom(this.object);

		final Container result =
				object.getScope().getEnclosingContainer();

		resolver.getWalker().up(object, this, result, this);

		return result;
	}

	@Override
	protected void normalize(PathNormalizer normalizer) {

		final Obj object = normalizer.lastPrediction().getScope().toObject();

		object.assertDerivedFrom(this.object);

		final Container result =
				object.getScope().getEnclosingContainer();

		normalizer.up(result.getScope(), toPath(), this);
	}

	@Override
	protected void normalizeStatic(PathNormalizer normalizer) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer) {
		return reproducedPath(reproducer.getScope().getEnclosingScopePath());
	}

	@Override
	protected PathOp op(PathOp start) {
		return new OpaqueLocalOp(start);
	}

	private static final class OpaqueLocalOp extends PathOp {

		OpaqueLocalOp(PathOp start) {
			super(start);
		}

		@Override
		public HostValueOp value() {
			throw new UnsupportedOperationException(this + " has no value");
		}

		@Override
		public HostOp field(CodeDirs dirs, MemberKey memberKey) {
			throw new UnsupportedOperationException(
					"Can not retrieve a field of " + this);
		}

		@Override
		public ObjectOp materialize(CodeDirs dirs, ObjHolder holder) {
			throw new UnsupportedOperationException(
					"Can not materialize " + this);
		}

		@Override
		public HostOp target(CodeDirs dirs) {
			throw new UnsupportedOperationException(
					"Can not operate with " + this);
		}

		@Override
		public String toString() {
			return "OpaqueParentLocal[" + host() + ']';
		}

	}

}

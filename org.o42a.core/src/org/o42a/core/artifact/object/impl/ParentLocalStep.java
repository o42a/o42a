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
package org.o42a.core.artifact.object.impl;

import static org.o42a.core.ref.path.PathReproduction.reproducedPath;

import org.o42a.codegen.Generator;
import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.local.LocalOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.*;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;


public final class ParentLocalStep extends Step {

	private final Obj object;

	public ParentLocalStep(Obj object) {
		this.object = object;
	}

	@Override
	public StepKind getStepKind() {
		return StepKind.PARENT_STEP;
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

		final Obj object = start.toObject();

		object.assertDerivedFrom(this.object);

		final Container result =
				object.getScope().getEnclosingContainer();

		walker.up(object, this, result);

		return result;
	}

	@Override
	public HostOp write(CodeDirs dirs, HostOp start) {
		return new OpaqueLocalOp(start);
	}

	@Override
	public Step combineWithMember(MemberKey memberKey) {
		return ((ObjectArtifact) this.object).addFieldDep(memberKey);
	}

	@Override
	public Step combineWithLocalOwner(Obj owner) {
		return ((ObjectArtifact) this.object).addEnclosingOwnerDep(owner);
	}

	@Override
	public Step combineWithRef(Ref ref) {
		return ((ObjectArtifact) this.object).addRefDep(ref);
	}

	@Override
	public PathReproduction reproduce(
			LocationInfo location,
			Reproducer reproducer) {
		return reproducedPath(reproducer.getScope().getEnclosingScopePath());
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
	public String toString() {
		return "ParentLocal[" + this.object + ']';
	}

	@Override
	protected FieldDefinition fieldDefinition(
			BoundPath path,
			Distributor distributor) {
		return defaultFieldDefinition(path, distributor);
	}

	private static final class OpaqueLocalOp implements HostOp {

		private final HostOp host;

		OpaqueLocalOp(HostOp host) {
			this.host = host;
		}

		@Override
		public Generator getGenerator() {
			return this.host.getGenerator();
		}

		@Override
		public CodeBuilder getBuilder() {
			return this.host.getBuilder();
		}

		@Override
		public CompilerContext getContext() {
			return this.host.getContext();
		}

		@Override
		public ObjectOp toObject(CodeDirs dirs) {
			return null;
		}

		@Override
		public LocalOp toLocal() {
			return null;
		}

		@Override
		public HostOp field(CodeDirs dirs, MemberKey memberKey) {
			throw new UnsupportedOperationException(
					"Can not retrieve a field of " + this);
		}

		@Override
		public ObjectOp materialize(CodeDirs dirs) {
			throw new UnsupportedOperationException(
					"Can not materialize " + this);
		}

		@Override
		public void assign(CodeDirs dirs, HostOp value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString() {
			return "OpaqueParentLocal[" + this.host + ']';
		}

	}

}

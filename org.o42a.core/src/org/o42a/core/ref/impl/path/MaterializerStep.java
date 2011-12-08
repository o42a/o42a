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
package org.o42a.core.ref.impl.path;

import static org.o42a.core.ref.path.PathReproduction.reproducedPath;

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.PathOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;


public final class MaterializerStep extends Step {

	public static final MaterializerStep MATERIALIZER_STEP =
			new MaterializerStep();

	private MaterializerStep() {
	}

	@Override
	public PathKind getPathKind() {
		return PathKind.RELATIVE_PATH;
	}

	@Override
	public boolean isMaterial() {
		return true;
	}

	@Override
	public RefUsage getObjectUsage() {
		return null;
	}

	@Override
	public String toString() {
		return "*";
	}

	@Override
	protected void rebuild(PathRebuilder rebuilder) {

		final Step prev = rebuilder.getPreviousStep();

		if (prev.isMaterial()) {
			rebuilder.replace(prev);
		}
	}

	@Override
	protected TypeRef ancestor(
			BoundPath path,
			LocationInfo location,
			Distributor distributor) {
		return path.cut(1).ancestor(location, distributor);
	}

	@Override
	protected FieldDefinition fieldDefinition(
			BoundPath path,
			Distributor distributor) {
		return path.cut(1).fieldDefinition(distributor);
	}

	@Override
	protected Container resolve(
			PathResolver resolver,
			BoundPath path,
			int index,
			Scope start,
			PathWalker walker) {

		final Artifact<?> artifact = start.getArtifact();

		assert artifact != null :
			"Can not materialize " + start;

		final Obj result = artifact.materialize();

		walker.materialize(artifact, this, result);

		return result;
	}

	@Override
	protected PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer) {
		return reproducedPath(toPath());
	}

	@Override
	protected void normalize(PathNormalizer normalizer) {
	}

	@Override
	protected PathOp op(PathOp start) {
		return new Op(start);
	}

	private static final class Op extends PathOp {

		Op(PathOp start) {
			super(start);
		}

		@Override
		public void writeLogicalValue(CodeDirs dirs) {
			op().writeLogicalValue(dirs);
		}

		@Override
		public ValOp writeValue(ValDirs dirs) {
			return op().writeValue(dirs);
		}

		@Override
		public HostOp target(CodeDirs dirs) {
			return op().target(dirs).materialize(dirs);
		}

		@Override
		public String toString() {

			final HostOp host = host();

			if (host == null) {
				return super.toString();
			}

			return '(' + host.toString() + ")*";
		}

		private final PathOp op() {
			return (PathOp) host();
		}

	}

}

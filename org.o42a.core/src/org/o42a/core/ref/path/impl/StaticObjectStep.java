/*
    Compiler Core
    Copyright (C) 2011-2013 Ruslan Lopatin

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

import static org.o42a.core.ref.path.PathReproduction.unchangedPath;

import org.o42a.core.Container;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.op.*;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;


public class StaticObjectStep extends Step {

	private final Obj object;

	public StaticObjectStep(Obj object) {
		this.object = object;
	}

	@Override
	public PathKind getPathKind() {
		return PathKind.RELATIVE_PATH;
	}

	@Override
	public RefUsage getObjectUsage() {
		return RefUsage.CONTAINER_REF_USAGE;
	}

	@Override
	public String toString() {
		if (this.object == null) {
			return super.toString();
		}
		return "&" + this.object;
	}

	@Override
	protected FieldDefinition fieldDefinition(Ref ref) {
		return defaultFieldDefinition(ref);
	}

	@Override
	protected TypeRef ancestor(LocationInfo location, Ref ref) {
		return defaultAncestor(location, ref);
	}

	@Override
	protected TypeRef iface(Ref ref) {
		return ref.toTypeRef();
	}

	@Override
	protected Container resolve(StepResolver resolver) {
		if (resolver.isFullResolution()) {
			this.object.resolveAll();
		}
		resolver.getWalker().object(this, this.object);
		return this.object;
	}

	@Override
	protected void normalize(PathNormalizer normalizer) {
		normalizer.finish();
	}

	@Override
	protected void normalizeStatic(PathNormalizer normalizer) {
		normalizer.finish();
	}

	@Override
	protected PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer) {
		return unchangedPath(toPath());
	}

	@Override
	protected PathOp op(PathOp start) {
		return new Op(start, this);
	}

	private static final class Op extends StepOp<StaticObjectStep> {

		Op(PathOp start, StaticObjectStep step) {
			super(start, step);
		}

		@Override
		public HostValueOp value() {
			return targetValueOp();
		}

		@Override
		public HostOp target(CodeDirs dirs) {

			final ObjectIR objectIR = getStep().object.ir(getGenerator());

			return objectIR.op(getBuilder(), dirs.code());
		}

	}

}

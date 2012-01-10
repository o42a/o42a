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
package org.o42a.core.ref.impl.path;

import static org.o42a.core.ref.path.PathReproduction.unchangedPath;

import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.PathOp;
import org.o42a.core.ir.op.StepOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.path.*;
import org.o42a.core.source.LocationInfo;


public class ObjectStep extends AbstractObjectStep {

	private final Obj object;

	public ObjectStep(Obj object) {
		this.object = object;
	}

	@Override
	public PathKind getPathKind() {
		return PathKind.RELATIVE_PATH;
	}

	@Override
	public PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer) {
		return unchangedPath(toPath());
	}

	@Override
	public PathOp op(PathOp start) {
		return new Op(start, this);
	}

	@Override
	public String toString() {
		if (this.object == null) {
			return super.toString();
		}
		return "&" + this.object;
	}

	@Override
	protected FieldDefinition fieldDefinition(
			BoundPath path,
			Distributor distributor) {
		return defaultFieldDefinition(path, distributor);
	}

	@Override
	protected Obj resolveObject(
			BoundPath path,
			int index,
			Scope start) {
		return this.object;
	}

	@Override
	protected void walkToObject(PathWalker walker, Obj object) {
		walker.object(this, object);
	}

	private static final class Op extends StepOp<ObjectStep> {

		Op(PathOp start, ObjectStep step) {
			super(start, step);
		}

		@Override
		public HostOp target(CodeDirs dirs) {

			final ObjectIR objectIR = getStep().object.ir(dirs.getGenerator());

			return objectIR.op(getBuilder(), dirs.code());
		}

	}

}

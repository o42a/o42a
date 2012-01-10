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
import static org.o42a.core.value.Value.falseValue;

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.path.*;
import org.o42a.core.source.LocationInfo;


public class FalseStep extends Step {

	@Override
	public PathKind getPathKind() {
		return PathKind.ABSOLUTE_PATH;
	}

	@Override
	public RefUsage getObjectUsage() {
		return null;
	}

	@Override
	public String toString() {
		return "FALSE";
	}

	@Override
	protected FieldDefinition fieldDefinition(
			BoundPath path,
			Distributor distributor) {
		return defaultFieldDefinition(path, distributor);
	}

	@Override
	protected Container resolve(
			PathResolver resolver,
			BoundPath path,
			int index,
			Scope start,
			PathWalker walker) {

		final Obj falseObject = start.getContext().getFalse();

		walker.module(this, falseObject);

		return falseObject;
	}

	@Override
	protected PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer) {
		return unchangedPath(toPath());
	}

	@Override
	protected void normalize(PathNormalizer normalizer) {
	}

	@Override
	protected PathOp op(PathOp start) {
		return new Op(start, this);
	}

	private static final class Op extends StepOp<FalseStep> {

		Op(PathOp start, FalseStep step) {
			super(start, step);
		}

		@Override
		public void writeLogicalValue(CodeDirs dirs) {
			dirs.code().go(dirs.falseDir());
		}

		@Override
		public ValOp writeValue(ValDirs dirs) {

			final ValOp result = falseValue().op(getBuilder(), dirs.code());

			dirs.code().go(dirs.falseDir());

			return result;
		}

		@Override
		public HostOp target(CodeDirs dirs) {

			final Obj falseObject = getContext().getFalse();

			return falseObject.ir(getGenerator()).op(getBuilder(), dirs.code());
		}

	}

}

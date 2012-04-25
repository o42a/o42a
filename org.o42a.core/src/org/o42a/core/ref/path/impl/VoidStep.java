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
package org.o42a.core.ref.path.impl;

import static org.o42a.core.ref.Prediction.exactPrediction;
import static org.o42a.core.ref.path.PathReproduction.unchangedPath;
import static org.o42a.core.value.Value.voidValue;

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.path.*;
import org.o42a.core.source.LocationInfo;


public class VoidStep extends Step {

	private static final Inline INLINE_VOID = new Inline();

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
		return "VOID";
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

		final Obj voidObject = start.getContext().getVoid();

		walker.module(this, voidObject);

		return voidObject;
	}

	@Override
	protected void normalize(PathNormalizer normalizer) {
		normalizer.skipStep();
	}

	@Override
	protected void normalizeStatic(PathNormalizer normalizer) {

		final Obj voidObject =
				normalizer.stepStart().getScope().getContext().getVoid();

		normalizer.inline(
				exactPrediction(
						normalizer.lastPrediction(),
						voidObject.getScope()),
				INLINE_VOID);
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

	private static final class Inline extends InlineStep {

		@Override
		public void ignore() {
		}

		@Override
		public ValOp writeValue(ValDirs dirs, HostOp host) {
			return voidValue().op(dirs.getBuilder(), dirs.code());
		}

		@Override
		public void writeCond(CodeDirs dirs, HostOp host) {
		}

		@Override
		public void after(InlineStep preceding) {
		}

		@Override
		public void cancel() {
		}

		@Override
		public String toString() {
			return "VOID";
		}

	}

	private static final class Op extends StepOp<VoidStep> {

		Op(PathOp start, VoidStep step) {
			super(start, step);
		}

		@Override
		public void writeLogicalValue(CodeDirs dirs) {
		}

		@Override
		public ValOp writeValue(ValDirs dirs) {
			return voidValue().op(getBuilder(), dirs.code());
		}

		@Override
		public HostOp target(CodeDirs dirs) {

			final Obj voidObject = getContext().getVoid();

			return voidObject.ir(getGenerator()).op(getBuilder(), dirs.code());
		}

	}

}

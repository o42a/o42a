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
package org.o42a.core.ref.impl.normalizer;

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.InlineValue;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.path.*;
import org.o42a.core.source.LocationInfo;


public abstract class InlineStep extends Step implements NormalStep {

	private final Step step;
	private final InlineValue def;

	public InlineStep(Step step, InlineValue def) {
		this.step = step;
		this.def = def;
	}

	@Override
	public Path appendTo(Path path) {
		return path.append(this);
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
	public String toString() {
		if (this.step == null) {
			return super.toString();
		}
		return "In-line[" + this.step + ']';
	}

	@Override
	protected FieldDefinition fieldDefinition(
			BoundPath path,
			Distributor distributor) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Scope revert(Scope target) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Container resolve(
			PathResolver resolver,
			BoundPath path,
			int index,
			Scope start,
			PathWalker walker) {
		return resolveStep(this.step, resolver, path, index, start, walker);
	}

	@Override
	protected void normalize(PathNormalizer normalizer) {
		normalizer.skip();
	}

	@Override
	protected PathOp op(PathOp start) {
		return new Op(start, this);
	}

	private static final class Op extends StepOp<InlineStep> {

		Op(PathOp start, InlineStep step) {
			super(start, step);
		}

		@Override
		public void writeLogicalValue(CodeDirs dirs) {
			getStep().def.writeCond(dirs, host());
		}

		@Override
		public ValOp writeValue(ValDirs dirs) {
			return getStep().def.writeValue(dirs, host());
		}

		@Override
		public HostOp target(CodeDirs dirs) {
			throw new UnsupportedOperationException();
		}

	}

}

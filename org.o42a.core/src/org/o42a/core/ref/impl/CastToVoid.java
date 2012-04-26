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
package org.o42a.core.ref.impl;

import static org.o42a.core.ref.path.PathReproduction.reproducedPath;
import static org.o42a.core.value.Value.voidValue;

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;


final class CastToVoid extends Step {

	static final CastToVoid CAST_TO_VOID = new CastToVoid();

	private CastToVoid() {
	}

	@Override
	public PathKind getPathKind() {
		return PathKind.RELATIVE_PATH;
	}

	@Override
	public RefUsage getObjectUsage() {
		return RefUsage.LOGICAL_REF_USAGE;
	}

	@Override
	public String toString() {
		return "@@void";
	}

	@Override
	protected TypeRef ancestor(
			BoundPath path,
			LocationInfo location,
			Distributor distributor) {
		return ValueType.VOID.typeRef(location, distributor.getScope());
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
		walker.skip(this, start);
		return start.getContainer();
	}

	@Override
	protected void normalize(PathNormalizer normalizer) {
		normalizer.inline(normalizer.lastPrediction(), new Inline());
	}

	@Override
	protected void normalizeStatic(PathNormalizer normalizer) {
		normalize(normalizer);
	}

	@Override
	protected PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer) {
		return reproducedPath(toPath());
	}

	@Override
	protected PathOp op(PathOp start) {
		return new Op(start, this);
	}

	private static final class Inline extends InlineStep {

		private InlineStep preceding;

		@Override
		public void ignore() {
		}

		@Override
		public void after(InlineStep preceding) {
			this.preceding = preceding;
		}

		@Override
		public void writeCond(CodeDirs dirs, HostOp host) {
			this.preceding.writeCond(dirs, host);
		}

		@Override
		public ValOp writeValue(ValDirs dirs, HostOp host) {
			assert dirs.getValueStruct().assertIs(ValueStruct.VOID);

			writeCond(dirs.dirs(), host);

			return voidValue().op(dirs.getBuilder(), dirs.code());
		}

		@Override
		public void cancel() {
		}

		@Override
		public String toString() {
			return "@@void";
		}

	}

	private static final class Op extends StepOp<CastToVoid> {

		Op(PathOp start, CastToVoid step) {
			super(start, step);
		}

		@Override
		public void writeCond(CodeDirs dirs) {
			start().writeCond(dirs);
		}

		@Override
		public ValOp writeValue(ValDirs dirs) {
			assert dirs.getValueStruct().assertIs(ValueStruct.VOID);

			writeCond(dirs.dirs());

			return voidValue().op(dirs.getBuilder(), dirs.code());
		}

		@Override
		public HostOp target(CodeDirs dirs) {
			return start().target(dirs);
		}

	}

}

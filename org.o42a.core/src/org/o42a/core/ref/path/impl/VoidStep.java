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

import static org.o42a.core.ref.Prediction.exactPrediction;
import static org.o42a.core.ref.path.PathReproduction.unchangedPath;

import org.o42a.codegen.code.Code;
import org.o42a.core.Container;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.string.ID;


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
	protected FieldDefinition fieldDefinition(Ref ref) {
		return defaultFieldDefinition(ref);
	}

	@Override
	protected TypeRef ancestor(LocationInfo location, Ref ref) {
		return ref.toTypeRef();
	}

	@Override
	protected TypeRef iface(Ref ref) {
		return ref.toTypeRef();
	}

	@Override
	protected Container resolve(StepResolver resolver) {

		final Obj voidObject = resolver.getContext().getVoid();

		resolver.getWalker().module(this, voidObject);

		return voidObject;
	}

	@Override
	protected Ref statefulRef(Ref ref) {
		return defaultStatefulRef(ref);
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
	protected HostOp op(HostOp host) {
		return new VoidOp(host, this);
	}

	@Override
	protected RefTargetIR targetIR(RefIR refIR) {
		throw new UnsupportedOperationException();
	}

	private static final class Inline extends InlineStep {

		@Override
		public void ignore() {
		}

		@Override
		public ValOp writeValue(ValDirs dirs, HostOp host) {
			return dirs.getBuilder().voidVal(dirs.code());
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

	private static final class VoidOp
			extends StepOp<VoidStep>
			implements HostValueOp {

		VoidOp(HostOp host, VoidStep step) {
			super(host, step);
		}

		@Override
		public HostValueOp value() {
			return this;
		}

		@Override
		public HostTargetOp target() {
			return pathTargetOp();
		}

		@Override
		public void writeCond(CodeDirs dirs) {
		}

		@Override
		public ValOp writeValue(ValDirs dirs) {
			return dirs.getBuilder().voidVal(dirs.code());
		}

		@Override
		public void assign(CodeDirs dirs, HostOp value) {
			throw new UnsupportedOperationException(
					this + " is not assignable");
		}

		@Override
		public HostOp pathTarget(CodeDirs dirs) {
			return voidObject(dirs.code());
		}

		@Override
		protected TargetStoreOp allocateStore(ID id, Code code) {
			return voidObject(code).allocateStore(id, code);
		}

		private ObjOp voidObject(Code code) {

			final Obj voidObject = getContext().getVoid();

			return voidObject.ir(getGenerator()).op(getBuilder(), code);
		}

	}

}

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

import static org.o42a.core.ir.value.Val.FALSE_VAL;
import static org.o42a.core.ir.value.Val.falseVal;
import static org.o42a.core.ref.Prediction.exactPrediction;
import static org.o42a.core.ref.path.PathReproduction.unchangedPath;

import org.o42a.codegen.code.Code;
import org.o42a.core.Container;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.impl.ConstValOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.string.ID;


public class NoneStep extends Step {

	private static final Inline INLINE_NONE = new Inline();
	private static final NoneStoreOp NONE_STORE = new NoneStoreOp();

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
		return "NONE";
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

		final Obj noneObject = resolver.getContext().getNone();

		resolver.getWalker().module(this, noneObject);

		return noneObject;
	}

	@Override
	protected PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer) {
		return unchangedPath(toPath());
	}

	@Override
	protected void normalize(PathNormalizer normalizer) {
		normalizer.skipStep();
	}

	@Override
	protected void normalizeStatic(PathNormalizer normalizer) {

		final Obj noneObject =
				normalizer.stepStart().getScope().getContext().getNone();

		normalizer.inline(
				exactPrediction(
						normalizer.lastPrediction(),
						noneObject.getScope()),
				INLINE_NONE);
	}

	@Override
	protected PathOp op(HostOp host) {
		return new NoneOp(host, this);
	}

	@Override
	protected RefTargetIR targetIR(RefIR refIR) {
		throw new UnsupportedOperationException();
	}

	private static ObjOp noneObject(CodeDirs dirs) {

		final CodeBuilder builder = dirs.getBuilder();
		final Obj none = builder.getContext().getNone();

		return none.ir(dirs.getGenerator()).op(builder, dirs.code());
	}

	private static final class Inline extends InlineStep {

		@Override
		public void ignore() {
		}

		@Override
		public ValOp writeValue(ValDirs dirs, HostOp host) {
			dirs.code().go(dirs.falseDir());
			return new ConstValOp(
					dirs.getBuilder(),
					dirs.value().ptr(),
					FALSE_VAL);
		}

		@Override
		public void writeCond(CodeDirs dirs, HostOp host) {
			dirs.code().go(dirs.falseDir());
		}

		@Override
		public void after(InlineStep preceding) {
		}

		@Override
		public void cancel() {
		}

		@Override
		public String toString() {
			return "NONE";
		}

	}

	private static final class NoneOp
			extends StepOp<NoneStep>
			implements HostValueOp {

		NoneOp(HostOp host, NoneStep step) {
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
			dirs.code().go(dirs.falseDir());
		}

		@Override
		public ValOp writeValue(ValDirs dirs) {
			dirs.code().go(dirs.falseDir());
			return new ConstValOp(
					dirs.getBuilder(),
					dirs.value().ptr(),
					falseVal(dirs.getValueType()));
		}

		@Override
		public void assign(CodeDirs dirs, HostOp value) {
			throw new UnsupportedOperationException(
					this + " is not assignable");
		}

		@Override
		public HostOp pathTarget(CodeDirs dirs) {
			dirs.code().go(dirs.falseDir());
			return noneObject(dirs);
		}

		@Override
		protected TargetStoreOp allocateStore(ID id, Code code) {
			return NONE_STORE;
		}

	}

	private static final class NoneStoreOp implements TargetStoreOp {

		@Override
		public void storeTarget(CodeDirs dirs) {
			dirs.code().go(dirs.falseDir());
		}

		@Override
		public TargetOp loadTarget(CodeDirs dirs) {
			dirs.code().go(dirs.falseDir());
			return noneObject(dirs);
		}

		@Override
		public String toString() {
			return "NONE";
		}

	}

}

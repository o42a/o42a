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

import static org.o42a.core.ir.value.Val.falseVal;
import static org.o42a.core.ref.Prediction.exactPrediction;
import static org.o42a.core.ref.path.PathReproduction.unchangedPath;

import java.util.function.Function;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.core.Container;
import org.o42a.core.ir.field.local.LocalIROp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.Val;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.impl.ConstValOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.LocalRegistry;
import org.o42a.util.string.ID;


public class FalseStep extends Step {

	private static final Inline INLINE_FALSE = new Inline();

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
	protected FieldDefinition fieldDefinition(Ref ref) {
		return defaultFieldDefinition(ref);
	}

	@Override
	protected void localMember(LocalRegistry registry) {
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

		final Obj falseObject = resolver.getContext().getFalse();

		resolver.getWalker().module(this, falseObject);

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
		normalizer.skipStep();
	}

	@Override
	protected void normalizeStatic(PathNormalizer normalizer) {

		final Obj falseObject =
				normalizer.stepStart().getScope().getContext().getFalse();

		normalizer.inline(
				exactPrediction(
						normalizer.lastPrediction(),
						falseObject.getScope()),
				INLINE_FALSE);
	}

	@Override
	protected HostOp op(HostOp host) {
		return new FalseOp(host, this);
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

			final Block code = dirs.code();
			final ConstValOp result = new ConstValOp(
					dirs.getBuilder(),
					dirs.value().ptr(code),
					Val.FALSE_VAL);

			code.go(dirs.falseDir());

			return result;
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
			return "FALSE";
		}

	}

	private static final class FalseOp
			extends StepOp<FalseStep>
			implements HostValueOp {

		FalseOp(HostOp host, FalseStep step) {
			super(host, step);
		}

		@Override
		public HostValueOp value() {
			return this;
		}

		@Override
		public void writeCond(CodeDirs dirs) {
			dirs.code().go(dirs.falseDir());
		}

		@Override
		public ValOp writeValue(ValDirs dirs) {

			final Block code = dirs.code();
			final ConstValOp result = new ConstValOp(
					dirs.getBuilder(),
					dirs.value().ptr(code),
					falseVal(dirs.getValueType()));

			code.go(dirs.falseDir());

			return result;
		}

		@Override
		public void assign(CodeDirs dirs, HostOp value) {
			throw new UnsupportedOperationException(
					this + " is not assignable");
		}

		@Override
		public ObjOp pathTarget(CodeDirs dirs) {
			return falseIR().exactOp(dirs);
		}

		@Override
		public TargetStoreOp allocateStore(ID id, Code code) {
			return falseIR().exactTargetStore(id);
		}

		@Override
		public TargetStoreOp localStore(
				ID id,
				Function<CodeDirs, LocalIROp> getLocal) {
			return falseIR().exactTargetStore(id);
		}

		private final ObjectIR falseIR() {
			return getContext().getFalse().ir(getGenerator());
		}

	}

}

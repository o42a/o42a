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

import static org.o42a.core.ir.value.Val.FALSE_VAL;
import static org.o42a.core.ir.value.Val.falseVal;
import static org.o42a.core.ref.Prediction.exactPrediction;
import static org.o42a.core.ref.path.PathReproduction.unchangedPath;

import java.util.function.Function;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.core.Container;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.field.local.LocalIROp;
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
import org.o42a.core.st.sentence.LocalRegistry;
import org.o42a.util.string.ID;


public class NoneStep extends Step {

	private static final Inline INLINE_NONE = new Inline();

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
	protected void localMember(LocalRegistry registry) {
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
	protected HostOp op(HostOp host) {
		return new NoneOp(host, this);
	}

	@Override
	protected RefTargetIR targetIR(RefIR refIR) {
		throw new UnsupportedOperationException();
	}

	private static ObjOp noneObject(CodeDirs dirs, OpPresets presets) {

		final CodeBuilder builder = dirs.getBuilder();
		final Obj none = builder.getContext().getNone();

		return none.ir(dirs.getGenerator()).exactOp(dirs).setPresets(presets);
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
					FALSE_VAL);

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
			return "NONE";
		}

	}

	private static final class NoneOp
			extends StepOp<NoneStep>
			implements HostValueOp {

		NoneOp(HostOp host, NoneStep step) {
			super(host, step);
		}

		private NoneOp(NoneOp proto, OpPresets presets) {
			super(proto, presets);
		}

		@Override
		public final NoneOp setPresets(OpPresets presets) {
			if (presets.is(getPresets())) {
				return this;
			}
			return new NoneOp(this, presets);
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
		public HostOp pathTarget(CodeDirs dirs) {
			dirs.code().go(dirs.falseDir());
			return noneObject(dirs, getPresets());
		}

		@Override
		public TargetStoreOp allocateStore(ID id, Code code) {
			return new NoneStoreOp(this);
		}

		@Override
		public TargetStoreOp localStore(
				ID id,
				Function<CodeDirs, LocalIROp> getLocal) {
			return new NoneStoreOp(this);
		}

	}

	private static final class NoneStoreOp implements TargetStoreOp {

		private final NoneOp op;

		NoneStoreOp(NoneOp op) {
			this.op = op;
		}

		@Override
		public void storeTarget(CodeDirs dirs) {
			dirs.code().go(dirs.falseDir());
		}

		@Override
		public HostOp loadTarget(CodeDirs dirs) {
			dirs.code().go(dirs.falseDir());
			return noneObject(dirs, this.op.getPresets());
		}

		@Override
		public String toString() {
			return "NONE";
		}

	}

}

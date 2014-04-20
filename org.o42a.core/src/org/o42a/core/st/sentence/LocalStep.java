/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.core.st.sentence;

import static org.o42a.core.member.AccessSource.FROM_DEFINITION;
import static org.o42a.core.member.MemberName.localName;
import static org.o42a.core.ref.RefUsage.CONTAINER_REF_USAGE;
import static org.o42a.core.ref.path.PathReproduction.reproducedPath;

import org.o42a.analysis.Analyzer;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Data;
import org.o42a.codegen.data.SubData;
import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.ir.cmd.LocalOp;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.Accessor;
import org.o42a.core.member.MemberPath;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.path.impl.ObjectStepUses;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.string.ID;


final class LocalStep extends Step {

	private final Local local;
	private ObjectStepUses uses;

	LocalStep(Local local) {
		this.local = local;
	}

	public final Local local() {
		return this.local;
	}

	@Override
	public final PathKind getPathKind() {
		return PathKind.RELATIVE_PATH;
	}

	@Override
	public final RefUsage getObjectUsage() {
		return RefUsage.CONTAINER_REF_USAGE;
	}

	@Override
	public String toString() {
		if (this.local == null) {
			return super.toString();
		}
		return this.local.toString();
	}

	@Override
	protected void rebuild(PathRebuilder rebuilder) {
		if (!rebuilder.isStatic()) {
			// Locals should never be statically referenced.
			rebuilder.combinePreviousWithLocal(local());
		}
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
		return local().originalRef().getInterface().prefixWith(
				refPrefix(ref));
	}

	@Override
	protected Container resolve(StepResolver resolver) {

		final Scope start = resolver.getStart();
		final Resolver refResolver = start.resolver();

		if (resolver.isFullResolution()) {
			uses().useBy(resolver);

			final RefUsage usage;

			if (resolver.isLastStep()) {
				// Resolve only the last value.
				usage = resolver.refUsage();
			} else {
				usage = CONTAINER_REF_USAGE;
			}

			local().ref().resolveAll(
					refResolver.fullResolver(resolver.refUser(), usage));
		}

		final Obj resolution =
				local().ref().resolve(refResolver).toObject();

		resolver.getWalker().local(this, start, local());

		return resolution.toObject();
	}

	@Override
	protected void normalize(PathNormalizer normalizer) {
		normalizer.cancel();
	}

	@Override
	protected void normalizeStatic(PathNormalizer normalizer) {
		normalizer.cancel();// Locals should never be statically referenced.
	}

	@Override
	protected void normalizeStep(Analyzer analyzer) {
		local().ref().normalize(analyzer);
	}

	@Override
	protected PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer) {

		final MemberPath path =
				reproducer.getReproducer()
				.getContainer()
				.member(
						Accessor.OWNER.accessBy(local(), FROM_DEFINITION),
						localName(local().getName()),
						reproducer.getScope().toObject());

		if (path == null) {
			return null;
		}

		final Local local = path.toLocal();

		if (local != null) {
			return reproducedPath(local.toPath());
		}

		return null;
	}

	@Override
	protected Ref statefulRef(Ref ref) {

		final Ref oldRef = local().ref();
		final Ref newRef = oldRef.toStateful();

		if (oldRef == newRef) {
			return ref;
		}

		final Local local = new Local(local(), local().getName(), newRef);
		final Path prefix = ref.getPath().getPath().cut(1);

		return prefix.append(new LocalStep(local))
				.bind(ref, ref.getScope())
				.target(ref.distribute());
	}

	@Override
	protected LocalStepOp op(HostOp host) {
		return new LocalStepOp(host, this);
	}

	@Override
	protected RefTargetIR targetIR(RefIR refIR) {
		return new LocalRefTargetIR(refIR.getGenerator(), this);
	}

	private final ObjectStepUses uses() {
		if (this.uses != null) {
			return this.uses;
		}
		return this.uses = new ObjectStepUses(this);
	}

	private PrefixPath refPrefix(Ref ref) {
		return ref.getPath().cut(1).toPrefix(ref.getScope());
	}

	private static final class LocalStepOp
			extends StepOp<LocalStep>
			implements HostValueOp {

		private LocalOp op;

		public LocalStepOp(HostOp host, LocalStep local) {
			super(host, local);
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
			op(dirs).writeCond(dirs);
		}

		@Override
		public ValOp writeValue(ValDirs dirs) {
			return op(dirs.dirs()).writeValue(dirs);
		}

		@Override
		public void assign(CodeDirs dirs, HostOp value) {
			pathValueOp().assign(dirs, value);
		}

		@Override
		public HostOp pathTarget(CodeDirs dirs) {
			return op(dirs).target(dirs);
		}

		@Override
		protected TargetStoreOp allocateStore(ID id, Code code) {

			final LocalOp op =
					getBuilder()
					.localsOf(code.getAllocator())
					.get(getStep().local());

			return new LocalStoreOp(op);
		}

		private LocalOp op(CodeDirs dirs) {
			if (this.op != null) {
				return this.op;
			}
			return this.op = dirs.locals().get(getStep().local());
		}

	}

	private static final class LocalStoreOp implements TargetStoreOp {

		private final LocalOp local;

		LocalStoreOp(LocalOp local) {
			this.local = local;
		}

		@Override
		public void storeTarget(CodeDirs dirs) {
		}

		@Override
		public TargetOp loadTarget(CodeDirs dirs) {
			return this.local.target(dirs);
		}

		@Override
		public String toString() {
			if (this.local == null) {
				return super.toString();
			}
			return this.local.toString();
		}

	}

	private static final class LocalRefTargetIR
			extends PathIR
			implements RefTargetIR {

		private final LocalStep local;
		private final RefTargetIR targetIR;

		LocalRefTargetIR(Generator generator, LocalStep local) {
			this.local = local;

			final Ref ref = this.local.local().ref();
			final RefIR refIR = ref.ir(generator);
			final Step lastStep = ref.getPath().lastStep();

			this.targetIR = stepTargetIR(refIR, lastStep);
		}

		@Override
		public Data<?> allocate(ID id, SubData<?> data) {
			return this.targetIR.allocate(id, data);
		}

		@Override
		public RefTargetOp op(Code code, StructOp<?> data) {
			return new LocalRefTargetOp(this, this.targetIR.op(code, data));
		}

		@Override
		public String toString() {
			if (this.local == null) {
				return super.toString();
			}
			return this.local.toString();
		}

	}

	private static final class LocalRefTargetOp implements RefTargetOp {

		private final LocalRefTargetIR ir;
		private final RefTargetOp target;

		LocalRefTargetOp(LocalRefTargetIR ir, RefTargetOp target) {
			this.ir = ir;
			this.target = target;
		}

		@Override
		public DumpablePtrOp<?> ptr() {
			return this.target.ptr();
		}

		@Override
		public void storeTarget(CodeDirs dirs, HostOp host) {

			final LocalStepOp op = this.ir.local.op(host);
			final TargetStoreOp store = op.op(dirs).getTargetStore();

			this.target.copyTarget(dirs, store);
		}

		@Override
		public void copyTarget(CodeDirs dirs, TargetStoreOp store) {

			final LocalStoreOp localStore = (LocalStoreOp) store;
			final TargetStoreOp st = localStore.local.getTargetStore();

			this.target.copyTarget(dirs, st);
		}

		@Override
		public TargetOp loadTarget(CodeDirs dirs) {
			return this.target.loadTarget(dirs);
		}

		@Override
		public DataOp toData(ID id, Code code) {
			return ptr().toData(id, code);
		}

		@Override
		public AnyOp toAny(ID id, Code code) {
			return ptr().toAny(id, code);
		}

	}

}
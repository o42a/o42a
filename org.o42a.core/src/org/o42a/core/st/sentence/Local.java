/*
    Compiler Core
    Copyright (C) 2013 Ruslan Lopatin

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
import static org.o42a.util.string.Capitalization.CASE_SENSITIVE;

import org.o42a.analysis.Analyzer;
import org.o42a.codegen.code.Code;
import org.o42a.core.*;
import org.o42a.core.ir.cmd.LocalOp;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.*;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.path.impl.ObjectStepUses;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.string.ID;
import org.o42a.util.string.Name;


public final class Local extends Step implements ContainerInfo, MemberPath {

	public static final Name ANONYMOUS_LOCAL_NAME =
			CASE_SENSITIVE.canonicalName("L");
	public static final MemberName ANONYMOUS_LOCAL_MEMBER =
			localName(ANONYMOUS_LOCAL_NAME);

	private final Location location;
	private final Name name;
	private final MemberName memberId;
	private final Ref ref;
	private ObjectStepUses uses;

	Local(LocationInfo location, Name name, Ref ref) {
		assert name != null :
			"Local name not specified";
		assert ref != null :
			"Local reference not specified";
		this.location = location.getLocation();
		this.name = name;
		this.ref = ref;
		this.memberId = localName(name);
	}

	public final Name getName() {
		return this.name;
	}

	public final MemberName getMemberId() {
		return this.memberId;
	}

	public final Ref ref() {
		return this.ref;
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
	public final Location getLocation() {
		return this.location;
	}

	@Override
	public final Scope getScope() {
		return ref().getScope();
	}

	@Override
	public final Container getContainer() {
		return ref().getContainer();
	}

	public Ref toRef() {
		return toPath().bind(this, getScope()).target(distribute());
	}

	@Override
	public final Path pathToMember() {
		return toPath();
	}

	@Override
	public final Member toMember() {
		return null;
	}

	@Override
	public final Local toLocal() {
		return this;
	}

	@Override
	public final Distributor distribute() {
		return Contained.distribute(this);
	}

	@Override
	public final Distributor distributeIn(Container container) {
		return Contained.distributeIn(this, container);
	}

	@Override
	public final void assertScopeIs(Scope scope) {
		Scoped.assertScopeIs(this, scope);
	}

	@Override
	public final void assertCompatible(Scope scope) {
		Scoped.assertCompatible(this, scope);
	}

	@Override
	public final void assertSameScope(ScopeInfo other) {
		Scoped.assertSameScope(this, other);
	}

	@Override
	public final void assertCompatibleScope(ScopeInfo other) {
		Scoped.assertCompatibleScope(this, other);
	}

	@Override
	public String toString() {
		if (this.name == null) {
			return super.toString();
		}
		return this.name.toString();
	}

	@Override
	protected void rebuild(PathRebuilder rebuilder) {
		if (!rebuilder.isStatic()) {
			// Locals should never be statically referenced.
			rebuilder.combinePreviousWithLocal(this);
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
		return ref().getInterface().prefixWith(refPrefix(ref));
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

			ref().resolveAll(
					refResolver.fullResolver(resolver.refUser(), usage));
		}

		final Obj resolution = ref().resolve(refResolver).toObject();

		resolver.getWalker().local(start, this);

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
		ref().normalize(analyzer);
	}

	@Override
	protected PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer) {

		final MemberPath path =
				reproducer.getReproducer()
				.getContainer()
				.member(
						Accessor.OWNER.accessBy(this, FROM_DEFINITION),
						localName(getName()),
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
	protected PathOp op(PathOp start) {
		return new Op(start, this);
	}

	@Override
	protected RefTargetIR targetIR(RefIR refIR) {
		return defaultTargetIR(refIR);
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

	private static final class Op extends PathOp implements HostValueOp {

		private final Local local;
		private LocalOp op;

		public Op(PathOp start, Local local) {
			super(start);
			this.local = local;
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
		public String toString() {
			if (this.local == null) {
				return super.toString();
			}
			return this.local.toString();
		}

		@Override
		protected TargetStoreOp allocateStore(ID id, Code code) {

			final LocalOp op =
					getBuilder().localsOf(code.getAllocator()).get(this.local);

			return new LocalStoreOp(op);
		}

		private LocalOp op(CodeDirs dirs) {
			if (this.op != null) {
				return this.op;
			}
			return this.op = dirs.locals().get(this.local);
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

}

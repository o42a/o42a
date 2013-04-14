/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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
package org.o42a.core.object.state;

import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;
import static org.o42a.core.ir.object.state.DepIR.DEP_IR;
import static org.o42a.core.ir.object.state.DepOp.createDep;
import static org.o42a.core.ref.RefUsage.BODY_REF_USAGE;
import static org.o42a.core.ref.RefUsage.CONTAINER_REF_USAGE;
import static org.o42a.core.ref.RefUser.dummyRefUser;
import static org.o42a.core.ref.RefUser.rtRefUser;
import static org.o42a.core.ref.path.PathResolver.fullPathResolver;
import static org.o42a.core.ref.path.PathResolver.pathResolver;
import static org.o42a.core.ref.path.PathWalker.DUMMY_PATH_WALKER;

import org.o42a.analysis.Analyzer;
import org.o42a.analysis.use.ProxyUser;
import org.o42a.codegen.code.Allocator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.object.state.DepIR;
import org.o42a.core.ir.object.state.DepOp;
import org.o42a.core.ir.op.*;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.path.impl.ObjectStepUses;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.link.Link;
import org.o42a.util.string.ID;
import org.o42a.util.string.Name;
import org.o42a.util.string.SubID;


public final class Dep extends Step implements SubID {

	private final Obj declaredIn;
	private final Ref ref;
	private final Name name;
	private final ID id;
	private final Obj target;
	private ObjectStepUses uses;
	private ProxyUser<RefUsage> depUser;
	private byte enabled;
	private byte compileTimeOnly;
	private SyntheticDep synthetic;
	private Analyzer syntheticAnalyzer;
	private boolean isSynthetic;

	Dep(Obj declaredIn, Ref ref, Name name, ID id) {
		this.declaredIn = declaredIn;
		this.ref = ref;
		this.name = name;
		this.id = id;
		this.target = target();
		assert !this.target.getConstructionMode().isRuntime()
			|| this.target.getConstructionMode().isPredefined():
			"Can not find an interface of run-time constructed dependency";
	}

	public final Obj getDeclaredIn() {
		return this.declaredIn;
	}

	public final Name getName() {
		return this.name;
	}

	public final Ref ref() {
		return this.ref;
	}

	public final Scope enclosingScope(Scope scope) {
		if (getDeclaredIn().getScope().is(scope)) {
			return scope.getEnclosingScope();
		}
		return walkToEnclosingScope(scope, DUMMY_PATH_WALKER);
	}

	public Scope walkToEnclosingScope(Scope scope, PathWalker walker) {

		final PathResolver resolver = pathResolver(scope, dummyRefUser());

		return walkToEnclosingScope(resolver, walker);
	}

	public final Object getDepKey() {

		final Path path = this.ref.getPath().getPath();
		final Path template = path.getTemplate();

		return template != null ? template : path;
	}

	public final Obj getDepTarget() {
		return this.target;
	}

	public final boolean exists(Analyzer analyzer) {
		return isEnabled() && !isSynthetic(analyzer);
	}

	public final boolean isEnabled() {
		if (this.enabled != 0) {
			return this.enabled > 0;
		}
		return this.compileTimeOnly < 0;
	}

	public final void setSynthetic(SyntheticDep synthetic) {
		this.synthetic = synthetic;
	}

	@Override
	public final PathKind getPathKind() {
		return PathKind.RELATIVE_PATH;
	}

	@Override
	public RefUsage getObjectUsage() {
		return RefUsage.CONTAINER_REF_USAGE;
	}

	@Override
	public final ID toID() {
		return this.id;
	}

	@Override
	public final ID toDisplayID() {
		return this.id;
	}

	@Override
	public String toString() {
		if (this.id == null) {
			return super.toString();
		}
		return this.id.toString();
	}

	@Override
	protected FieldDefinition fieldDefinition(Ref ref) {
		return ref().toFieldDefinition().prefixWith(refPrefix(ref));
	}

	@Override
	protected TypeRef ancestor(LocationInfo location, Ref ref) {
		return ref().ancestor(location).prefixWith(refPrefix(ref));
	}

	@Override
	protected TypeRef iface(Ref ref) {
		return ref().getInterface().prefixWith(refPrefix(ref));
	}

	@Override
	protected Container resolve(StepResolver resolver) {

		final Obj object = resolver.getStart().toObject();

		assert object != null :
			"Dependency should be resolved against object, but were not: "
			+ resolver.getStart();

		final Scope enclosingScope = up(resolver);
		final Resolver enclosingResolver = enclosingScope.resolver();

		if (resolver.isFullResolution()) {
			compileTimeOnly(resolver.refUsage().isCompileTimeOnly());
			uses().useBy(resolver);

			if (!resolver.refUsage().isCompileTimeOnly()
					&& this.depUser == null) {
				this.depUser = new ProxyUser<>(uses().uses().toUser());
				ref().resolveAll(
						enclosingResolver.fullResolver(
								rtRefUser(this.depUser, this.depUser),
						BODY_REF_USAGE));
			}

			final RefUsage usage;

			if (resolver.isLastStep()) {
				// Resolve only the last value.
				usage = resolver.refUsage();
			} else {
				usage = CONTAINER_REF_USAGE;
			}

			ref().resolveAll(
					enclosingResolver.fullResolver(resolver.refUser(), usage));

			final ObjectDeps deps = getDeclaredIn().deps();

			deps.depResolved(this);
		}

		final Obj resolution = ref().resolve(enclosingResolver).toObject();

		resolver.getWalker().dep(object, this);

		return resolution.toObject();
	}

	@Override
	protected final void normalize(PathNormalizer normalizer) {

		final Obj object = normalizer.lastPrediction().getScope().toObject();
		final Scope objectScope = object.getScope();
		final Scope enclosingScope = enclosingScope(objectScope);

		if (!normalizer.up(
				enclosingScope,
				objectScope.getEnclosingScopePath(),
				new ReversePath() {
					@Override
					public Scope revert(Scope target) {
						return object.meta().findIn(target).getScope();
					}
				})) {
			return;
		}

		normalizer.skip(normalizer.lastPrediction(), new DepDisabler());
		normalizer.append(
				ref().getPath(),
				uses().nestedNormalizer(normalizer));
	}

	@Override
	protected void normalizeStatic(PathNormalizer normalizer) {
		throw new UnsupportedOperationException(
				"Dep can not be a part of static path " + normalizer.getPath());
	}

	@Override
	protected Path nonNormalizedRemainder(PathNormalizer normalizer) {
		return ref().getPath().getPath();
	}

	@Override
	protected void normalizeStep(Analyzer analyzer) {
		ref().normalize(analyzer);
	}

	@Override
	protected PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer) {
		reproducer.getLogger().notReproducible(ref().getLocation());
		return null;
	}

	@Override
	protected final PathOp op(PathOp start) {
		if (isSynthetic(start.getGenerator().getAnalyzer())) {
			return new SyntheticOp(start, this);
		}
		assert exists(start.getGenerator().getAnalyzer()) :
			this + " does not exist";
		return new Op(start, this);
	}

	final void reuseDep() {
		if (this.enabled < 0) {
			this.enabled = 0;
		}
	}

	private PrefixPath refPrefix(Ref ref) {
		return ref.getPath()
				.cut(1)
				.append(getDeclaredIn().getScope().getEnclosingScopePath())
				.toPrefix(ref.getScope());
	}

	private final ObjectStepUses uses() {
		if (this.uses != null) {
			return this.uses;
		}
		return this.uses = new ObjectStepUses(this);
	}

	private boolean isSynthetic(Analyzer analyzer) {
		if (this.syntheticAnalyzer == analyzer) {
			return this.isSynthetic;
		}
		this.syntheticAnalyzer = analyzer;
		if (this.synthetic == null) {
			return this.isSynthetic = false;
		}
		if (!this.synthetic.isSynthetic(analyzer, this)) {
			return this.isSynthetic = false;
		}
		this.isSynthetic = true;
		if (this.depUser != null) {
			this.depUser.setProxied(null);
		}
		return true;
	}

	private Scope up(StepResolver resolver) {

		final PathResolver pathResolver;

		if (resolver.isFullResolution()) {
			pathResolver = fullPathResolver(
					resolver.getStart(),
					resolver.refUser(),
					CONTAINER_REF_USAGE);
		} else {
			pathResolver = pathResolver(
					resolver.getStart(),
					resolver.refUser());
		}

		return walkToEnclosingScope(pathResolver, DUMMY_PATH_WALKER);
	}

	private Scope walkToEnclosingScope(
			PathResolver resolver,
			PathWalker walker) {
		getDeclaredIn().assertCompatible(resolver.getPathStart());

		final Path enclosingPath =
				getDeclaredIn().getScope().getEnclosingScopePath();
		final PathResolution enclosing =
				enclosingPath.bind(ref(), resolver.getPathStart())
				.walk(resolver, walker);

		return enclosing.getResult().getScope();
	}

	private void ignoreDep() {
		if (this.enabled == 0) {
			this.enabled = -1;
			this.depUser.setProxied(null);
		}
	}

	private void enableDep() {
		this.enabled = 1;
		this.depUser.setProxied(uses().uses().toUser());
	}

	private void compileTimeOnly(boolean compileTimeOnly) {
		if (!compileTimeOnly) {
			this.compileTimeOnly = -1;
		} else if (this.compileTimeOnly == 0) {
			this.compileTimeOnly = 1;
		}
	}

	private Obj target() {

		final Scope enclosingScope =
				this.declaredIn.getScope().getEnclosingScope();
		final Obj target =
				ref().resolve(enclosingScope.resolver()).toObject();

		if (!target.getConstructionMode().isRuntime()
				|| target.getConstructionMode().isPredefined()) {
			return target;
		}

		final Link link = target.getDereferencedLink();

		if (link != null) {
			return link.getInterfaceRef().getType();
		}

		return target.type().getAncestor().getType();
	}

	private final class DepDisabler extends NormalAppender {

		@Override
		public Path appendTo(Path path) {
			ignoreDep();
			return path;
		}

		@Override
		public void ignore() {
			ignoreDep();
		}

		@Override
		public void cancel() {
			enableDep();
		}

		@Override
		public String toString() {
			return "-";
		}

	}

	private static final class Op extends StepOp<Dep> {

		Op(PathOp start, Dep step) {
			super(start, step);
		}

		@Override
		public HostValueOp value() {
			return pathValueOp();
		}

		@Override
		public HostTargetOp target() {
			return pathTargetOp();
		}

		@Override
		public DepOp pathTarget(CodeDirs dirs) {

			final ObjHolder holder = tempObjHolder(dirs.getAllocator());

			return dep(dirs, holder);
		}

		@Override
		protected TargetStoreOp allocateStore(ID id, Code code) {

			final StructRecOp<DepIR.Op> ptr =
					code.allocatePtr(id, DEP_IR);

			return new DepStepStoreOp(this, code.getAllocator(), ptr);
		}

		private DepOp dep(CodeDirs dirs, ObjHolder holder) {
			return start()
					.target()
					.materialize(dirs, holder)
					.dep(dirs, getStep());
		}

	}

	private static final class DepStepStoreOp implements TargetStoreOp {

		private final Op op;
		private final Allocator allocator;
		private final StructRecOp<DepIR.Op> ptr;
		private DepOp dep;

		DepStepStoreOp(Op op, Allocator allocator, StructRecOp<DepIR.Op> ptr) {
			this.op = op;
			this.allocator = allocator;
			this.ptr = ptr;
		}

		@Override
		public void storeTarget(CodeDirs dirs) {
			this.dep = this.op.dep(dirs, tempObjHolder(this.allocator));
			this.ptr.store(dirs.code(), this.dep.ptr());
		}

		@Override
		public TargetOp loadTarget(CodeDirs dirs) {
			return createDep(this.dep, this.ptr.load(null, dirs.code()));
		}

		@Override
		public String toString() {
			if (this.ptr == null) {
				return super.toString();
			}
			return this.ptr.toString();
		}

	}

	private static final class SyntheticOp extends StepOp<Dep> {

		private final Dep dep;

		SyntheticOp(PathOp start, Dep dep) {
			super(start, dep);
			this.dep = dep;
		}

		@Override
		public HostValueOp value() {
			return pathValueOp();
		}

		@Override
		public HostTargetOp target() {
			return pathTargetOp();
		}

		@Override
		public HostOp pathTarget(CodeDirs dirs) {
			return path();
		}

		private PathOp path() {

			final Scope declaredIn = this.dep.getDeclaredIn().getScope();
			final PathOp enclosing =
					declaredIn.getEnclosingScopePath()
					.bind(this.dep.ref(), declaredIn)
					.op(start());

			return this.dep.ref().op(enclosing).path();
		}

		@Override
		protected TargetStoreOp allocateStore(ID id, Code code) {
			return path().target().allocateStore(id, code);
		}

	}


}

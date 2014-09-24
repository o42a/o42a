/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;
import static org.o42a.core.ir.op.TargetStoreOp.indirectTargetStore;
import static org.o42a.core.ref.RefUsage.CONTAINER_REF_USAGE;
import static org.o42a.core.ref.path.PathResolver.fullPathResolver;
import static org.o42a.core.ref.path.PathResolver.pathResolver;
import static org.o42a.core.ref.path.PathWalker.DUMMY_PATH_WALKER;
import static org.o42a.util.fn.CondInit.condInit;
import static org.o42a.util.fn.Init.init;

import java.util.function.Function;

import org.o42a.analysis.Analyzer;
import org.o42a.analysis.use.UseFlag;
import org.o42a.codegen.code.Code;
import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.ir.field.dep.DepOp;
import org.o42a.core.ir.field.local.LocalIROp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.op.*;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.path.impl.ObjectStepUses;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.LocalRegistry;
import org.o42a.util.fn.CondInit;
import org.o42a.util.fn.Init;
import org.o42a.util.string.ID;
import org.o42a.util.string.Name;
import org.o42a.util.string.SubID;


public final class Dep extends Step implements SubID {

	private final Obj declaredIn;
	private final Ref ref;
	private final Name name;
	private final ID id;
	private final Obj target;
	private final Init<ObjectStepUses> uses =
			init(() -> new ObjectStepUses(this));
	private byte enabled;
	private byte compileTimeOnly;
	private SyntheticDep synthetic;
	private CondInit<Analyzer, UseFlag> isSynthetic = condInit(
			(a, f) -> a.toUseCase() == f.getUseCase(),
			this::detectSynthetic);

	Dep(Obj declaredIn, Ref ref, Name name, ID id) {
		this.declaredIn = declaredIn;
		this.ref = ref;
		this.name = name;
		this.id = id;
		this.target = target();
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

		final PathResolver resolver = pathResolver(scope, dummyUser());

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
	protected void localMember(LocalRegistry registry) {
		registry.declareMemberLocal();
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

			final RefUsage usage;

			if (resolver.isLastStep()) {
				// Resolve only the last value.
				usage = resolver.refUsage();
			} else {
				usage = CONTAINER_REF_USAGE;
			}

			ref().resolveAll(
					enclosingResolver.fullResolver(resolver, usage));

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
				target -> object.meta().findIn(target).getScope())) {
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
	protected final HostOp op(HostOp host) {
		if (isSynthetic(host.getGenerator().getAnalyzer())) {
			return new SyntheticDepOp(host, this);
		}
		assert exists(host.getGenerator().getAnalyzer()) :
			this + " does not exist";
		return new DepStepOp(host, this);
	}

	@Override
	protected RefTargetIR targetIR(RefIR refIR) {
		throw new UnsupportedOperationException();
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
		return this.uses.get();
	}

	private boolean isSynthetic(Analyzer analyzer) {
		return this.isSynthetic.get(analyzer).isUsed();
	}

	private UseFlag detectSynthetic(Analyzer analyzer) {
		if (this.synthetic != null
				&& this.synthetic.isSynthetic(analyzer, this)) {
			return analyzer.toUseCase().usedFlag();
		}
		return analyzer.toUseCase().unusedFlag();
	}

	private Scope up(StepResolver resolver) {

		final PathResolver pathResolver;

		if (resolver.isFullResolution()) {
			pathResolver = fullPathResolver(
					resolver.getStart(),
					resolver,
					CONTAINER_REF_USAGE);
		} else {
			pathResolver = pathResolver(resolver.getStart(), resolver);
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
		}
	}

	private void enableDep() {
		this.enabled = 1;
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
				getDeclaredIn().getScope().getEnclosingScope();
		final Obj target = ref().resolve(enclosingScope.resolver()).toObject();

		return target.getInterface();
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

	private static final class DepStepOp extends StepOp<Dep> {

		DepStepOp(HostOp host, Dep step) {
			super(host, step);
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

			final Code alloc = code.inset(id);

			return indirectTargetStore(
					id,
					dirs -> dep(dirs, tempObjHolder(alloc.getAllocator()))
					.allocateStore(id, alloc));
		}

		@Override
		protected TargetStoreOp localStore(
				ID id,
				Function<CodeDirs, LocalIROp> getLocal) {
			return indirectTargetStore(
					id,
					dirs -> dep(dirs, tempObjHolder(dirs.getAllocator()))
					.localStore(id, getLocal));
		}

		private DepOp dep(CodeDirs dirs, ObjHolder holder) {
			return host()
					.target()
					.materialize(dirs, holder)
					.dep(dirs, getStep());
		}

	}

	private static final class SyntheticDepOp extends StepOp<Dep> {

		private final Dep dep;

		SyntheticDepOp(HostOp host, Dep dep) {
			super(host, dep);
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

		private HostOp path() {

			final Scope declaredIn = this.dep.getDeclaredIn().getScope();
			final HostOp enclosing =
					declaredIn.getEnclosingScopePath()
					.bind(this.dep.ref(), declaredIn)
					.op(host());

			return this.dep.ref().op(enclosing).path();
		}

		@Override
		protected TargetStoreOp allocateStore(ID id, Code code) {
			return path().target().allocateStore(id, code);
		}

		@Override
		protected TargetStoreOp localStore(
				ID id,
				Function<CodeDirs, LocalIROp> getLocal) {
			return path().target().localStore(id, getLocal);
		}

	}


}

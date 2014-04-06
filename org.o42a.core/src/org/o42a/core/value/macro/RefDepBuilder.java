/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.value.macro;

import static org.o42a.core.ref.RefUser.dummyRefUser;
import static org.o42a.core.ref.path.PathResolver.pathResolver;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.member.Member;
import org.o42a.core.object.Meta;
import org.o42a.core.object.Obj;
import org.o42a.core.object.meta.MetaDep;
import org.o42a.core.object.meta.ParentMetaDep;
import org.o42a.core.object.state.Dep;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.ReversePath;
import org.o42a.core.ref.path.*;
import org.o42a.core.st.sentence.Local;
import org.o42a.core.value.link.Link;
import org.o42a.util.ArrayUtil;


final class RefDepBuilder<D extends MetaDep> implements PathWalker {

	private final RefDep<D> dep;
	private final Ref ref;
	private final PathTemplate template;
	private Meta[] parentMeta;
	private Path depPath;
	private boolean nested;

	RefDepBuilder(RefDep<D> dep, Ref ref, PathTemplate template) {
		this.dep = dep;
		this.ref = ref;
		this.template = template;
	}

	@Override
	public boolean root(BoundPath path, Scope root) {
		return false;
	}

	@Override
	public boolean start(BoundPath path, Scope start) {
		if (path.isStatic()) {
			return false;
		}
		if (this.nested) {
			return true;
		}
		return appendParentMeta(start);
	}

	@Override
	public boolean module(Step step, Obj module) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean staticScope(Step step, Scope scope) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean up(
			Container enclosed,
			Step step,
			Container enclosing,
			ReversePath reversePath) {
		if (this.depPath != null) {
			return invalidRef();
		}
		return appendParentMeta(enclosing.getScope());
	}

	@Override
	public boolean member(Container container, Step step, Member member) {
		if (container.toObject() == null) {
			return invalidRef();
		}
		if (member.toField() == null) {
			return invalidRef();
		}
		return appendDepStep(step);
	}

	@Override
	public boolean dereference(Obj linkObject, Step step, Link link) {
		return invalidRef();
	}

	@Override
	public boolean local(Step step, Scope scope, Local local) {
		return invalidRef();
	}

	@Override
	public boolean dep(Obj object, Dep dep) {

		final Scope start = object.getScope();
		final boolean oldNested = this.nested;

		this.nested = true;

		final boolean result;
		final Scope enclosing = dep.walkToEnclosingScope(start, this);

		if (enclosing == null) {
			result = false;
		} else {
			result = dep.ref()
					.resolve(enclosing.walkingResolver(this))
					.isResolved();
		}

		this.nested = oldNested;

		return result;
	}

	@Override
	public boolean object(Step step, Obj object) {
		if (this.depPath == null) {
			// The dependency path is not started yet.
			final Scope enclosingScope = object.getScope().getEnclosingScope();

			if (enclosingScope.toObject() == null) {
				// Enclosing scope is a (local) member of some object.
				// The dependency affects the member owner.
				final Member topMember = enclosingScope.toMember();

				appendParentMeta(topMember.getScope());

				// The dependency path starts with this member access.
				this.depPath = topMember.getMemberKey().toPath();
			}
		}

		return appendDepStep(step);
	}

	@Override
	public boolean pathTrimmed(BoundPath path, Scope root) {
		return root(path, root);
	}

	@Override
	public void abortedAt(Scope last, Step brokenStep) {
	}

	@Override
	public boolean done(Container result) {
		return true;
	}

	public final D buildDep() {

		final BoundPath path = this.ref.getPath();
		final Scope start = this.ref.getScope();
		final PathResolution resolution =
				path.walk(pathResolver(start, dummyRefUser()), this);

		if (!resolution.isResolved()) {
			return null;
		}
		if (resolution.getObject() == null) {
			invalidRef();
			return null;
		}
		if (this.parentMeta == null) {
			invalidRef();
			return null;
		}

		final D dep = newDep();

		addParentDeps(dep);

		return dep;
	}

	private final boolean invalidRef() {
		this.dep.invalidRef(this.ref);
		return false;
	}

	private D newDep() {

		final Ref ref;
		final Meta meta = this.parentMeta[0];
		final Scope scope = this.ref.getScope();

		if (scope.toObject() != null) {
			ref = this.ref;
		} else {

			final Scope metaScope = meta.getObject().getScope();
			final PrefixPath prefix =
					scope.toMember()
					.getMemberKey()
					.toPath()
					.toPrefix(metaScope);

			ref = this.ref.prefixWith(prefix);
		}

		return this.dep.newDep(meta, ref, this.template);
	}

	private boolean appendParentMeta(Scope scope) {

		final Obj object = scope.toObject();

		if (object == null) {
			return true;
		}

		if (this.parentMeta == null) {
			this.parentMeta = new Meta[] {object.meta()};
		} else {
			this.parentMeta =
					ArrayUtil.append(this.parentMeta, object.meta());
		}

		return true;
	}

	private boolean appendDepStep(Step step) {
		if (this.depPath == null) {
			this.depPath = step.toPath();
		} else {
			this.depPath = this.depPath.append(step);
		}
		return true;
	}

	private void addParentDeps(D dep) {
		if (this.parentMeta.length > 1) {
			addTopDep(dep, addIntermediateDeps(dep));
		}
	}

	private IntermediateMetaDep addIntermediateDeps(D dep) {

		final int lastMetaIdx = this.parentMeta.length - 1;
		IntermediateMetaDep nested = null;

		for (int i = 1; i < lastMetaIdx; ++i) {

			final IntermediateMetaDep parent;

			if (nested == null) {
				parent = new IntermediateMetaDep(dep);
				this.dep.setParentDep(dep, parent);
			} else {
				parent = new IntermediateMetaDep(nested);
				nested.setParentDep(parent);
			}

			nested = parent;
		}

		return nested;
	}

	private void addTopDep(D dep, IntermediateMetaDep nested) {
		if (nested == null) {

			final TopMetaDep top = new TopMetaDep(dep, depPath());

			this.dep.setParentDep(dep, top);
		} else {

			final TopMetaDep top = new TopMetaDep(nested, depPath());

			nested.setParentDep(top);
		}
	}

	private BoundPath depPath() {

		final Meta topMeta = this.parentMeta[this.parentMeta.length - 1];
		final Path depPath;

		if (this.depPath == null) {
			depPath = Path.SELF_PATH;
		} else {
			depPath = this.depPath;
		}

		return depPath.bind(this.ref, topMeta.getObject().getScope());
	}

	private static final class IntermediateMetaDep extends ParentMetaDep {

		private MetaDep parentDep;

		IntermediateMetaDep(MetaDep nested) {
			super(nested);
		}

		@Override
		public MetaDep parentDep() {
			return this.parentDep;
		}

		final void setParentDep(MetaDep parentDep) {
			this.parentDep = parentDep;
		}

	}

	private static final class TopMetaDep extends ParentMetaDep {

		private final BoundPath depPath;

		TopMetaDep(MetaDep nested, BoundPath depPath) {
			super(nested);
			this.depPath = depPath;
		}

		@Override
		public MetaDep parentDep() {
			return null;
		}

		@Override
		protected boolean triggered(Meta meta) {

			final PathResolution resolution = this.depPath.resolve(
					pathResolver(meta.getObject().getScope(), dummyRefUser()));

			if (!resolution.isResolved()) {
				return false;
			}

			return resolution.getObject().meta().isUpdated();
		}

	}

}

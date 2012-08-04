/*
    Compiler
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.compiler.ip.macro;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.ref.path.PathResolver.pathResolver;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.member.Member;
import org.o42a.core.object.Meta;
import org.o42a.core.object.Obj;
import org.o42a.core.object.array.ArrayElement;
import org.o42a.core.object.link.Link;
import org.o42a.core.object.meta.MetaDep;
import org.o42a.core.object.meta.ParentMetaDep;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.ReversePath;
import org.o42a.core.ref.path.*;
import org.o42a.util.ArrayUtil;
import org.o42a.util.log.LogRecord;


final class MacroDepBuilder<D extends MetaDep> implements PathWalker {

	private final MacroDep<D> builder;
	private final Ref macroRef;
	private final PathTemplate template;
	private Meta[] parentMeta;
	private Path depPath;

	MacroDepBuilder(MacroDep<D> builder, Ref macroRef, PathTemplate template) {
		this.builder = builder;
		this.macroRef = macroRef;
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
			return invalidMacroRef();
		}
		return appendParentMeta(enclosed.getScope());
	}

	@Override
	public boolean member(Container container, Step step, Member member) {
		if (container.toObject() == null) {
			return invalidMacroRef();
		}
		return appendDepStep(step);
	}

	@Override
	public boolean dereference(Obj linkObject, Step step, Link link) {
		return invalidMacroRef();
	}

	@Override
	public boolean arrayIndex(
			Scope start,
			Step step,
			Ref array,
			Ref index,
			ArrayElement element) {
		return invalidMacroRef();
	}

	@Override
	public boolean dep(Obj object, Step step, Ref dependency) {
		return invalidMacroRef();
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

				appendParentMeta(
						topMember
						.getMemberOwner()
						.getOwner()
						.getScope());

				// The dependency path starts with this member access.
				this.depPath = topMember.getMemberKey().toPath();
			}
		}

		return appendDepStep(step);
	}

	@Override
	public void error(LogRecord message) {
	}

	@Override
	public void pathTrimmed(BoundPath path, Scope root) {
	}

	@Override
	public void abortedAt(Scope last, Step brokenStep) {
	}

	@Override
	public boolean done(Container result) {
		return true;
	}

	public final D buildDep() {

		final BoundPath path = this.macroRef.getPath();
		final Scope start = this.macroRef.getScope();
		final PathResolution resolution =
				path.walk(pathResolver(start, dummyUser()), this);

		if (!resolution.isResolved()) {
			return null;
		}
		if (resolution.getObject() == null) {
			invalidMacroRef();
			return null;
		}
		if (this.parentMeta == null) {
			invalidMacroRef();
			return null;
		}

		final D dep = newDep();

		addParentDeps(dep);

		return dep;
	}

	private final boolean invalidMacroRef() {
		this.macroRef.getLogger().error(
				"invalid_macro_ref",
				this.macroRef,
				"Invalid macro reference");
		return false;
	}

	private D newDep() {

		final Ref macroRef;
		final Meta meta = this.parentMeta[0];
		final Scope scope = this.macroRef.getScope();

		if (scope.toObject() != null) {
			macroRef = this.macroRef;
		} else {

			final Scope metaScope = meta.getObject().getScope();
			final PrefixPath prefix =
					scope.toMember()
					.getMemberKey()
					.toPath()
					.toPrefix(metaScope);

			macroRef = this.macroRef.prefixWith(prefix);
		}

		return this.builder.newDep(meta, macroRef, this.template);
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

	private IntermediateMacroDep addIntermediateDeps(D dep) {

		final int lastMetaIdx = this.parentMeta.length - 1;
		IntermediateMacroDep nested = null;

		for (int i = 1; i < lastMetaIdx; ++i) {

			final IntermediateMacroDep parent;

			if (nested == null) {
				parent = new IntermediateMacroDep(dep);
				this.builder.setParentDep(dep, parent);
			} else {
				parent = new IntermediateMacroDep(nested);
				nested.setParentDep(parent);
			}

			nested = parent;
		}

		return nested;
	}

	private void addTopDep(D dep, IntermediateMacroDep nested) {
		if (nested == null) {

			final TopMacroDep top = new TopMacroDep(dep, depPath());

			this.builder.setParentDep(dep, top);
		} else {

			final TopMacroDep top = new TopMacroDep(nested, depPath());

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

		return depPath.bind(this.macroRef, topMeta.getObject().getScope());
	}

	private static final class IntermediateMacroDep extends ParentMetaDep {

		private MetaDep parentDep;

		IntermediateMacroDep(MetaDep nested) {
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

	private static final class TopMacroDep extends ParentMetaDep {

		private final BoundPath depPath;

		TopMacroDep(MetaDep nested, BoundPath depPath) {
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
					pathResolver(meta.getObject().getScope(), dummyUser()));

			if (!resolution.isResolved()) {
				return false;
			}

			return resolution.getObject().meta().isUpdated();
		}

	}

}
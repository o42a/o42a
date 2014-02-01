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
package org.o42a.core.object.meta;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.ref.RefUsage.TEMP_REF_USAGE;
import static org.o42a.core.ref.RefUser.dummyRefUser;
import static org.o42a.core.ref.path.PathResolver.fullPathResolver;
import static org.o42a.core.ref.path.PathResolver.pathResolver;

import org.o42a.core.Scope;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.Meta;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.PathResolution;


public abstract class MetaDep {

	private final Meta declaredIn;
	private BoundPath parentPath;
	private Nesting nesting;
	private MetaDep next;

	public MetaDep(Meta declaredIn) {
		assert declaredIn != null :
			"Meta the dependency declared in is not specified";
		this.declaredIn = declaredIn;
	}

	public final Meta getDeclaredIn() {
		return this.declaredIn;
	}

	public final boolean updated(Meta meta) {

		final UpdatedMeta top = topMeta(meta);

		if (top == null) {
			return false;
		}

		return top.checkUpdated();
	}

	public abstract MetaDep parentDep();

	public abstract MetaDep nestedDep();

	public final Meta parentMeta(Meta meta) {
		meta.getObject().assertDerivedFrom(getDeclaredIn().getObject());

		final BoundPath parentPath = parentPath();

		if (parentPath == null) {
			return null;
		}

		final PathResolution parentResolution = parentPath.resolve(
				pathResolver(meta.getObject().getScope(), dummyRefUser()));
		final Meta parentMeta = parentResolution.getObject().meta();

		if (!meta.getParentMeta().is(parentMeta)) {
			// Out of scope.
			return null;
		}

		return parentMeta;
	}

	public final Meta nestedMeta(Meta meta) {
		meta.getObject().assertDerivedFrom(getDeclaredIn().getObject());

		final Nesting nesting = nesting();

		if (nesting == null) {
			return null;
		}

		return nesting.findObjectIn(meta.getObject().getScope()).meta();
	}

	public final void register() {

		final ObjectMeta declaredIn = getDeclaredIn();

		declaredIn.addDep(this);
	}

	final MetaDep getNext() {
		return this.next;
	}

	final void setNext(MetaDep next) {
		this.next = next;
	}

	protected abstract boolean triggered(Meta meta);

	protected abstract boolean changed(Meta meta);

	private final BoundPath parentPath() {
		if (this.parentPath != null) {
			return this.parentPath;
		}

		final MetaDep parentDep = parentDep();

		if (parentDep == null) {
			return null;
		}

		final Scope scope = getDeclaredIn().getObject().getScope();
		final Scope enclosingScope = scope.getEnclosingScope();
		final Obj enclosingObject = enclosingScope.toObject();

		if (enclosingObject != null) {
			return setParentPath(
					scope.getEnclosingScopePath().bind(scope, scope));
		}

		assert enclosingScope.toMember() != null :
			"Wrong enclosing scope: " + enclosingScope;

		return setParentPath(
				scope.getEnclosingScopePath()
				.append(enclosingScope.getEnclosingScopePath())
				.bind(scope, scope));
	}

	private BoundPath setParentPath(BoundPath path) {

		final Scope scope = getDeclaredIn().getObject().getScope();

		path.resolve(fullPathResolver(scope, dummyRefUser(), TEMP_REF_USAGE));

		return this.parentPath = path;
	}

	private final Nesting nesting() {
		if (this.nesting != null) {
			return this.nesting;
		}

		final MetaDep nestedDep = nestedDep();

		if (nestedDep == null) {
			return null;
		}

		final Meta nestedMeta = nestedDep.getDeclaredIn();
		final Scope enclosingScope =
				nestedMeta.getObject()
				.getScope()
				.getEnclosingScope();
		final Obj enclosingObject = enclosingScope.toObject();

		if (enclosingObject != null) {
			assert enclosingObject.meta().is(getDeclaredIn()) :
				"Wrong enclosing object: " + enclosingObject
				+ ", but expected " + getDeclaredIn().getObject();
			return this.nesting = nestedMeta.getNesting();
		}

		return this.nesting = new MemberObjectNesting(
				enclosingScope.toMember().getMemberKey(),
				nestedMeta.getNesting());
	}

	private UpdatedMeta topMeta(Meta meta) {

		Meta currentMeta = meta;
		MetaDep currentDep = this;

		for (;;) {

			final MetaDep parentDep = currentDep.parentDep();

			if (parentDep == null) {
				return new UpdatedMeta(currentMeta, currentDep);
			}

			final Meta parentMeta = currentDep.parentMeta(currentMeta);

			if (parentMeta == null) {
				// Out of the scope. Trigger can no longer trip.
				return null;
			}

			currentMeta = parentMeta;
			currentDep = parentDep;
		}
	}

	private static final class UpdatedMeta {

		private final Meta meta;
		private final MetaDep dep;

		UpdatedMeta(Meta meta, MetaDep dep) {
			this.meta = meta;
			this.dep = dep;
		}

		public final boolean checkUpdated() {

			final ObjectMeta meta = this.meta;

			return meta.checkUpdated(this.dep);
		}

	}

	private static final class MemberObjectNesting implements Nesting {

		private final MemberKey memberKey;
		private final Nesting nesting;

		MemberObjectNesting(MemberKey memberKey, Nesting nesting) {
			this.memberKey = memberKey;
			this.nesting = nesting;
		}

		@Override
		public Obj findObjectIn(Scope enclosing) {
			return this.nesting.findObjectIn(
					enclosing.getContainer()
					.member(this.memberKey)
					.substance(dummyUser())
					.getScope());
		}

		@Override
		public String toString() {
			if (this.nesting == null) {
				return super.toString();
			}
			return "Nesting[" + this.memberKey + '/' + this.nesting + ']';
		}

	}

}

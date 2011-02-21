/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core;

import java.util.ArrayList;

import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathFragment;


public class Namespace extends AbstractContainer {

	private final Container enclosing;
	private final ArrayList<NsUse> uses = new ArrayList<NsUse>();

	public Namespace(Container enclosing) {
		super(enclosing);
		this.enclosing = enclosing;
	}

	@Override
	public final Container getEnclosingContainer() {
		return this.enclosing;
	}

	public void useNamespace(Ref path) {
		this.uses.add(new NsUse(path));
	}

	public void useObject(Ref path, String alias) {
		this.uses.add(new ObjUse(path, alias));
	}

	@Override
	public final Scope getScope() {
		return this.enclosing.getScope();
	}

	@Override
	public Member toMember() {
		return this.enclosing.toMember();
	}

	@Override
	public final Artifact<?> toArtifact() {
		return this.enclosing.toArtifact();
	}

	@Override
	public final Obj toObject() {
		return this.enclosing.toObject();
	}

	@Override
	public final Clause toClause() {
		return this.enclosing.toClause();
	}

	@Override
	public final LocalScope toLocal() {
		return this.enclosing.toLocal();
	}

	@Override
	public final Namespace toNamespace() {
		return this;
	}

	@Override
	public final Member member(MemberKey memberKey) {
		return this.enclosing.member(memberKey);
	}

	@Override
	public Path member(ScopeInfo user, MemberId memberId, Obj declaredIn) {
		return this.enclosing.member(user, memberId, declaredIn);
	}

	@Override
	public Path findMember(ScopeInfo user, MemberId memberId, Obj declaredIn) {
		if (accessibleFrom(user)) {

			final Path found = findInNs(user, memberId, declaredIn);

			if (found != null) {
				return found;
			}
		}

		return this.enclosing.findMember(user, memberId, declaredIn);
	}

	@Override
	public String toString() {
		return "Namespace[" + this.enclosing + ']';
	}

	protected boolean accessibleFrom(ScopeInfo user) {
		return user.getContext() == getContext();
	}

	private Container container(Ref ref) {

		final Resolution resolution = ref.getResolution();
		final Obj container = resolution.toObject();

		if (container == null) {
			getContext().getLogger().notObject(ref, resolution);
			return getContext().getFalse();
		}
		if (!container.accessBy(ref).checkInstanceUse()) {
			return getContext().getFalse();
		}

		return container;
	}

	private Path findInNs(ScopeInfo user, MemberId memberId, Obj declaredIn) {

		final Obj object = toObject();

		if (object != null) {
			object.resolveMembers(memberId.containsAdapterId());
		}

		Path result = null;
		int resultPriority = 0;

		for (NsUse use : this.uses) {

			final Path found = use.findField(user, memberId, declaredIn);

			if (found == null) {
				continue;
			}

			final int priority = use.getPriority();

			if (result != null) {
				if (resultPriority == priority) {
					getContext().getLogger().ambiguousMember(
							user,
							memberId.toString());
					return null;
				}
				if (resultPriority > priority) {
					continue;
				}
			}

			result = found;
			resultPriority = priority;
		}

		return result;
	}

	private class NsUse {

		protected final Ref ref;
		private Container container;

		NsUse(Ref ref) {
			this.ref = ref;
		}

		public int getPriority() {
			return 1;
		}

		public Path getPath() {

			final Path path = this.ref.getPath();

			if (path != null || this.container != null) {
				return path;
			}

			this.ref.getLogger().notPath(this.ref);

			this.container = this.ref.getContext().getFalse();

			return null;
		}

		public Container getContainer() {
			getPath();
			if (this.container != null) {
				return this.container;
			}
			return this.container = container(this.ref);
		}

		public Path findField(
				ScopeInfo user,
				MemberId memberId,
				Obj declaredIn) {

			final Path found =
				getContainer().findMember(user, memberId, declaredIn);

			return found != null ? getPath().append(found) : null;
		}

		@Override
		public String toString() {
			return "Use Namespace: " + this.ref;
		}

	}

	private final class ObjUse extends NsUse {

		private String alias;

		ObjUse(Ref ref, String alias) {
			super(ref);
			this.alias = alias;
		}

		@Override
		public int getPriority() {
			return 2;
		}

		public String getAlias() {
			if (this.alias != null) {
				return this.alias;
			}

			final Path path = getPath();

			if (path == null) {
				return this.alias = "";
			}

			final PathFragment[] fragments = path.getFragments();
			final PathFragment lastFragment = fragments[fragments.length - 1];

			this.alias = lastFragment.getName();
			if (this.alias == null) {
				getContext().getLogger().noName(this.ref);
				return this.alias = "";
			}

			return this.alias;
		}

		@Override
		public Path findField(
				ScopeInfo user,
				MemberId memberId,
				Obj declaredIn) {
			if (declaredIn != null) {
				return null;
			}
			if (!getAlias().equals(memberId.getName())) {
				return null;
			}
			return this.ref.getPath();
		}

		@Override
		public String toString() {
			return "Use Object: " + this.ref + " as " + this.alias;
		}

	}

}

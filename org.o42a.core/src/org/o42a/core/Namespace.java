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

import static org.o42a.core.ref.path.PathResolver.pathResolver;
import static org.o42a.util.use.User.dummyUser;

import java.util.ArrayList;

import org.o42a.core.artifact.Accessor;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.Role;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.LocationInfo;


public class Namespace extends AbstractContainer {

	private final Container enclosing;
	private final ArrayList<NsUse> uses = new ArrayList<NsUse>();

	public Namespace(LocationInfo location, Container enclosing) {
		super(location);
		this.enclosing = enclosing;
	}

	@Override
	public final Container getEnclosingContainer() {
		return this.enclosing;
	}

	public void useNamespace(Ref path) {
		this.uses.add(new NsUse(path));
	}

	public void useNamespace(BoundPath path) {
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
	public Path member(
			PlaceInfo user,
			Accessor accessor,
			MemberId memberId,
			Obj declaredIn) {
		return this.enclosing.member(user, accessor, memberId, declaredIn);
	}

	@Override
	public Path findMember(
			PlaceInfo user,
			Accessor accessor,
			MemberId memberId,
			Obj declaredIn) {
		if (accessibleBy(accessor)) {

			final Path found = findInNs(user, accessor, memberId, declaredIn);

			if (found != null) {
				return found;
			}
		}

		return this.enclosing.findMember(user, accessor, memberId, declaredIn);
	}

	@Override
	public String toString() {
		return "Namespace[" + this.enclosing + ']';
	}

	protected boolean accessibleBy(Accessor accessor) {
		return accessor == Accessor.DECLARATION || accessor == Accessor.OWNER;
	}

	private Container container(Ref ref) {

		final Resolution resolution = ref.getResolution();
		final Obj container = resolution.toObject();

		if (container == null) {
			getContext().getLogger().notObject(ref, resolution);
			return getContext().getFalse();
		}
		if (!Role.INSTANCE.checkUseBy(ref, ref)) {
			return getContext().getFalse();
		}

		return container;
	}

	private Path findInNs(
			PlaceInfo user,
			Accessor accessor,
			MemberId memberId,
			Obj declaredIn) {

		final Obj object = toObject();

		if (object != null) {
			object.resolveMembers(memberId.containsAdapterId());
		}

		Path result = null;
		int resultPriority = 0;

		for (NsUse use : this.uses) {

			final Path found =
					use.findField(user, accessor, memberId, declaredIn);

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
		private BoundPath path;
		private Container container;

		NsUse(Ref ref) {
			this.ref = ref;
		}

		NsUse(BoundPath path) {
			this.path = path;
			this.ref = null;
		}

		public int getPriority() {
			return 1;
		}

		public Path getPath() {
			if (this.path != null) {
				return this.path.getPath();
			}

			final BoundPath path = this.ref.getPath();

			if (path != null) {
				this.path = path;
				return this.path.getPath();
			}
			if (this.container != null) {
				return null;
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
			if (this.ref != null) {
				return this.container = container(this.ref);
			}
			return this.container =
					this.path.resolve(pathResolver(dummyUser()), getScope())
					.getResult();
		}

		public Path findField(
				PlaceInfo user,
				Accessor accessor,
				MemberId memberId,
				Obj declaredIn) {

			final Path found = getContainer().findMember(
					user,
					accessor,
					memberId,
					declaredIn);

			return found != null ? getPath().append(found) : null;
		}

		@Override
		public String toString() {
			return "Use Namespace: " + this.ref;
		}

	}

	private final class ObjUse extends NsUse {

		private final String alias;

		ObjUse(Ref ref, String alias) {
			super(ref);
			this.alias = alias;
		}

		@Override
		public int getPriority() {
			return 2;
		}

		public String getAlias() {
			return this.alias;
		}

		@Override
		public Path findField(
				PlaceInfo user,
				Accessor accessor,
				MemberId memberId,
				Obj declaredIn) {
			if (declaredIn != null) {
				return null;
			}
			if (!getAlias().equals(memberId.getName())) {
				return null;
			}
			return this.ref.getPath().getPath();
		}

		@Override
		public String toString() {
			if (this.alias == null) {
				return "Use Object: " + this.ref;
			}
			return "Use Object: " + this.ref + " as " + this.alias;
		}

	}

}

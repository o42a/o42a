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
package org.o42a.core;

import static org.o42a.core.ref.RefUser.dummyRefUser;
import static org.o42a.core.ref.path.PathResolver.pathResolver;

import java.util.ArrayList;

import org.o42a.core.member.*;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.object.Obj;
import org.o42a.core.object.Role;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.Local;
import org.o42a.util.string.Name;


public class Namespace extends AbstractContainer {

	private final Container enclosing;
	private final ArrayList<NsUse> uses = new ArrayList<>();

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

	public void useObject(Ref path, Name alias) {
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
	public final Obj toObject() {
		return this.enclosing.toObject();
	}

	@Override
	public final Clause toClause() {
		return this.enclosing.toClause();
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
	public MemberPath member(
			Access access,
			MemberId memberId,
			Obj declaredIn) {
		return getEnclosingContainer()
				.member(access, memberId, declaredIn);
	}

	@Override
	public MemberPath findMember(
			Access access,
			MemberId memberId,
			Obj declaredIn) {

		final MemberPath foundInEnclosing =
				getEnclosingContainer()
				.member(access, memberId, declaredIn);

		if (foundInEnclosing != null) {
			return foundInEnclosing;
		}
		if (accessibleBy(access.getAccessor())) {

			final BoundPath foundInNS = findInNs(access, memberId, declaredIn);

			if (foundInNS != null) {
				return new NsPath(foundInNS);
			}
		}

		return this.enclosing.findMember(access, memberId, declaredIn);
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
			getContext().getLogger().notObject(ref.getLocation(), resolution);
			return getContext().getNone();
		}
		if (!Role.INSTANCE.checkUseBy(ref, ref)) {
			return getContext().getNone();
		}

		return container;
	}

	private BoundPath findInNs(
			Access access,
			MemberId memberId,
			Obj declaredIn) {

		final Obj object = toObject();

		if (object != null) {
			object.resolveMembers(memberId.containsAdapterId());
		}

		BoundPath result = null;
		int resultPriority = 0;

		for (NsUse use : this.uses) {

			final BoundPath found = use.findField(access, memberId, declaredIn);

			if (found == null) {
				continue;
			}

			final int priority = use.getPriority();

			if (result != null) {
				if (resultPriority == priority) {
					getContext().getLogger().ambiguousMember(
							access.getLocation(),
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

	private static final class NsPath implements MemberPath {

		private final BoundPath path;

		NsPath(BoundPath path) {
			this.path = path;
		}

		@Override
		public Path pathToMember() {
			return this.path.getPath();
		}

		@Override
		public Member toMember() {
			return null;
		}

		@Override
		public Local toLocal() {
			return null;
		}

		@Override
		public String toString() {
			if (this.path == null) {
				return super.toString();
			}
			return this.path.toString();
		}

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

		public BoundPath getPath() {
			if (this.path != null) {
				return this.path;
			}

			final BoundPath path = this.ref.getPath();

			if (path != null) {
				return this.path = path;
			}
			if (this.container != null) {
				return null;
			}

			this.ref.getLogger().error("not_path", this.ref, "Not a path");

			this.container = this.ref.getContext().getNone();

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
					this.path.resolve(pathResolver(getScope(), dummyRefUser()))
					.getResult();
		}

		public BoundPath findField(
				Access access,
				MemberId memberId,
				Obj declaredIn) {

			final MemberPath found = getContainer().findMember(
					access,
					memberId,
					declaredIn);

			return found != null
					? getPath().append(found.pathToMember()) : null;
		}

		@Override
		public String toString() {
			return "Use Namespace: " + this.ref;
		}

	}

	private final class ObjUse extends NsUse {

		private final Name alias;

		ObjUse(Ref ref, Name alias) {
			super(ref);
			this.alias = alias;
		}

		@Override
		public int getPriority() {
			return 2;
		}

		public final Name getAlias() {
			return this.alias;
		}

		@Override
		public BoundPath findField(
				Access access,
				MemberId memberId,
				Obj declaredIn) {
			if (declaredIn != null) {
				return null;
			}

			final MemberName memberName = memberId.getMemberName();

			if (memberName == null || !getAlias().is(memberName.getName())) {
				return null;
			}

			return this.ref.getPath();
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

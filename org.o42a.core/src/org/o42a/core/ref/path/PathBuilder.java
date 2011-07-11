/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.core.ref.path;

import static org.o42a.core.member.AdapterId.adapterId;
import static org.o42a.core.member.MemberId.fieldName;
import static org.o42a.core.ref.path.Path.ROOT_PATH;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.*;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.*;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.ArrayUtil;
import org.o42a.util.use.UserInfo;


public final class PathBuilder {

	public static PathBuilder pathBuilder(String name, String... names) {

		final Fragment[] fragments = new Fragment[names.length + 1];

		fragments[0] = new MemberById(fieldName(name));
		for (int i = 0; i < names.length; ++i) {
			fragments[i + 1] = new MemberById(fieldName(names[i]));
		}

		return new PathBuilder(fragments);
	}

	private final Fragment[] fragments;
	private CompilerContext context;
	private AbsolutePath path;

	private PathBuilder(Fragment[] fragments) {
		this.fragments = fragments;
	}

	public final AbsolutePath path(CompilerContext context) {
		if (cacheCompatible(context)) {
			return this.path;
		}
		this.context = context;
		return this.path = buildPath(context);
	}

	public final Ref target(CompilerContext context) {
		return path(context).target(context);
	}

	public final Artifact<?> artifact(CompilerContext context) {
		return path(context).target(context).getResolution().toArtifact();
	}

	public final Obj materialize(CompilerContext context) {
		return artifact(context).materialize();
	}

	public final Ref target(LocationInfo location, Distributor distributor) {
		return path(location.getContext()).target(location, distributor);
	}

	public final AdapterId toAdapterId(
			LocationInfo location,
			Distributor distributor) {
		return adapterId(target(location, distributor).toStaticTypeRef());
	}

	public final MemberKey memberKey(CompilerContext context) {

		final AbsolutePath path = path(context);
		final Member member = path.resolve(context).toMember();

		assert member != null :
			"Member not found: " + this;

		return member.getKey();

	}

	public final MemberId memberId(CompilerContext context) {
		return memberKey(context).getMemberId();
	}

	public final Member memberOf(Container container) {

		final MemberKey key = memberKey(container.getContext());
		final Member found = container.member(key);

		assert found != null :
			key + "(" + this + ") not found in : " + container;

		return found;
	}

	public final Member memberOf(Scope scope) {
		return memberOf(scope.getContainer());
	}

	public final Field<?> fieldOf(UserInfo user, Container container) {

		final Member member = memberOf(container);
		final Field<?> field = member.toField(user);

		assert field != null :
			"Not a field: " + member;

		return field;
	}

	public final Field<?> fieldOf(UserInfo user, Scope scope) {
		return fieldOf(user, scope.getContainer());
	}

	public final boolean cacheCompatible(CompilerContext context) {
		if (this.context == null) {
			return false;
		}
		return context.compatible(this.context);
	}

	public final PathBuilder appendField(String name) {
		return new PathBuilder(ArrayUtil.append(
				this.fragments,
				new MemberById(fieldName(name))));
	}

	public final PathBuilder appendAdapter(PathBuilder adapterPath) {
		return new PathBuilder(ArrayUtil.append(
				this.fragments,
				new AdapterFragment(adapterPath)));
	}

	public final PathBuilder appendMember(PathBuilder memberPath) {
		return new PathBuilder(ArrayUtil.append(
				this.fragments,
				new MemberFragment(memberPath)));
	}

	@Override
	public String toString() {
		return toString(this.fragments.length);
	}

	protected AbsolutePath buildPath(CompilerContext context) {

		final int len = this.fragments.length;

		if (len == 0) {
			return ROOT_PATH;
		}

		AbsolutePath path = ROOT_PATH;
		Container container = context.getRoot();
		int i = 0;

		for (;;) {

			final Fragment fragment = this.fragments[i];
			final int next = i + 1;
			final Member member = fragment.member(container);

			assert member != null :
				"Member " + toString(next) + " not found";

			path = path.append(member.getKey());
			if (next >= len) {
				return path;
			}
			i = next;
			container = member.substance(dummyUser());
		}
	}

	private String toString(int len) {
		if (len == 0) {
			return "</>";
		}

		final StringBuilder out = new StringBuilder();

		out.append('<');
		for (int i = 0; i < len; ++i) {
			out.append('/').append(this.fragments[i]);
		}
		out.append('>');

		return out.toString();
	}

	private interface Fragment {

		Member member(Container container);

	}

	private static final class MemberById implements Fragment {

		private final MemberId memberId;

		MemberById(MemberId name) {
			this.memberId = name;
		}

		@Override
		public Member member(Container container) {
			return container.toObject().member(this.memberId);
		}

		@Override
		public String toString() {
			return this.memberId.toString();
		}

	}

	private static final class AdapterFragment implements Fragment {

		private final PathBuilder path;

		AdapterFragment(PathBuilder path) {
			this.path = path;
		}

		@Override
		public Member member(Container container) {
			return container.toObject().member(
					adapterId(this.path.target(
							container.getContext()).toStaticTypeRef()));
		}

		@Override
		public String toString() {
			return "@" + this.path;
		}

	}

	private static final class MemberFragment implements Fragment {

		private final PathBuilder path;

		MemberFragment(PathBuilder path) {
			this.path = path;
		}

		@Override
		public Member member(Container container) {
			return this.path.memberOf(container);
		}

		@Override
		public String toString() {
			return this.path.toString();
		}

	}

}

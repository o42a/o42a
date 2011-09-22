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
package org.o42a.core.ref.path;

import static org.o42a.core.ref.path.PathReproduction.reproducedPath;
import static org.o42a.core.ref.path.PathReproduction.unchangedPath;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.Field;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;


public class MemberFragment extends PathFragment {

	private final MemberKey memberKey;

	public MemberFragment(MemberKey memberKey) {
		if (memberKey == null) {
			throw new NullPointerException("Field key not specified");
		}
		this.memberKey = memberKey;
	}

	@Override
	public String getName() {
		return this.memberKey.getName();
	}

	public final MemberKey getMemberKey() {
		return this.memberKey;
	}

	@Override
	public PathFragment materialize() {

		final MemberKey memberKey = getMemberKey();
		final Member member =
				memberKey.getOrigin().getContainer().member(memberKey);
		final Field<?> field = member.toField(dummyUser());

		if (field == null) {
			return null;
		}
		if (field.getArtifactKind().isObject()) {
			return null;
		}

		return MATERIALIZE;
	}

	@Override
	public Container resolve(
			PathResolver resolver,
			Path path,
			int index,
			Scope start,
			PathWalker walker) {

		final Member member = resolveMember(resolver, path, index, start);

		if (member == null) {
			return null;
		}

		walker.member(start.getContainer(), this, member);

		return member.substance(resolver);
	}

	@Override
	public final PathReproduction reproduce(
			LocationInfo location,
			Reproducer reproducer,
			Scope scope) {

		final Scope origin = this.memberKey.getOrigin();

		return reproduce(location, reproducer, origin, scope);
	}

	@Override
	public HostOp write(CodeDirs dirs, HostOp start) {

		final Member firstDeclaration = firstDeclaration();

		if (firstDeclaration.toLocal(dummyUser()) != null) {
			// Member is a local scope.
			return start;
		}

		assert firstDeclaration.toField(dummyUser()) != null :
			"Field expected: " + firstDeclaration;

		// Member is field.
		return start.field(dirs, this.memberKey);
	}

	@Override
	public int hashCode() {
		return this.memberKey.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		final MemberFragment other = (MemberFragment) obj;

		return this.memberKey.equals(other.memberKey);
	}

	@Override
	public String toString() {
		return this.memberKey.toString();
	}

	@Override
	protected PathFragment rebuild(PathFragment prev) {
		return prev.combineWithMember(this.memberKey);
	}

	protected Member resolveMember(
			PathResolver resolver,
			Path path,
			int index,
			Scope start) {

		final Member member = start.getContainer().member(this.memberKey);

		if (member == null) {
			unresolved(resolver, path, index, start);
			return null;
		}

		return member;
	}

	protected PathReproduction reproduce(
			LocationInfo location,
			Reproducer reproducer,
			Scope origin,
			Scope scope) {

		final Member member = origin.getContainer().member(this.memberKey);

		if (origin.getContainer().toClause() == null
				&& member.toClause() == null) {
			// Neither clause, nor member of clause.
			// Return unchanged.
			return unchangedPath(toPath());
		}

		final MemberKey reproductionKey =
				this.memberKey.getMemberId().reproduceFrom(origin).key(scope);

		return reproducedPath(reproductionKey.toPath());
	}

	private final Member firstDeclaration() {

		final Scope origin = this.memberKey.getOrigin();

		return origin.getContainer().member(this.memberKey);
	}

	private Container unresolved(
			LocationInfo location,
			Path path,
			int index,
			Scope start) {
		start.getContext().getLogger().unresolved(
				location,
				path.toString(index + 1));
		return null;
	}

}

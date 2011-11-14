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

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.PathOp;
import org.o42a.core.ir.op.StepOp;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.source.LocationInfo;


public class MemberStep extends Step {

	private final MemberKey memberKey;

	public MemberStep(MemberKey memberKey) {
		if (memberKey == null) {
			throw new NullPointerException("Field key not specified");
		}
		this.memberKey = memberKey;
	}

	@Override
	public PathKind getPathKind() {
		return PathKind.RELATIVE_PATH;
	}

	@Override
	public String getName() {
		return this.memberKey.getName();
	}

	public final MemberKey getMemberKey() {
		return this.memberKey;
	}

	@Override
	public boolean isMaterial() {

		final Member member = firstDeclaration();
		final MemberField field = member.toMemberField();

		if (field == null) {
			return true;
		}

		return field.getArtifactKind().isObject();
	}

	@Override
	public Container resolve(
			PathResolver resolver,
			BoundPath path,
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
			PathReproducer reproducer) {

		final Scope origin = this.memberKey.getOrigin();

		return reproduce(location, reproducer, origin, reproducer.getScope());
	}

	@Override
	public PathOp op(PathOp start) {
		return new Op(start, this);
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

		final MemberStep other = (MemberStep) obj;

		return this.memberKey.equals(other.memberKey);
	}

	@Override
	public String toString() {
		return this.memberKey.toString();
	}

	@Override
	protected FieldDefinition fieldDefinition(
			BoundPath path,
			Distributor distributor) {
		return defaultFieldDefinition(path, distributor);
	}

	protected Member resolveMember(
			PathResolver resolver,
			BoundPath path,
			int index,
			Scope start) {

		final Member member = start.getContainer().member(this.memberKey);

		if (member == null) {
			unresolved(path, index, start);
			return null;
		}

		return member;
	}

	protected PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer,
			Scope origin,
			Scope scope) {

		final Member member = origin.getContainer().member(this.memberKey);

		if (origin.getContainer().toClause() == null
				&& member.toMemberClause() == null) {
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
			BoundPath path,
			int index,
			Scope start) {
		start.getContext().getLogger().unresolved(
				path,
				path.toString(index + 1));
		return null;
	}

	private static final class Op extends StepOp<MemberStep> {

		Op(PathOp start, MemberStep step) {
			super(start, step);
		}

		@Override
		public HostOp target(CodeDirs dirs) {

			final Member firstDeclaration = getStep().firstDeclaration();

			if (firstDeclaration.toMemberLocal() != null) {
				// Member is a local scope.
				return host();
			}

			assert firstDeclaration.toMemberField() != null :
				"Field expected: " + firstDeclaration;

			// Member is field.
			return start().field(dirs, getStep().getMemberKey());
		}

	}

}

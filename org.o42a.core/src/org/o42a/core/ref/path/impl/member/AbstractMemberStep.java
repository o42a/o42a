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
package org.o42a.core.ref.path.impl.member;

import static org.o42a.core.ref.path.PathReproduction.reproducedPath;
import static org.o42a.core.ref.path.PathReproduction.unchangedPath;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.ir.op.*;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;


public abstract class AbstractMemberStep extends Step {

	private final MemberKey memberKey;

	public AbstractMemberStep(MemberKey memberKey) {
		if (memberKey == null) {
			throw new NullPointerException("Field key not specified");
		}
		this.memberKey = memberKey;
	}

	@Override
	public final PathKind getPathKind() {
		return PathKind.RELATIVE_PATH;
	}

	public final MemberKey getMemberKey() {
		return this.memberKey;
	}

	@Override
	public final RefUsage getObjectUsage() {
		return RefUsage.CONTAINER_REF_USAGE;
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

		final AbstractMemberStep other = (AbstractMemberStep) obj;

		return this.memberKey.equals(other.memberKey);
	}

	@Override
	public String toString() {
		if (this.memberKey == null) {
			return super.toString();
		}
		return this.memberKey.toString();
	}

	@Override
	protected FieldDefinition fieldDefinition(Ref ref) {
		return defaultFieldDefinition(ref);
	}

	@Override
	protected TypeRef ancestor(LocationInfo location, Ref ref) {
		return defaultAncestor(location, ref);
	}

	@Override
	protected TypeRef iface(Ref ref) {
		return ref.toTypeRef();
	}

	protected final Member resolveMember(
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

	@Override
	protected final PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer) {

		final Scope origin = this.memberKey.getOrigin();

		return reproduce(location, reproducer, origin, reproducer.getScope());
	}

	/**
	 * Reproduces a member step.
	 *
	 * @param location the reproduced step location.
	 * @param reproducer the reproducer.
	 * @param origin the member's {@link MemberKey#getOrigin() origin}.
	 * @param scope the scope the reproduced member belongs to.
	 *
	 * @return the reproduced step in the form of path reproduction.
	 */
	protected PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer,
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

	@Override
	protected final PathOp op(PathOp start) {
		return new MemberOp(start, this);
	}

	private final Member firstDeclaration() {

		final Scope origin = this.memberKey.getOrigin();

		return origin.getContainer().member(this.memberKey);
	}

	private Container unresolved(BoundPath path, int index, Scope start) {
		start.getContext().getLogger().unresolved(
				path.getLocation(),
				path.toString(index + 1));
		return null;
	}

	private static final class MemberOp extends StepOp<AbstractMemberStep> {

		MemberOp(PathOp start, AbstractMemberStep step) {
			super(start, step);
		}

		@Override
		public HostValueOp value() {
			return targetValueOp();
		}

		@Override
		public HostOp pathTarget(CodeDirs dirs) {

			final Member firstDeclaration = getStep().firstDeclaration();

			assert firstDeclaration.toField() != null :
				"Field expected: " + firstDeclaration;

			// Member is field.
			return start().field(dirs, getStep().getMemberKey());
		}

	}

}

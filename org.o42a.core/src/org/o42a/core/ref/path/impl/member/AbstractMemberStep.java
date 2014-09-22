/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import static org.o42a.core.ir.op.HostTargetOp.ALLOC_STORE_SUFFIX;

import org.o42a.codegen.code.Code;
import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.field.FldStoreOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.*;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.PathKind;
import org.o42a.core.ref.path.Step;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.string.ID;


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
	protected final HostOp op(HostOp host) {
		return new MemberOp(host, this);
	}

	private Container unresolved(BoundPath path, int index, Scope start) {
		start.getContext().getLogger().unresolved(
				path.getLocation(),
				path.toString(index + 1));
		return null;
	}

	private static final class MemberOp extends StepOp<AbstractMemberStep> {

		MemberOp(HostOp host, AbstractMemberStep step) {
			super(host, step);
		}

		@Override
		public HostValueOp value() {
			return pathValueOp();
		}

		@Override
		public HostTargetOp target() {
			return pathTargetOp();
		}

		@Override
		public FldOp<?, ?> pathTarget(CodeDirs dirs) {
			return host().target().field(dirs, getStep().getMemberKey());
		}

		@Override
		protected TargetStoreOp allocateStore(ID id, Code code) {

			final Code alloc = code.inset(id.detail(ALLOC_STORE_SUFFIX));

			return new MemberStoreOp(id, alloc, this);
		}

	}

	private static final class MemberStoreOp implements FldStoreOp {

		private final ID id;
		private final Code alloc;
		private final MemberOp member;
		private FldStoreOp store;

		MemberStoreOp(ID id, Code alloc, MemberOp member) {
			this.id = id;
			this.alloc = alloc;
			this.member = member;
		}

		@Override
		public void storeTarget(CodeDirs dirs) {

			final FldOp<?, ?> field = this.member.pathTarget(dirs);

			this.store = field.allocateStore(this.id, this.alloc);
			this.store.storeTarget(dirs);
		}

		@Override
		public ObjectOp loadOwner(CodeDirs dirs) {
			return this.store.loadOwner(dirs);
		}

		@Override
		public TargetOp loadTarget(CodeDirs dirs) {
			return this.store.loadTarget(dirs);
		}

		@Override
		public String toString() {
			if (this.id != null) {
				return super.toString();
			}
			return this.id.toString();
		}

	}

}

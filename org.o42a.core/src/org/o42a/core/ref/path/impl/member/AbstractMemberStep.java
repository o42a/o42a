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

import java.util.function.Function;

import org.o42a.codegen.code.Code;
import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.field.FldStoreOp;
import org.o42a.core.ir.field.local.LocalIROp;
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
import org.o42a.core.st.sentence.LocalRegistry;
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
	protected void localMember(LocalRegistry registry) {
		registry.declareMemberLocal();
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

		private MemberOp(MemberOp proto, OpPresets presets) {
			super(proto, presets);
		}

		@Override
		public final MemberOp setPresets(OpPresets presets) {
			if (presets.is(getPresets())) {
				return this;
			}
			return new MemberOp(this, presets);
		}

		@Override
		public HostValueOp value() {
			return pathValueOp();
		}

		@Override
		public FldOp<?, ?> pathTarget(CodeDirs dirs) {
			return host()
					.setPresets(getPresets())
					.field(dirs, getStep().getMemberKey());
		}

		@Override
		public TargetStoreOp allocateStore(ID id, Code code) {

			final Code alloc = code.inset(id);

			return new MemberStoreOp(
					id,
					dirs -> pathTarget(dirs).allocateStore(id, alloc));
		}

		@Override
		public TargetStoreOp localStore(
				ID id,
				Function<CodeDirs, LocalIROp> getLocal) {
			return new MemberStoreOp(
					id,
					dirs -> pathTarget(dirs).localStore(id, getLocal));
		}

	}

	private static final class MemberStoreOp
			extends IndirectTargetStoreOp<FldStoreOp>
			implements FldStoreOp {

		MemberStoreOp(ID id, Function<CodeDirs, FldStoreOp> getStore) {
			super(id, getStore);
		}

		@Override
		public ObjectOp loadOwner(CodeDirs dirs) {
			return store().loadOwner(dirs);
		}

	}

}

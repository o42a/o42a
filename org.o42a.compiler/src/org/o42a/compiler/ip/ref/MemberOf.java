/*
    Compiler
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
package org.o42a.compiler.ip.ref;

import org.o42a.common.ref.CompoundPathWalker;
import org.o42a.core.Scope;
import org.o42a.core.member.AccessSource;
import org.o42a.core.member.MemberContainer;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Role;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.RoleResolver;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathExpander;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;


public class MemberOf extends ContainedFragment {

	private final AccessSource accessSource;
	private final MemberId memberId;
	private final StaticTypeRef declaredIn;

	public MemberOf(
			LocationInfo location,
			AccessDistributor distributor,
			MemberId memberId,
			StaticTypeRef declaredIn) {
		super(location, distributor);
		this.accessSource = distributor.getAccessSource();
		this.memberId = memberId;
		this.declaredIn = declaredIn;
	}

	@Override
	public Path expand(PathExpander expander, int index, Scope owner) {

		final AccessorResolver accessorResolver = new AccessorResolver();
		final RoleResolver roleResolver = new RoleResolver(this, Role.INSTANCE);
		final CompoundPathWalker walker =
				new CompoundPathWalker(roleResolver, accessorResolver);

		if (!expander.replay(walker)) {
			return null;
		}

		final MemberContainer container = owner.getContainer();
		final Path memberPath = container.member(
				accessorResolver.getAccessor()
				.accessBy(this, this.accessSource),
				this.memberId,
				this.declaredIn != null
				? this.declaredIn.getType() : null);

		if (memberPath == null) {
			getLogger().error(
					"undefined_member",
					this,
					"Member '%s' is not defined in '%s'",
					this.memberId,
					container);
			return null;
		}

		return memberPath;
	}

	@Override
	public FieldDefinition fieldDefinition(Ref ref) {
		return defaultFieldDefinition(ref);
	}

	@Override
	public TypeRef iface(Ref ref) {
		return ref.toTypeRef();
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append(this.memberId);
		if (this.declaredIn != null) {
			out.append("@<").append(this.declaredIn).append('>');
		}

		return out.toString();
	}

}

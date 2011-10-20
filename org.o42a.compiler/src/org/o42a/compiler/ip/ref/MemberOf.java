/*
    Compiler
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
package org.o42a.compiler.ip.ref;

import static org.o42a.core.ref.path.Path.SELF_PATH;
import static org.o42a.core.ref.path.Path.materializePath;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.common.resolution.CompoundPathWalker;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.Role;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.MemberContainer;
import org.o42a.core.member.MemberId;
import org.o42a.core.ref.common.PlacedPathFragment;
import org.o42a.core.ref.common.RoleResolver;
import org.o42a.core.ref.path.ExpansionContext;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.source.LocationInfo;


public class MemberOf extends PlacedPathFragment {

	private final MemberId memberId;
	private final StaticTypeRef declaredIn;

	public MemberOf(
			LocationInfo location,
			Distributor distributor,
			MemberId memberId,
			StaticTypeRef declaredIn) {
		super(location, distributor);
		this.memberId = memberId;
		this.declaredIn = declaredIn;
	}

	@Override
	public Path expand(ExpansionContext context, int index, Scope owner) {

		final Scope scope = getScope();
		final AccessorResolver accessorResolver = new AccessorResolver();
		final RoleResolver roleResolver = new RoleResolver(Role.INSTANCE);
		final CompoundPathWalker walker =
				new CompoundPathWalker(roleResolver, accessorResolver);

		if (!context.replay(walker)) {
			if (!roleResolver.getRole().atLeast(Role.INSTANCE)) {
				Role.INSTANCE.reportMisuseBy(this, owner);
			}
			return null;
		}

		final Artifact<?> artifact = owner.getArtifact();
		final MemberContainer container;
		final Path prefix;

		if (artifact != null) {

			final Obj object = artifact.toObject();

			if (object == null) {
				container = artifact.materialize();
				prefix = materializePath();
			} else {
				container = object;
				prefix = SELF_PATH;
			}
		} else {
			container = scope.getContainer();
			prefix = SELF_PATH;
		}

		final Path memberPath = container.member(
				this,
				accessorResolver.getAccessor(),
				this.memberId, this.declaredIn != null
				? this.declaredIn.typeObject(dummyUser()) : null);

		if (memberPath == null) {
			return null;
		}

		return prefix.append(memberPath);
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

/*
    Compiler
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
package org.o42a.compiler.ip.ref;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.compiler.ip.ref.RefInterpreter.linkTargetIsAccessibleFrom;
import static org.o42a.core.member.AdapterId.adapterId;
import static org.o42a.core.ref.path.Path.SELF_PATH;

import org.o42a.common.ref.CompoundPathWalker;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.member.*;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.object.Role;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.RoleResolver;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathExpander;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.link.LinkValueType;


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
		final Access access =
				accessorResolver.getAccessor()
				.accessBy(this, this.accessSource);

		final Path found = memberOfContainerOrLinkTarget(container, access);

		if (found != null) {
			return found;
		}

		getLogger().error(
				"undefined_member",
				this,
				"Member '%s' is not defined in '%s'",
				this.memberId,
				container);

		return null;
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

	private Path memberOfContainerOrLinkTarget(
			MemberContainer container,
			Access access) {

		final Path memberOfAdapter = memberOfContainer(container, access);

		if (memberOfAdapter != null) {
			return memberOfAdapter;
		}

		return memberOfLinkTarget(container);
	}

	private Path memberOfContainer(MemberContainer container, Access access) {

		final MemberPath memberPath = container.member(
				access,
				this.memberId,
				this.declaredIn != null
				? this.declaredIn.getType() : null);

		if (memberPath != null) {
			return memberPath.pathToMember();
		}

		return memberOfAdapter(access, container);
	}

	private Path memberOfAdapter(
			Access access,
			MemberContainer container) {
		if (this.declaredIn == null) {
			return null;
		}

		final MemberPath adapterPath =
				container.member(access, adapterId(this.declaredIn), null);

		if (adapterPath == null) {
			return null;
		}

		final Member adapterMember = adapterPath.toMember();

		if (adapterMember == null) {
			return null;
		}

		final Container adapter = adapterMember.substance(dummyUser());

		if (adapter == null) {
			return null;
		}

		final Accessor memberOfAdapterAccessor;

		if (adapter.getLocation().getContext().declarationsVisibleFrom(
				getLocation().getContext())) {
			memberOfAdapterAccessor = Accessor.DECLARATION;
		} else {
			memberOfAdapterAccessor = Accessor.PUBLIC;
		}

		final MemberPath memberOfAdapter = adapter.member(
				memberOfAdapterAccessor.accessBy(this, this.accessSource),
				this.memberId,
				null);

		if (memberOfAdapter == null) {
			return null;
		}

		return adapterPath.pathToMember().append(
				memberOfAdapter.pathToMember());
	}

	private Path memberOfLinkTarget(MemberContainer container) {
		if (!linkTargetIsAccessibleFrom(this.accessSource)) {
			return null;
		}

		final Obj object = container.toObject();

		if (object == null) {
			return null;
		}

		final LinkValueType linkType =
				object.type().getValueType().toLinkType();

		if (linkType == null) {
			return null;
		}

		final Obj iface =
				linkType.interfaceRef(object.type().getParameters()).getType();
		final Path targetMember = memberOfContainer(
				iface,
				Accessor.PUBLIC.accessBy(this, this.accessSource));

		if (targetMember == null) {
			return null;
		}

		return SELF_PATH.dereference().append(targetMember);
	}

}

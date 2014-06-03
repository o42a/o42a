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
import static org.o42a.core.member.MemberIdKind.FIELD_NAME;
import static org.o42a.core.ref.RefUser.dummyRefUser;
import static org.o42a.core.ref.path.Path.FALSE_PATH;
import static org.o42a.core.ref.path.Path.SELF_PATH;
import static org.o42a.core.ref.path.Path.VOID_PATH;
import static org.o42a.core.ref.path.PathResolver.pathResolver;
import static org.o42a.util.string.Capitalization.CASE_INSENSITIVE;

import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.compiler.ip.access.AccessRules;
import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.member.*;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathExpander;
import org.o42a.core.ref.path.PathResolution;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.link.LinkValueType;
import org.o42a.util.CheckResult;
import org.o42a.util.fn.Holder;


public class MemberById extends ContainedFragment {

	private static final MemberName VOID_MEMBER =
			FIELD_NAME.memberName(CASE_INSENSITIVE.canonicalName("void"));
	private static final MemberName FALSE_MEMBER =
			FIELD_NAME.memberName(CASE_INSENSITIVE.canonicalName("false"));

	private final AccessRules accessRules;
	private final StaticTypeRef declaredIn;
	private final MemberId memberId;

	public MemberById(
			LocationInfo location,
			AccessDistributor distributor,
			MemberId memberId,
			StaticTypeRef declaredIn) {
		super(location, distributor);
		this.accessRules = distributor.getAccessRules();
		this.memberId = memberId;
		this.declaredIn = declaredIn;
	}

	@Override
	public Path expand(PathExpander expander, int index, Scope start) {

		final Obj declaredIn;

		if (this.declaredIn != null) {
			declaredIn = this.declaredIn.getType();
		} else {
			declaredIn = null;
		}

		return path(getContainer(), declaredIn);
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
		if (this.memberId == null) {
			return super.toString();
		}
		return this.memberId.toString();
	}

	private Path path(Container container, Obj declaredIn) {

		final Holder<Path> memberOfContainer =
				findInContainer(container, declaredIn);

		if (memberOfContainer != null) {
			return memberOfContainer.get();
		}

		final Container enclosing = container.getEnclosingContainer();

		if (enclosing == null) {

			final Path topLevelObject = topLevelObject(declaredIn);

			if (topLevelObject != null) {
				return topLevelObject;
			}

			getLogger().unresolved(getLocation(), this.memberId);

			return null;
		}

		final Path found = path(enclosing, declaredIn);

		if (found == null) {
			return null;
		}
		if (found.isAbsolute()) {
			return found;
		}

		final Scope enclosingScope = enclosing.getScope();
		final PathResolution pathResolution =
				found.bind(this, enclosingScope).resolve(
						pathResolver(enclosingScope, dummyRefUser()));

		if (!pathResolution.isResolved()) {
			return null;
		}

		final Container result = pathResolution.getResult();

		if (enclosingScope.is(container.getScope())) {
			return found;
		}
		if (result.getScope().is(container.getScope())) {
			return SELF_PATH;
		}

		final Path enclosingScopePath =
				container.getScope().getEnclosingScopePath();

		assert enclosingScopePath != null :
			found + " should be an absolute path";

		return enclosingScopePath.append(found);
	}

	private Path topLevelObject(Obj declaredIn) {
		if (declaredIn != null) {
			return null;
		}
		if (this.memberId.equals(VOID_MEMBER)) {
			return VOID_PATH;
		}
		if (this.memberId.equals(FALSE_MEMBER)) {
			return FALSE_PATH;
		}
		return null;
	}

	private Holder<Path> findInContainer(
			Container container,
			Obj declaredIn) {
		if (!this.accessRules.containerIsVisible(getContainer(), container)) {
			return null;
		}

		final Holder<Path> memberOfContainer = memberOrMatchingContainer(
				encosingAccessor(container),
				container,
				declaredIn);

		if (memberOfContainer != null) {
			return memberOfContainer;
		}

		return memberOfLinkTarget(container, declaredIn);
	}

	private Accessor encosingAccessor(Container container) {
		if (getScope().is(container.getScope())) {
			return Accessor.OWNER;
		}
		if (container.getLocation().getContext().declarationsVisibleFrom(
				getLocation().getContext())) {
			return Accessor.DECLARATION;
		}
		return Accessor.ENCLOSED;
	}

	private Holder<Path> memberOrMatchingContainer(
			Accessor accessor,
			Container container,
			Obj declaredIn) {

		final Holder<Path> memberOfAdapter =
				memberOfContainer(accessor, container, declaredIn);

		if (memberOfAdapter != null) {
			return memberOfAdapter;
		}

		return matchingContainer(container, declaredIn);
	}

	private Holder<Path> memberOfContainer(
			Accessor accessor,
			Container container,
			Obj declaredIn) {

		final Access access =
				accessor.accessBy(this, this.accessRules.getSource());
		final MemberPath found =
				container.findMember(access, this.memberId, declaredIn);

		if (found != null) {
			return new Holder<>(found.pathToMember());
		}

		return memberOfAdapter(access, container);
	}

	private Holder<Path> memberOfAdapter(Access access, Container container) {
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

		final MemberPath memberOfAdapter = adapter.member(
				memberOfAdapterAccessor(adapter)
				.accessBy(this, this.accessRules.getSource()),
				this.memberId,
				null);

		if (memberOfAdapter == null) {
			return null;
		}

		return new Holder<>(
				adapterPath.pathToMember()
				.append(memberOfAdapter.pathToMember()));
	}

	private Accessor memberOfAdapterAccessor(final Container adapter) {
		if (adapter.getLocation().getContext().declarationsVisibleFrom(
				getLocation().getContext())) {
			return Accessor.DECLARATION;
		}
		return Accessor.PUBLIC;
	}

	private Holder<Path> matchingContainer(
			Container container,
			Obj declaredIn) {

		final MemberPath matchingPath =
				container.matchingPath(this.memberId, declaredIn);

		if (matchingPath == null) {
			return null;
		}

		final CheckResult accessibilityCheck =
				this.accessRules.checkContainerAccessibility(
						this,
						getContainer(),
						container);

		if (accessibilityCheck.isError()) {
			return new Holder<>(null);
		}

		assert accessibilityCheck.isOk() :
			"Wrong visibility check implementation";

		return new Holder<>(matchingPath.pathToMember());
	}

	private Holder<Path> memberOfLinkTarget(
			Container container,
			Obj declaredIn) {
		if (!linkTargetIsAccessibleFrom(this.accessRules.getSource())) {
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
		final Holder<Path> targetMember =
				memberOfContainer(Accessor.PUBLIC, iface, declaredIn);

		if (targetMember == null) {
			return null;
		}

		final Path targetMemberPath = targetMember.get();

		if (targetMemberPath == null) {
			return targetMember;
		}

		return new Holder<>(SELF_PATH.dereference().append(targetMemberPath));
	}

}

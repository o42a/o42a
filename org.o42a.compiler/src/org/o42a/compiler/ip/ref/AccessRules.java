/*
    Compiler
    Copyright (C) 2013 Ruslan Lopatin

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

import static org.o42a.compiler.ip.Interpreter.CLAUSE_DECL_IP;
import static org.o42a.compiler.ip.ref.RefInterpreter.matchModule;
import static org.o42a.compiler.ip.ref.RefInterpreter.prototypeExpressionClause;
import static org.o42a.core.member.AccessSource.FROM_DECLARATION;
import static org.o42a.core.member.AccessSource.FROM_DEFINITION;
import static org.o42a.core.ref.path.Path.SELF_PATH;

import org.o42a.compiler.ip.Interpreter;
import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.member.*;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.string.Name;


public abstract class AccessRules {

	public static final AccessRules ACCESS_FROM_DECLARATION =
			new SimpleAccessRules(FROM_DECLARATION);
	public static final AccessRules ACCESS_FROM_DEFINITION =
			new SimpleAccessRules(FROM_DEFINITION);

	private final AccessSource source;

	public AccessRules(AccessSource source) {
		assert source != null :
			"Access source not specified";
		this.source = source;
	}

	public final AccessSource getSource() {
		return this.source;
	}

	public abstract AccessRules setSource(AccessSource source);

	public abstract Ref selfRef(
			Interpreter ip,
			LocationInfo location,
			AccessDistributor distributor);

	public final Ref parentRef(
			Interpreter ip,
			LocationInfo location,
			AccessDistributor distributor,
			Name name) {

		final Path path = parentPath(ip, location, distributor, name);

		if (path == null) {
			return null;
		}

		return path.bind(location, distributor.getScope()).target(distributor);
	}

	public Ref memberById(
			Interpreter ip,
			LocationInfo location,
			AccessDistributor distributor,
			MemberId memberId,
			StaticTypeRef declaredIn) {
		return new MemberById(
				ip,
				location,
				distributor,
				memberId,
				declaredIn).toRef();
	}

	public abstract boolean checkAccessibility(
			LocationInfo location,
			AccessDistributor distributor,
			Container to);

	public final AccessDistributor distribute(Distributor distributor) {
		if (distributor.getClass() != AccessDistributor.class) {
			return new AccessDistributor(distributor, this);
		}
		return distribute((AccessDistributor) distributor);
	}

	public final AccessDistributor distribute(AccessDistributor distributor) {
		return distributor.setAccessRules(this);
	}

	@Override
	public String toString() {

		final AccessSource source = getSource();

		if (source == null) {
			return super.toString();
		}

		return "ACCESS_" + source.toString();
	}

	protected final Ref defaultSelfRef(
			LocationInfo location,
			AccessDistributor distributor) {
		return SELF_PATH.bind(location, distributor.getScope())
				.target(distributor);
	}

	private Path parentPath(
			Interpreter ip,
			LocationInfo location,
			AccessDistributor distributor,
			Name name) {

		Path path = SELF_PATH;
		Path parentPath = SELF_PATH;
		Container nested = null;
		Container container = distributor.getContainer();

		for (;;) {

			if (containerHasName(container, name)) {
				if (!checkAccessibility(location, distributor, container)) {
					return null;
				}
				if (!skip(ip, nested)) {
					return path.append(parentPath);
				}
			}

			nested = container;

			final Container parent = container.getParentContainer();

			if (parent == null) {
				unresolvedParent(location, name);
				return null;
			}

			final Scope scope = container.getScope();
			final Path enclosingScopePath = scope.getEnclosingScopePath();

			if (enclosingScopePath == null) {
				unresolvedParent(location, name);
				return null;
			}

			if (scope.is(parent.getScope())) {

				final Member parentMember = parent.toMember();

				if (parentMember == null
						|| scope.getContainer().toMember()
						== parentMember) {
					parentPath = SELF_PATH;
				} else {
					parentPath = parentMember.getMemberKey().toPath();
				}
				container = parent;
				continue;
			}

			container = parent;
			parentPath = SELF_PATH;
			if (path != null) {
				path = path.append(enclosingScopePath);
			} else {
				path = enclosingScopePath;
			}
		}
	}

	private boolean containerHasName(Container container, Name name) {
		if (name == null) {
			return true;
		}

		final Member member = container.toMember();

		if (member == null) {
			return matchModule(name, container);
		}

		final MemberName memberName = member.getMemberKey().getMemberName();

		if (memberName == null) {
			return false;
		}

		return name.is(memberName.getName());
	}

	private static boolean skip(Interpreter ip, Container nested) {
		if (nested == null) {
			return false;
		}
		if (ip == CLAUSE_DECL_IP) {
			return false;
		}
		// Top-level expression clause
		// shouldn't have access to enclosing prototype.
		return prototypeExpressionClause(nested);
	}

	private static void unresolvedParent(LocationInfo location, Name name) {
		location.getLocation().getLogger().error(
				"unresolved_parent",
				location,
				"Enclosing member '%s' can not be found",
				name);
	}

}
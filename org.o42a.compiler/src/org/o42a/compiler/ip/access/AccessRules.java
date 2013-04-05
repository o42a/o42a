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
package org.o42a.compiler.ip.access;

import static org.o42a.compiler.ip.ref.RefInterpreter.matchModule;
import static org.o42a.core.member.AccessSource.FROM_CLAUSE_REUSE;
import static org.o42a.core.member.AccessSource.FROM_DECLARATION;
import static org.o42a.core.member.AccessSource.FROM_DEFINITION;
import static org.o42a.core.ref.path.Path.SELF_PATH;

import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.st.StatementsAccess;
import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.member.AccessSource;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberName;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.Statements;
import org.o42a.util.CheckResult;
import org.o42a.util.string.Name;


public abstract class AccessRules {

	public static final AccessRules ACCESS_FROM_TYPE =
			new TypeDefinitionAccessRules();
	public static final AccessRules ACCESS_FROM_DECLARATION =
			new SimpleAccessRules(FROM_DECLARATION);
	public static final AccessRules ACCESS_FROM_CLAUSE_REUSE =
			new SimpleAccessRules(FROM_CLAUSE_REUSE);
	public static final AccessRules ACCESS_FROM_DEFINITION =
			new SimpleAccessRules(FROM_DEFINITION);
	public static final AccessRules ACCESS_FROM_PLACEMENT =
			ACCESS_FROM_DECLARATION;
	public static final AccessRules ACCESS_FROM_TITLE =
			ACCESS_FROM_DECLARATION;
	public static final AccessRules ACCESS_FROM_HEADER =
			ACCESS_FROM_DEFINITION;
	public static final AccessRules ACCESS_FROM_PATH_COMPILER =
			ACCESS_FROM_DECLARATION;

	private final AccessSource source;

	public AccessRules(AccessSource source) {
		assert source != null :
			"Access source not specified";
		this.source = source;
	}

	public final AccessSource getSource() {
		return this.source;
	}

	public abstract Ref selfRef(
			Interpreter ip,
			LocationInfo location,
			AccessDistributor distributor);

	public final Ref parentRef(
			LocationInfo location,
			AccessDistributor distributor,
			Name name) {

		final Path path = parentPath(location, distributor, name);

		if (path == null) {
			return null;
		}

		return path.bind(location, distributor.getScope()).target(distributor);
	}

	/**
	 * Checks whether the given enclosing container is accessible from the
	 * nested one.
	 *
	 * <p>This method can report error if access is explicitly prohibited,
	 * such as in case of attempt to access an object from its type definition.
	 * </p>
	 *
	 * @param location an accessing reference location.
	 * @param from an accessing reference container.
	 * @param to the accessed enclosing container.
	 *
	 * @return the result of the check.
	 */
	public abstract CheckResult checkContainerAccessibility(
			LocationInfo location,
			Container from,
			Container to);

	/**
	 * Checks whether the given enclosing container is visible from the nested
	 * one.
	 *
	 * <p>In contrast to {@link #checkContainerAccessibility(LocationInfo,
	 * Container, Container)} method, this one does not report errors.
	 * It is used to check the visibility of intermediate enclosing container
	 * instead of a final one.</p>
	 *
	 * @param by an accessing reference container.
	 * @param what the accessed enclosing container.
	 *
	 * @return the result of the check.
	 */
	public abstract boolean containerIsVisible(Container by, Container what);

	public abstract AccessRules typeRules();

	public abstract AccessRules declarationRules();

	public abstract AccessRules contentRules();

	public abstract AccessRules clauseReuseRules();

	public StatementsAccess statements(Statements<?, ?> statements) {
		return new StatementsAccess(this, statements);
	}

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
			LocationInfo location,
			AccessDistributor distributor,
			Name name) {

		final Container from = distributor.getContainer();
		Path path = SELF_PATH;
		Path parentPath = SELF_PATH;
		Container container = from;

		for (;;) {
			if (containerHasName(container, name)) {

				final CheckResult checkResult =
						checkContainerAccessibility(location, from, container);

				if (checkResult.isError()) {
					return null;
				}
				if (checkResult.isOk()) {
					return path.append(parentPath);
				}
			}

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
						|| scope.getContainer().toMember() == parentMember) {
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

	private static void unresolvedParent(LocationInfo location, Name name) {
		location.getLocation().getLogger().error(
				"unresolved_parent",
				location,
				"Enclosing member '%s' can not be found",
				name);
	}

}

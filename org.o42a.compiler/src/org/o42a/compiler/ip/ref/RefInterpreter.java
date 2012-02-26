/*
    Compiler
    Copyright (C) 2011,2012 Ruslan Lopatin

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
import static org.o42a.compiler.ip.ref.MemberById.prototypeExpressionClause;
import static org.o42a.core.ref.path.Path.ROOT_PATH;
import static org.o42a.core.ref.path.Path.SELF_PATH;

import org.o42a.ast.expression.ExpressionNode;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.member.Member;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.LocationInfo;


public class RefInterpreter {

	public static Path enclosingModulePath(Container of) {

		Container container = of;

		if (container.getScope().isTopScope()) {
			return ROOT_PATH;
		}

		Path result = null;

		for (;;) {

			final Container enclosing =
					container.getScope().getEnclosingContainer();

			if (enclosing.getScope().isTopScope()) {
				if (result == null) {
					return SELF_PATH;
				}
				return result;
			}

			final Path enclosingScopePath =
					container.getScope().getEnclosingScopePath();

			if (result == null) {
				result = enclosingScopePath;
			} else {
				result = result.append(enclosingScopePath);
			}

			container = enclosing;
		}
	}

	public static Path parentPath(
			Interpreter ip,
			LocationInfo location,
			String name,
			Container of) {

		Path path = SELF_PATH;
		Path parentPath = SELF_PATH;
		Container nested = null;
		Container container = of;

		for (;;) {
			if (match(name, container) && !skip(ip, nested)) {
				return path.append(parentPath);
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

			if (scope == parent.getScope()) {

				final Member parentMember = parent.toMember();

				if (parentMember == null
						|| scope.getContainer().toMember()
						== parentMember) {
					parentPath = SELF_PATH;
				} else {
					parentPath = parentMember.getKey().toPath();
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

	public static Path clauseObjectPath(LocationInfo location, Scope of) {

		Scope scope = of;
		Path path = Path.SELF_PATH;

		for (;;) {

			final Clause clause = scope.getContainer().toClause();

			if (clause == null) {

				final Obj object = scope.toObject();

				if (object == null) {
					location.getContext().getLogger().error(
							"unresolved_object_intrinsic",
							location,
							"Enclosing object not found");
					return null;
				}

				return path;
			}

			final Scope enclosingScope = scope.getEnclosingScope();

			if (enclosingScope == null) {
				return null;
			}

			path = path.append(scope.getEnclosingScopePath());
			scope = enclosingScope;
		}
	}

	public static boolean isRootRef(ExpressionNode node) {
		return node.accept(RootVisitor.ROOT_VISITOR, null) != null;
	}

	private RefInterpreter() {
	}

	private static boolean match(String name, Container container) {

		final Member member = container.toMember();

		if (member == null) {
			return false;
		}
		if (name == null) {
			return true;
		}

		return name.equals(member.getKey().getName());
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

	private static void unresolvedParent(LocationInfo location, String name) {
		location.getContext().getLogger().error(
				"unresolved_parent",
				location,
				"Enclosing member '%s' can not be found",
				name);
	}

}

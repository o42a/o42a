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

import static org.o42a.compiler.ip.Interpreter.CLAUSE_DECL_IP;
import static org.o42a.compiler.ip.ref.MemberById.prototypeExpressionClause;
import static org.o42a.core.ref.path.Path.SELF_PATH;

import org.o42a.ast.Node;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.member.Member;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.Wrap;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Location;


public class ParentRef extends Wrap {

	private final Interpreter ip;
	private final String name;

	public ParentRef(
			Interpreter ip,
			CompilerContext context,
			Node node,
			String name,
			Distributor distributor) {
		super(new Location(context, node), distributor);
		this.ip = ip;
		this.name = name;
	}

	@Override
	public String toString() {
		if (this.name == null) {
			return super.toString();
		}
		return this.name + "::";
	}

	@Override
	protected Ref resolveWrapped() {

		Path path = SELF_PATH;
		Path parentPath = SELF_PATH;
		Container nested = null;
		Container container = getContainer();

		for (;;) {
			if (match(container) && !skip(nested)) {
				return path.append(parentPath).target(this, distribute());
			}

			nested = container;

			final Container parent = container.getParentContainer();

			if (parent == null) {
				unresolved();
				return errorRef(this);
			}

			final Scope scope = container.getScope();
			final Path enclosingScopePath = scope.getEnclosingScopePath();

			if (enclosingScopePath == null) {
				unresolved();
				return errorRef(this);
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

	private boolean match(Container container) {

		final Member member = container.toMember();

		if (member == null) {
			return false;
		}
		if (this.name == null) {
			return true;
		}

		return this.name.equals(member.getKey().getName());
	}

	private boolean skip(Container nested) {
		if (nested == null) {
			return false;
		}
		if (this.ip == CLAUSE_DECL_IP) {
			return false;
		}
		// Top-level expression clause
		// shouldn't have access to enclosing prototype.
		return prototypeExpressionClause(nested);
	}

	public void unresolved() {
		getLogger().error(
				"unresolved_parent",
				this,
				"Enclosing member '%s' can not be found",
				this.name);
	}

}

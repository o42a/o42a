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

import org.o42a.ast.Node;
import org.o42a.core.*;
import org.o42a.core.member.Member;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.Wrap;
import org.o42a.core.ref.path.Path;


public class ParentRef extends Wrap {

	private final String name;

	public ParentRef(
			CompilerContext context,
			Node node,
			String name,
			Distributor distributor) {
		super(new Location(context, node), distributor);
		this.name = name;
	}

	@Override
	protected Ref resolveWrapped() {

		Path path = SELF_PATH;
		Path parentPath = SELF_PATH;
		Container enclosing = getContainer();

		for (;;) {

			final Member member = enclosing.toMember();

			if (member != null) {
				if (this.name == null
						|| this.name.equals(member.getKey().getName())) {
					return path.append(parentPath).target(this, distribute());
				}
			}

			final Container parent = enclosing.getParentContainer();

			if (parent == null) {
				getLogger().unresolvedParent(this, this.name);
				return errorRef(this);
			}

			final Scope enclosingScope = enclosing.getScope();
			final Path enclosingScopePath =
				enclosingScope.getEnclosingScopePath();

			if (enclosingScopePath == null) {
				getLogger().unresolvedParent(this, this.name);
				return errorRef(this);
			}

			if (enclosingScope == parent.getScope()) {

				final Member parentMember = parent.toMember();

				if (parentMember == null
						|| enclosingScope.getContainer().toMember()
						== parentMember) {
					parentPath = SELF_PATH;
				} else {
					parentPath = parentMember.getKey().toPath();
				}
				enclosing = parent;
				continue;
			}

			enclosing = parent;
			parentPath = SELF_PATH;
			if (path != null) {
				path = path.append(enclosingScopePath);
			} else {
				path = enclosingScopePath;
			}
		}
	}

}

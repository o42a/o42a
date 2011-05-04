/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.core.member.clause;

import org.o42a.core.member.MemberRegistry;
import org.o42a.core.member.local.LocalRegistry;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.st.sentence.Group;
import org.o42a.util.Lambda;


final class ImperativeGroupRegistry extends LocalRegistry {

	private ImperativeGroupRegistry(
			LocalScope scope,
			MemberRegistry ownerRegistry) {
		super(scope, ownerRegistry);
	}

	@Override
	public ClauseBuilder newClause(ClauseDeclaration declaration) {
		if (declaration.getKind() == ClauseKind.OVERRIDER) {
			declaration.getLogger().prohibitedOverriderClause(declaration);
			return null;
		}

		final MemberRegistryClauseBase registry = this;

		return registry.createClause(declaration);
	}

	static final class Builder implements Lambda<MemberRegistry, LocalScope> {

		private final Group group;

		Builder(Group group) {
			this.group = group;
		}

		@Override
		public MemberRegistry get(LocalScope arg) {
			return new ImperativeGroupRegistry(
					arg,
					this.group.getStatements().getMemberRegistry());
		}

	}

}

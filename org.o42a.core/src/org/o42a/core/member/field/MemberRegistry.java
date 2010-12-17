/*
    Compiler Core
    Copyright (C) 2010 Ruslan Lopatin

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
package org.o42a.core.member.field;

import org.o42a.core.LocationSpec;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.Member;
import org.o42a.core.member.clause.ClauseBuilder;
import org.o42a.core.member.clause.ClauseDeclaration;
import org.o42a.core.member.local.MemberRegistryLocalBase;


public abstract class MemberRegistry extends MemberRegistryLocalBase {

	public static MemberRegistry noDeclarations() {
		return new NoDeclarations();
	}

	public abstract DeclaredField<?> declareField(FieldDeclaration declaration);

	public MemberRegistry prohibitDeclarations() {
		return new ProhibitDeclarations(this);
	}

	private static class NoDeclarations extends MemberRegistry {

		@Override
		public Obj getOwner() {
			return null;
		}

		@Override
		public DeclaredField<?> declareField(FieldDeclaration declaration) {
			declaration.getContext().getLogger().prohibitedDeclaration(
					declaration);
			return null;
		}

		@Override
		public MemberRegistry prohibitDeclarations() {
			return this;
		}

		@Override
		public ClauseBuilder newClause(ClauseDeclaration declaration) {
			declaration.getLogger().prohibitedDeclaration(declaration);
			return null;
		}

		@Override
		public void declareMember(Member member) {
			member.getLogger().prohibitedDeclaration(member);
		}

		@Override
		public boolean declareBlock(LocationSpec location, String name) {
			location.getContext().getLogger().prohibitedDeclaration(location);
			return false;
		}

		@Override
		public String anonymousBlockName() {
			throw new UnsupportedOperationException();
		}

	}

	private static final class ProhibitDeclarations
			extends NoDeclarations {

		private final MemberRegistry registry;

		ProhibitDeclarations(MemberRegistry registry) {
			this.registry = registry;
		}

		@Override
		public Obj getOwner() {
			return this.registry.getOwner();
		}

		@Override
		public boolean declareBlock(LocationSpec location, String name) {
			return this.registry.declareBlock(location, name);
		}

	}

}

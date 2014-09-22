/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.core.member;

import static org.o42a.core.ref.path.Path.staticPath;

import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.Local;


public abstract class NonAliasMember extends Member {

	private final NonAliasPath memberPath = new NonAliasPath(this);

	public NonAliasMember(
			LocationInfo location,
			Distributor distributor,
			Obj owner) {
		super(location, distributor, owner);
	}

	@Override
	public final MemberPath getMemberPath() {
		return this.memberPath;
	}

	@Override
	public final boolean isAlias() {
		return false;
	}

	private static final class NonAliasPath implements MemberPath {

		private final NonAliasMember member;

		NonAliasPath(NonAliasMember member) {
			this.member = member;
		}

		@Override
		public final Path pathToMember() {
			if (this.member.isStatic()) {

				final Scope scope = this.member.getScope();

				return staticPath(scope, scope)
						.append(this.member.getMemberKey());
			}

			return this.member.getMemberKey().toPath();
		}

		@Override
		public final Member toMember() {
			return this.member;
		}

		@Override
		public final Local toLocal() {
			return null;
		}

		@Override
		public String toString() {
			if (this.member == null) {
				return super.toString();
			}
			return this.member.toString();
		}

	}

}

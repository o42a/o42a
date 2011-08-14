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
package org.o42a.core.member;

import java.util.HashSet;

import org.o42a.util.use.*;


final class MemberUses implements UserInfo {

	private final String name;
	private final Member member;
	private final MemberUser user;
	private final UseTracker tracker = new UseTracker();
	private final HashSet<UseInfo> uses = new HashSet<UseInfo>();

	MemberUses(String name, Member member) {
		this.name = name;
		this.member = member;
		this.user = new MemberUser();
	}

	@Override
	public User toUser() {
		return this.user;
	}

	public final void useBy(UseInfo use) {
		this.uses.add(use);
	}

	@Override
	public final boolean isUsedBy(UseCaseInfo useCase) {
		return getUseBy(useCase).isUsed();
	}

	@Override
	public UseFlag getUseBy(UseCaseInfo useCase) {
		if (!this.tracker.start(useCase.toUseCase())) {
			return this.tracker.getUseFlag();
		}
		for (UseInfo use : this.uses) {
			if (this.tracker.useBy(use)) {
				return this.tracker.getUseFlag();
			}
		}
		return this.tracker.done();
	}

	@Override
	public String toString() {
		if (this.member == null) {
			return super.toString();
		}
		return this.name + '[' + this.member + ']';
	}

	private final class MemberUser extends AbstractUser {

		@Override
		public UseFlag getUseBy(UseCaseInfo useCase) {
			return null;
		}

		@Override
		public String toString() {
			return MemberUses.this.toString();
		}

	}

}

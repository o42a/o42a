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
package org.o42a.core.member.impl;

import static org.o42a.util.use.SimpleUsage.ALL_SIMPLE_USAGES;

import java.util.HashSet;

import org.o42a.core.member.Member;
import org.o42a.util.use.*;


public final class MemberUses implements UserInfo, Uses<SimpleUsage> {

	private final String name;
	private final Member member;
	private final MemberUser user;
	private final UseTracker tracker = new UseTracker();
	private final HashSet<Uses<?>> uses = new HashSet<Uses<?>>();

	public MemberUses(String name, Member member) {
		this.name = name;
		this.member = member;
		this.user = new MemberUser();
	}

	@Override
	public final User<SimpleUsage> toUser() {
		return this.user;
	}

	@Override
	public final AllUsages<SimpleUsage> allUsages() {
		return toUser().allUsages();
	}

	@Override
	public UseFlag selectUse(
			UseCaseInfo useCase,
			UseSelector<SimpleUsage> selector) {

		final UseCase uc = useCase.toUseCase();

		if (uc.isSteady()) {
			return uc.usedFlag();
		}
		if (!this.tracker.start(uc)) {
			return this.tracker.getUseFlag();
		}
		for (Uses<?> use : this.uses) {
			if (this.tracker.useBy(use)) {
				return this.tracker.getUseFlag();
			}
		}
		return this.tracker.done();
	}

	@Override
	public boolean isUsed(
			UseCaseInfo useCase,
			UseSelector<SimpleUsage> selector) {
		return selectUse(useCase, selector).isUsed();
	}

	public final void useBy(Uses<?> use) {
		this.uses.add(use);
	}

	@Override
	public String toString() {
		if (this.member == null) {
			return super.toString();
		}
		return this.name + '[' + this.member + ']';
	}

	private final class MemberUser extends AbstractUser<SimpleUsage> {

		MemberUser() {
			super(ALL_SIMPLE_USAGES);
		}

		@Override
		public UseFlag selectUse(
				UseCaseInfo useCase,
				UseSelector<SimpleUsage> selector) {
			return MemberUses.this.selectUse(useCase, selector);
		}

		@Override
		public String toString() {
			return MemberUses.this.toString();
		}

	}

}

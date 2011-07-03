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

import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.member.field.Field;
import org.o42a.util.use.*;


public class FieldUses implements UseInfo {

	private final UseTracker tracker = new UseTracker();
	private final MemberContainer container;

	public FieldUses(MemberContainer container) {
		this.container = container;
	}

	public final MemberContainer getContainer() {
		return this.container;
	}

	@Override
	public UseFlag getUseBy(UseCase useCase) {
		if (!this.tracker.start(useCase)) {
			return this.tracker.getUseFlag();
		}

		for (Member member : getContainer().getMembers()) {

			final Field<?> field = member.toField(dummyUser());

			if (field == null) {
				continue;
			}
			if (this.tracker.useBy(member.getAnalysis())) {
				return this.tracker.getUseFlag();
			}
		}

		return this.tracker.done();
	}

	@Override
	public boolean isUsedBy(UseCase useCase) {
		return getUseBy(useCase).isUsed();
	}

	@Override
	public String toString() {
		return "FieldUses[" + this.container + ']';
	}

}

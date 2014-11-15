/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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

import static org.o42a.analysis.use.SimpleUsage.ALL_SIMPLE_USAGES;

import org.o42a.analysis.use.*;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberContainer;


public class FieldUses implements Uses<SimpleUsage> {

	private final UseTracker tracker = new UseTracker();
	private final MemberContainer container;

	public FieldUses(MemberContainer container) {
		this.container = container;
	}

	public final MemberContainer getContainer() {
		return this.container;
	}

	@Override
	public final AllUsages<SimpleUsage> allUsages() {
		return ALL_SIMPLE_USAGES;
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
			return this.tracker.lastFlag();
		}

		for (Member member : getContainer().getMembers()) {

			final MemberField field = member.toField();

			if (field == null) {
				continue;
			}
			if (this.tracker.check(field.getAnalysis().uses())) {
				return this.tracker.lastFlag();
			}
		}

		return this.tracker.unused();
	}

	@Override
	public String toString() {
		return "FieldUses[" + this.container + ']';
	}

}

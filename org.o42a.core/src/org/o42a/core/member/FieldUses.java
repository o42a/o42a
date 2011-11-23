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

import org.o42a.core.member.field.MemberField;
import org.o42a.util.use.*;


public class FieldUses implements Uses {

	private final UseTracker tracker = new UseTracker();
	private final MemberContainer container;

	public FieldUses(MemberContainer container) {
		this.container = container;
	}

	public final MemberContainer getContainer() {
		return this.container;
	}

	@Override
	public UseFlag getUseBy(UseCaseInfo useCase) {
		if (!this.tracker.start(useCase.toUseCase())) {
			return this.tracker.getUseFlag();
		}

		for (Member member : getContainer().getMembers()) {

			final MemberField field = member.toField();

			if (field == null) {
				continue;
			}
			if (this.tracker.useBy(field.getAnalysis())) {
				return this.tracker.getUseFlag();
			}
		}

		return this.tracker.done();
	}

	@Override
	public boolean isUsedBy(UseCaseInfo useCase) {
		return getUseBy(useCase).isUsed();
	}

	@Override
	public String toString() {
		return "FieldUses[" + this.container + ']';
	}

}

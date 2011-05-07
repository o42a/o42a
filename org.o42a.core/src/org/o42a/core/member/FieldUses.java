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

import org.o42a.util.use.UseCase;
import org.o42a.util.use.UseFlag;
import org.o42a.util.use.UseInfo;


public class FieldUses implements UseInfo {

	private final MemberContainer container;
	private UseFlag used;

	public FieldUses(MemberContainer container) {
		this.container = container;
	}

	@Override
	public boolean isUsedBy(UseCase useCase) {
		if (useCase.caseFlag(this.used)) {
			return this.used.isUsed();
		}
		for (Member member : this.container.getMembers()) {
			if (member.toField(dummyUser()) == null) {
				continue;
			}
			if (member.getAnalysis().isUsedBy(useCase)) {
				this.used = useCase.usedFlag();
				return true;
			}
		}
		this.used = useCase.unusedFlag();
		return false;
	}

	@Override
	public String toString() {
		return "FieldUses[" + this.container + ']';
	}

}

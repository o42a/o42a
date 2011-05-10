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

import org.o42a.core.Scope;
import org.o42a.core.member.MemberAnalysis.Status;
import org.o42a.core.member.field.Field;
import org.o42a.util.use.UseCase;
import org.o42a.util.use.UseFlag;
import org.o42a.util.use.UseInfo;


public class FieldUses implements UseInfo {

	private static final byte LIKE_SCOPE = -2;
	private static final byte ANALYSING = -1;
	private static final byte ANALYSED = 1;

	private final MemberContainer container;
	private UseFlag used;
	private byte analysed;

	public FieldUses(MemberContainer container) {
		this.container = container;
	}

	@Override
	public boolean isUsedBy(UseCase useCase) {
		if (useCase.caseFlag(this.used)) {
			return this.used.isUsed();
		}
		if (this.analysed == 0) {
			this.analysed = ANALYSING;
			if (fieldsUsedBy(useCase, false)) {
				return true;
			}
		} else if (this.analysed != LIKE_SCOPE) {
			return false;
		}
		return fieldsUsedBy(useCase, true);
	}

	@Override
	public String toString() {
		return "FieldUses[" + this.container + ']';
	}

	private boolean fieldsUsedBy(UseCase useCase, boolean scope) {

		boolean fieldsPresent = false;
		boolean likeScope = false;

		for (Member member : this.container.getMembers()) {

			final Field<?> field = member.toField(dummyUser());

			if (field == null) {
				continue;
			}

			fieldsPresent = true;

			if (isScopeField(field) != scope) {
				continue;
			}

			final MemberAnalysis analysis = member.getAnalysis();

			if (analysis.getStatus() == Status.ANALYSING) {
				assert scope :
					member + " is analysing, but is not a scope";
				likeScope = true;
				continue;
			}
			if (analysis.isUsedBy(useCase)) {
				this.analysed = ANALYSED;
				this.used = useCase.usedFlag();
				return true;
			}
		}
		if (likeScope) {
			this.analysed = LIKE_SCOPE;
			return false;
		}
		return !fieldsPresent;
	}

	private boolean isScopeField(Field<?> field) {
		if (field.isScopeField()) {
			return false;
		}

		final Scope enclosingScope =
			this.container.getScope().getEnclosingScope();

		return field.getArtifact().getScope() == enclosingScope;
	}

}

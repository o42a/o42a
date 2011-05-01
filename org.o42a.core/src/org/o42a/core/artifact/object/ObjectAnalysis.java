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
package org.o42a.core.artifact.object;

import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.member.Member;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.util.use.UseCase;
import org.o42a.util.use.UseFlag;


public class ObjectAnalysis {

	private final Obj object;
	private UseFlag fieldsAccessed;
	private byte staticAncestor;

	ObjectAnalysis(Obj object) {
		this.object = object;
	}

	public final Obj getObject() {
		return this.object;
	}

	public final boolean hasStaticAncestor() {
		if (this.staticAncestor != 0) {
			return this.staticAncestor > 0;
		}

		final TypeRef ancestor =
			getObject().type().useBy(dummyUser()).getAncestor();

		if (ancestor == null || ancestor.isStatic()) {
			this.staticAncestor = 1;
			return true;
		}

		this.staticAncestor = -1;
		return false;
	}

	public final boolean typeAccessedBy(UseCase useCase) {
		return getObject().type().isUsedBy(useCase);
	}

	public final boolean fieldsAccessedBy(UseCase useCase) {
		if (useCase.caseFlag(this.fieldsAccessed)) {
			return this.fieldsAccessed.isUsed();
		}
		this.fieldsAccessed = useCase.checkUseFlag();

		final boolean result = determineFieldsAccessedBy(useCase);

		this.fieldsAccessed = useCase.useFlag(result);

		return result;
	}

	public final boolean valueAccessedBy(UseCase useCase) {
		return getObject().value().isUsedBy(useCase);
	}

	@Override
	public String toString() {
		if (this.object == null) {
			return super.toString();
		}
		return "ObjectAnalysis[" + this.object + ']';
	}

	private boolean determineFieldsAccessedBy(UseCase useCase) {
		for (Member member : getObject().getMembers()) {

			final Field<?> field = member.toField(dummyUser());

			if (field == null) {
				continue;
			}
			if (member.getAnalysis().accessedBy(useCase)) {
				return true;
			}
		}

		return false;
	}
}

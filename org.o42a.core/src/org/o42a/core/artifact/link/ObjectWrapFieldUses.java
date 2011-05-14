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
package org.o42a.core.artifact.link;

import org.o42a.util.use.UseCase;
import org.o42a.util.use.UseInfo;


final class ObjectWrapFieldUses implements UseInfo {

	private final ObjectWrap objectWrap;
	private UseInfo[] uses;

	ObjectWrapFieldUses(ObjectWrap objectWrap) {
		this.objectWrap = objectWrap;
	}

	@Override
	public boolean isUsedBy(UseCase useCase) {
		for (UseInfo use : getUses()) {
			if (use.isUsedBy(useCase)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		if (this.objectWrap == null) {
			return super.toString();
		}
		return "FieldUses[" + this.objectWrap + ']';
	}

	private final UseInfo[] getUses() {
		if (this.uses != null) {
			return this.uses;
		}
		this.uses = new UseInfo[] {
			this.objectWrap.getWrapped().fieldUses(),
			this.objectWrap.superFieldUses(),
		};
		return this.uses;
	}

}

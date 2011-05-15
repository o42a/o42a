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

import org.o42a.util.use.*;


final class ObjectWrapFieldUses extends UseTracker implements UseInfo {

	private final ObjectWrap objectWrap;

	ObjectWrapFieldUses(ObjectWrap objectWrap) {
		this.objectWrap = objectWrap;
	}

	@Override
	public UseFlag getUseBy(UseCase useCase) {
		if (!start(useCase)) {
			return getUseFlag();
		}
		if (useBy(this.objectWrap.getWrapped().fieldUses())) {
			return getUseFlag();
		}
		if (useBy(this.objectWrap.ownFieldUses())) {
			return getUseFlag();
		}
		return done();
	}

	@Override
	public String toString() {
		if (this.objectWrap == null) {
			return super.toString();
		}
		return "FieldUses[" + this.objectWrap + ']';
	}

}

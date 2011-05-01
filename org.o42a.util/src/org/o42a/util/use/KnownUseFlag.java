/*
    Utilities
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
package org.o42a.util.use;


final class KnownUseFlag extends UseFlag {

	private final boolean used;

	KnownUseFlag(UseCase useCase, boolean used) {
		super(useCase);
		this.used = used;
	}

	@Override
	public boolean isUsed() {
		return this.used;
	}

	@Override
	public String toString() {
		if (this.used) {
			return "UsedBy[" + getUseCase() + ']';
		}
		return "UnusedBy[" + getUseCase() + ']';
	}

	@Override
	UnknownUseFlag toUnknown() {
		return null;
	}

}

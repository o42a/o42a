/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.ref;

import org.o42a.util.fn.Cancelable;


public abstract class Normal {

	private Normal next;

	public Normal(Normalizer normalizer) {
		if (isCancelable()) {
			normalizer.addNormal(this);
		}
	}

	public final boolean isCancelable() {
		return cancelable() != null;
	}

	protected abstract Cancelable cancelable();

	final Normal getNext() {
		return this.next;
	}

	final void setNext(Normal next) {
		this.next = next;
	}

}

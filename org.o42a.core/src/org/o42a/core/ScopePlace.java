/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core;

import org.o42a.core.member.local.LocalScope;
import org.o42a.util.Place;
import org.o42a.util.Place.Trace;


public class ScopePlace {

	public static ScopePlace scopePlace(Scope appearedIn) {
		return new ScopePlace(appearedIn);
	}

	public static LocalPlace localPlace(LocalScope appearedIn, Place place) {
		assert appearedIn != null :
			"The scope this place belongs to not specified";
		assert place != null :
			"Place not specified";
		return new LocalPlace(appearedIn, place);
	}

	public static final ScopePlace TOP_PLACE = new ScopePlace();

	private final Scope appearedIn;
	private final Place place;

	ScopePlace(Scope appearedIn, Place place) {
		this.appearedIn = appearedIn;
		this.place = place;
	}

	private ScopePlace(Scope appearedIn) {
		this(appearedIn, null);
	}

	private ScopePlace() {
		this.appearedIn = null;
		this.place = null;
	}

	public Scope getAppearedIn() {
		return this.appearedIn;
	}

	public final Place getPlace() {
		return this.place;
	}

	public final boolean isDeclarative() {
		return this.place == null;
	}

	public final boolean isImperative() {
		return this.place != null;
	}

	public Trace nestedTrace() {
		if (this.place != null) {
			return this.place.nestedTrace();
		}
		return Place.newTrace();
	}

	public LocalPlace toLocal() {
		return null;
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;

		result = prime * result + this.appearedIn.hashCode();
		result =
				prime * result
				+ ((this.place == null) ? 0 : this.place.hashCode());

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		final ScopePlace other = (ScopePlace) obj;

		if (this.appearedIn != other.appearedIn) {
			return false;
		}
		if (this.place == null) {
			if (other.place != null) {
				return false;
			}
		} else if (!this.place.equals(other.place)) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		if (this.place == null) {
			return this.appearedIn.toString();
		}
		return this.appearedIn.toString() + this.place.toString();
	}

}

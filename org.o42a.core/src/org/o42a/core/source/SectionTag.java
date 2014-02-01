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
package org.o42a.core.source;

import org.o42a.util.string.Name;


public final class SectionTag {

	public static final SectionTag IMPLICIT_SECTION_TAG =
			new SectionTag(null, null);

	private final SectionTag parent;
	private final Name tag;

	private SectionTag(SectionTag parent, Name tag) {
		this.parent = parent;
		this.tag = tag;
	}

	public final boolean isImplicit() {
		return this.tag == null;
	}

	public final SectionTag getParent() {
		return this.parent;
	}

	public final Name getTag() {
		return this.tag;
	}

	public final SectionTag append(Name tag) {
		assert tag != null :
			"Section tag not specified";
		return new SectionTag(this, tag);
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;

		result =
				prime * result
				+ ((this.parent == null) ? 0 : this.parent.hashCode());
		result =
				prime * result
				+ ((this.tag == null) ? 0 : this.tag.hashCode());

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

		final SectionTag other = (SectionTag) obj;

		if (this.parent == null) {
			if (other.parent != null) {
				return false;
			}
		} else if (!this.parent.equals(other.parent)) {
			return false;
		}
		if (this.tag == null) {
			if (other.tag != null) {
				return false;
			}
		} else if (!this.tag.is(other.tag)) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		if (isImplicit()) {
			return "<default section>";
		}

		final StringBuilder out = new StringBuilder();

		toString(out);

		return out.toString();
	}

	private void toString(StringBuilder out) {
		if (!this.parent.isImplicit()) {
			this.parent.toString(out);
			out.append(':');
		}
		out.append(this.tag);
	}

}

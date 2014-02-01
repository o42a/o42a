/*
    Utilities
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
package org.o42a.util.io;

import java.util.Formatter;

import org.o42a.util.log.LogLocation;


public final class SourceRange extends LogLocation {

	private final SourcePosition start;
	private final SourcePosition end;

	public SourceRange(SourcePosition start, SourcePosition end) {
		this.start = start;
		this.end = end;
	}

	@Override
	public final Source getSource() {
		return getStart().getSource();
	}

	@Override
	public final SourcePosition getPosition() {
		return getStart().getPosition();
	}

	@Override
	public final SourceRange getRange() {
		return this;
	}

	public final SourcePosition getStart() {
		return this.start;
	}

	public final SourcePosition getEnd() {
		return this.end;
	}

	@Override
	public void formatTo(
			Formatter formatter,
			int flags,
			int width,
			int precision) {
		formatter.format("%s..", this.start);
		this.end.formatTo(
				formatter,
				!this.start.getSource().equals(this.end.getSource()));
	}

	@Override
	public void print(StringBuilder out) {
		this.start.print(out, true);
		out.append("..");
		this.end.print(out, !this.start.getSource().equals(this.end.getSource()));
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;

		result = prime * result + this.start.hashCode();
		result = prime * result + this.end.hashCode();

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

		final SourceRange other = (SourceRange) obj;

		if (!this.start.equals(other.start)) {
			return false;
		}
		if (!this.end.equals(other.end)) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append('[');
		print(out);
		out.append(']');

		return out.toString();
	}

}

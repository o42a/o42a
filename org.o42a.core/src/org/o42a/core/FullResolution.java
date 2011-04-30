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
package org.o42a.core;


public final class FullResolution {

	private int started;
	private int finished;

	FullResolution() {
	}

	private final boolean isStarted() {
		return this.started != 0;
	}

	public final boolean isComplete() {
		return this.started != 0 && this.started == this.finished;
	}

	public final void start() {
		++this.started;
	}

	public final void end() {
		++this.finished;
	}

	public final boolean assertIncomplete() {
		assert this.started == 0 || this.started != this.finished :
			"Full resolution is already complete";
		return true;
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append("FullResolution[");
		if (!isStarted()) {
			out.append("NOT STARTED");
		} else if (isComplete()) {
			out.append("COMPLETE");
		} else {
			out.append("STARTED");
		}
		out.append(']');

		return out.toString();
	}

}

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


public final class FullResolution {

	private int started;
	private boolean initiated;

	private final boolean isInitiated() {
		return this.initiated;
	}

	public final boolean isComplete() {
		return isInitiated() && this.started == 0;
	}

	public final void initiate() {
		this.initiated = true;
		++this.started;
	}

	public final void start() {
		assert assertIncomplete();
		if (this.initiated) {
			++this.started;
		}
	}

	public final void end() {
		if (this.initiated) {
			--this.started;
		}
	}

	public final void reset() {
		this.started = 0;
	}

	public final boolean assertComplete() {
		assert isInitiated():
			"Full resolution is not initiated yet";
		assert isComplete():
			"Full resolution is not complete yet";
		return true;
	}

	public final boolean assertIncomplete() {
		assert !isInitiated() || !isComplete():
			"Full resolution is already complete";
		return true;
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append("FullResolution[");
		if (!isInitiated()) {
			out.append("NOT INITIATED");
		} else if (isComplete()) {
			out.append("COMPLETE");
		} else {
			out.append("IN PROGRESS");
		}
		out.append(']');

		return out.toString();
	}

}

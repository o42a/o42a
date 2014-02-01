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
package org.o42a.core.ref.impl.normalizer;

import org.o42a.core.ref.path.NormalAppender;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.Step;


public class SubPathNormalStep extends NormalAppender {

	private final Path path;
	private final int start;
	private final int end;

	public SubPathNormalStep(Path path, int start, int end) {
		this.path = path;
		this.start = start;
		this.end = end;
	}

	@Override
	public Path appendTo(Path path) {

		Path result = path;
		final Step[] steps = this.path.getSteps();

		for (int i = this.start; i < this.end; ++i) {
			result = result.append(steps[i]);
		}

		return result;
	}

	@Override
	public void ignore() {
	}

	@Override
	public void cancel() {
	}

	@Override
	public String toString() {
		if (this.path == null) {
			return super.toString();
		}

		final Step[] steps = this.path.getSteps();

		if (steps.length <= this.start) {
			return "<...>";
		}

		final StringBuilder out = new StringBuilder();
		out.append("<...");
		out.append(steps[this.start]);
		for (int i = this.start + 1; i < steps.length; ++i) {
			out.append('/').append(steps[i]);
		}
		out.append('>');

		return out.toString();
	}

}

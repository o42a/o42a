/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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


public final class NormalPathStep extends NormalAppender {

	private final Path path;

	public NormalPathStep(Path path) {
		this.path = path;
	}

	@Override
	public Path appendTo(Path path) {

		Path result = path;

		for (Step step : this.path.getSteps()) {
			result = result.append(step);
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
		return this.path.toString();
	}

}

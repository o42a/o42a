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

import java.util.List;

import org.o42a.core.Scope;
import org.o42a.core.ref.path.*;


public class UnchangedNormalPath implements NormalPath {

	private final BoundPath path;

	public UnchangedNormalPath(BoundPath path) {
		this.path = path;
	}

	@Override
	public boolean isNormalized() {
		return true;
	}

	@Override
	public Scope getOrigin() {
		return this.path.getOrigin();
	}

	@Override
	public BoundPath toPath() {
		return this.path;
	}

	@Override
	public void cancel() {
	}

	@Override
	public void appendTo(List<NormalStep> normalSteps) {
		normalSteps.add(new UnhchangedStep());
	}

	@Override
	public String toString() {
		if (this.path == null) {
			return super.toString();
		}
		return "UnchanhgedNormalPath" + this.path;
	}

	private final class UnhchangedStep implements NormalStep {

		@Override
		public Path appendTo(Path path) {

			Path result = path;

			for (Step step : toPath().getSteps()) {
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

			final BoundPath path = toPath();

			if (path == null) {
				return super.toString();
			}

			return path.toString();
		}

	}

}

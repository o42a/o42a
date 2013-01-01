/*
    Compiler Core
    Copyright (C) 2011-2013 Ruslan Lopatin

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
package org.o42a.core.ir.op;

import org.o42a.core.ref.path.Step;


public abstract class StepOp<S extends Step> extends PathOp {

	private final S step;

	public StepOp(PathOp start, S step) {
		super(start);
		this.step = step;
	}

	public final S getStep() {
		return this.step;
	}

	public final PathOp start() {
		return (PathOp) host();
	}

	@Override
	public String toString() {
		if (this.step == null) {
			return super.toString();
		}
		return this.step.toString();
	}

}

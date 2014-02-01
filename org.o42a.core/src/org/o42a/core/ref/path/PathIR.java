/*
    Compiler Core
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.core.ref.path;

import org.o42a.core.ir.op.HostOp;
import org.o42a.core.ir.op.RefIR;
import org.o42a.core.ir.op.RefTargetIR;


public abstract class PathIR {

	protected static RefTargetIR stepTargetIR(RefIR refIR, Step step) {
		return step.targetIR(refIR);
	}

	protected static HostOp pathOp(BoundPath path, HostOp start, int last) {

		final Step[] steps = path.getSteps();
		HostOp found = start;

		for (int i = 0; i < last; ++i) {
			found = steps[i].op(found);
			if (found == null) {
				throw new IllegalStateException(
						path.toString(i + 1) + " not found");
			}
		}

		return found;
	}

}

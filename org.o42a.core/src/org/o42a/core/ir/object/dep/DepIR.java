/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.core.ir.object.dep;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.RefIR;
import org.o42a.core.object.state.Dep;
import org.o42a.util.string.ID;


public class DepIR {

	private final ObjectIR objectIR;
	private final Dep dep;
	private final RefIR refIR;
	private final int index;
	private boolean omitted;

	public DepIR(ObjectIR objectIR, Dep dep, int index) {
		assert dep.exists(objectIR.getGenerator().getAnalyzer()) :
			dep + " does not exist";
		this.objectIR = objectIR;
		this.dep = dep;
		this.index = index;
		this.refIR = dep.ref().ir(getGenerator());
	}

	public final Generator getGenerator() {
		return getObjectIR().getGenerator();
	}

	public final Dep getDep() {
		return this.dep;
	}

	public final ID getId() {
		return getDep().toID();
	}

	public final boolean isOmitted() {
		return this.omitted;
	}

	public final ObjectIR getObjectIR() {
		return this.objectIR;
	}

	public final int getIndex() {
		return this.index;
	}

	public final DepOp op(Code code, ObjectOp host) {
		return new DepOp(code, host, this);
	}

	@Override
	public String toString() {
		if (this.dep == null) {
			return super.toString();
		}
		return this.dep.toString();
	}

	final RefIR refIR() {
		return this.refIR;
	}

}

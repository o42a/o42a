/*
    Modules Commons
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.common.ir;

import org.o42a.core.def.Definitions;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.op.ValOp;


public abstract class ProposedValueIR extends ObjectValueIR {

	public ProposedValueIR(ObjectIR objectIR) {
		super(objectIR);
	}

	@Override
	protected ValOp writeProposition(ValDirs dirs, ObjOp host, ObjectOp body) {
		return proposition(dirs, body != null ? body : host);
	}

	@Override
	protected ValOp buildProposition(
			ValDirs dirs,
			ObjOp host,
			Definitions definitions) {
		return proposition(dirs, host);
	}

	protected abstract ValOp proposition(ValDirs dirs, ObjectOp host);

}

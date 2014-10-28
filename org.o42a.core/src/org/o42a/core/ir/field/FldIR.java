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
package org.o42a.core.ir.field;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Ptr;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.data.Type;
import org.o42a.core.ir.field.local.LocalIR;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectIRBodies;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.ir.object.vmt.VmtRecord;
import org.o42a.core.object.Obj;
import org.o42a.util.string.ID;


public interface FldIR<F extends StructOp<F>, T extends Type<F>> {

	ID getId();

	FldKind getKind();

	Obj getDeclaredIn();

	default boolean isStateless() {
		return getKind().isStateless();
	}

	default Generator getGenerator() {
		return getBodyIR().getGenerator();
	}

	default ObjectIR getObjectIR() {
		return getBodyIR().getObjectIR();
	}

	ObjectIRBody getBodyIR();

	T getInstance();

	default T getTypeInstance() {
		if (getBodyIR().bodies().isTypeBodies()) {
			return getInstance();
		}
		return get(getObjectIR().typeBodies()).getInstance();
	}

	default Ptr<F> pointer(Generator generator) {
		return getInstance().pointer(generator);
	}

	default VmtRecord vmtRecord() {
		return null;
	}

	void allocate(SubData<?> data);

	FldIR<F, T> get(ObjectIRBodies bodies);

	default FldIR<F, T> get(ObjectIR objectIR) {

		final ObjectIRBodies bodies;

		if (!getBodyIR().bodies().isTypeBodies()) {
			bodies = objectIR.bodies();
		} else {
			bodies = objectIR.typeBodies();
		}

		return get(bodies);
	}

	Fld<?, ?> toFld();

	LocalIR toLocal();

}

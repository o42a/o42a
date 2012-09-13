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
package org.o42a.core.object.link.impl;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.struct.ValueIR;
import org.o42a.core.ir.value.struct.ValueOp;


final class LinkIR extends ValueIR {

	LinkIR(LinkValueStructIR valueStructIR, ObjectIR objectIR) {
		super(valueStructIR, objectIR);
	}

	@Override
	public void allocateBody(ObjectIRBodyData data) {
	}

	@Override
	public void allocateMethods(ObjectIRMethods methodsIR, SubData<?> data) {
	}

	@Override
	public ValueOp op(ObjectOp object) {
		return new LinkOp(this, object);
	}

	private static final class LinkOp extends ValueOp {

		LinkOp(LinkIR valueIR, ObjectOp object) {
			super(valueIR, object);
		}

		@Override
		public void init(Block code, ValOp value) {
			// Link value is never initialized.
		}

		@Override
		public void initToFalse(Block code) {
			// Link value is never initialized.
		}

		@Override
		public void assign(CodeDirs dirs, ObjectOp value) {
			throw new UnsupportedOperationException("Can't assign to link");
		}

		@Override
		protected ValOp write(ValDirs dirs) {
			return defaultWrite(dirs);
		}

	}

}

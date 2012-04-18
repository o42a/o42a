/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.path.InlineStep;


public final class InlineValueStep extends InlineStep {

	private final InlineValue value;

	public InlineValueStep(InlineValue value) {
		this.value = value;
	}

	@Override
	public void ignore() {
	}

	@Override
	public void after(InlineStep preceding) {
		assert preceding == null :
			"In-line step (" + this
			+ ") can not follow another one (" + preceding + ")";
	}

	@Override
	public void writeLogicalValue(CodeDirs dirs, HostOp host) {
		this.value.writeCond(dirs, host);
	}

	@Override
	public ValOp writeValue(ValDirs dirs, HostOp host) {
		return this.value.writeValue(dirs, host);
	}

	@Override
	public void cancel() {
	}

	@Override
	public String toString() {
		if (this.value == null) {
			return super.toString();
		}
		return this.value.toString();
	}

}

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
package org.o42a.core.ref.impl.cond;

import org.o42a.core.ir.cmd.Cmd;
import org.o42a.core.ir.cmd.Control;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.sentence.Local;


final class RefConditionCmd implements Cmd {

	private final RefCondition statement;

	RefConditionCmd(RefCondition statement) {
		this.statement = statement;
	}

	@Override
	public void write(Control control) {

		final RefOp op = ref().op(control.host());
		final Local local = this.statement.getLocal();

		if (local == null) {
			writeCond(control, op);
		} else {
			storeLocal(control.end(), op, local);
		}
	}

	@Override
	public String toString() {
		if (this.statement == null) {
			return super.toString();
		}
		return this.statement.toString();
	}

	private final Ref ref() {
		return this.statement.ref();
	}

	private void writeCond(Control control, RefOp op) {
		op.writeCond(control.dirs());
	}

	private void storeLocal(Control control, RefOp op, Local local) {
		control.locals().set(control.dirs(), local, op);
	}

}

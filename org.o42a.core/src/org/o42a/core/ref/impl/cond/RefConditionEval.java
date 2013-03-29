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
package org.o42a.core.ref.impl.cond;

import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.sentence.Local;


final class RefConditionEval implements Eval {

	private final RefCondition refCondition;

	RefConditionEval(RefCondition refCondition) {
		this.refCondition = refCondition;
	}

	@Override
	public void write(DefDirs dirs, HostOp host) {

		final RefOp op = ref().op(host);
		final Local local = this.refCondition.getLocal();

		if (local == null) {
			op.writeCond(dirs.dirs());
		} else {
			dirs.dirs().locals().set(dirs.dirs(), local, op);
		}
	}

	@Override
	public String toString() {
		if (this.refCondition == null) {
			return super.toString();
		}
		return this.refCondition.toString();
	}

	private final Ref ref() {
		return this.refCondition.getRef();
	}

}

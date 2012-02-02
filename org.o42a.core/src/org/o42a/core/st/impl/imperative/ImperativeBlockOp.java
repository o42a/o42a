/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.core.st.impl.imperative;

import static org.o42a.core.st.impl.imperative.ImperativeOp.writeSentences;

import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.local.Control;
import org.o42a.core.ir.local.StOp;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.st.sentence.ImperativeBlock;


public final class ImperativeBlockOp extends StOp {

	public ImperativeBlockOp(CodeBuilder builder, ImperativeBlock block) {
		super(builder, block);
	}

	@Override
	public void writeValue(Control control, ValOp result) {
		writeSentences(control, result, getBlock(), null);
	}

	@Override
	public void writeLogicalValue(Control control) {
		writeSentences(control, null, getBlock(), null);
	}

	private final ImperativeBlock getBlock() {
		return (ImperativeBlock) getStatement();
	}

}

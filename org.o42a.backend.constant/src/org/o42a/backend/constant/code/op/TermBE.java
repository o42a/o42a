/*
    Constant Handler Compiler Back-end
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
package org.o42a.backend.constant.code.op;

import org.o42a.backend.constant.code.CBlock;
import org.o42a.backend.constant.code.CCodePart;
import org.o42a.codegen.code.Block;


public abstract class TermBE extends AbstractBE {

	private final CCodePart<Block> part;

	public TermBE(CBlock<?> code) {
		this.part = code.term(this);
	}

	public TermBE(CCodePart<Block> part) {
		this.part = part;
		part.add(this);
	}

	public final CCodePart<Block> part() {
		return this.part;
	}

}

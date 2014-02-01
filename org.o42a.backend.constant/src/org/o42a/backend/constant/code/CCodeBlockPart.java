/*
    Constant Handler Compiler Back-end
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
package org.o42a.backend.constant.code;

import org.o42a.codegen.code.Block;
import org.o42a.util.string.ID;


final class CCodeBlockPart extends CBlockPart {

	CCodeBlockPart(CCodeBlock block) {
		super(block);
	}

	private CCodeBlockPart(CCodeBlock block, int index) {
		super(
				block,
				index != 0 ? block.getId().anonymous(index) : block.getId(),
				index);
	}

	@Override
	protected CBlockPart newNextPart(int index) {
		return new CCodeBlockPart(codeBlock(), index);
	}

	@Override
	protected Block createUnderlying() {

		final CCodeBlock block = codeBlock();
		final ID localId = block.getId().getLocal();
		final ID partName;

		if (index() == 0) {
			partName = localId;
		} else {
			partName = localId.anonymous(index());
		}

		return block.getEnclosing().firstPart().underlying().addBlock(partName);
	}

	private final CCodeBlock codeBlock() {
		return (CCodeBlock) code();
	}

}

/*
    Compiler Commons
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
package org.o42a.common.macro.st;

import org.o42a.core.ref.Ref;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.BlockBuilder;


final class ExpandMacroBlock extends BlockBuilder {

	private final Ref expansion;

	ExpandMacroBlock(Ref expansion) {
		super(expansion);
		this.expansion = expansion;
	}

	@Override
	public void buildBlock(Block block) {

		final ExpandMacroStatement statement =
				new ExpandMacroStatement(
						this.expansion.rescope(block.getScope()));

		block.declare(this).alternative(this).statement(statement);
	}

	@Override
	public String toString() {
		if (this.expansion == null) {
			return super.toString();
		}
		return '=' + this.expansion.toString();
	}

}

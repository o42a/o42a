/*
    Compiler
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.compiler.ip.file;

import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.BlockBuilder;


class SectionDefinition extends BlockBuilder {

	private final Section section;

	SectionDefinition(Section section) {
		super(section.getLocation());
		this.section = section;
	}

	@Override
	public void buildBlock(Block<?> block) {
		this.section.define(block.toDeclarativeBlock());
	}

}

/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core.artifact;

import static org.o42a.core.st.InstructionKind.REMOVE_INSTRUCTION;

import org.o42a.core.ref.Ref;
import org.o42a.core.st.InstructionKind;
import org.o42a.core.st.sentence.Block;


final class SkipDirective implements Directive {

	@Override
	public InstructionKind getInstructionKind() {
		return REMOVE_INSTRUCTION;
	}

	@Override
	public void apply(Ref directive) {
	}

	@Override
	public void apply(Block<?> block, Ref directive) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return "SKIP";
	}

}

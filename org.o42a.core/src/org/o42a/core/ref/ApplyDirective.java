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
package org.o42a.core.ref;

import org.o42a.core.artifact.Directive;
import org.o42a.core.st.Instruction;
import org.o42a.core.st.InstructionKind;
import org.o42a.core.st.sentence.Block;


final class ApplyDirective implements Instruction {

	private final Ref ref;
	private final Directive directive;

	ApplyDirective(Ref ref, Directive directive) {
		this.ref = ref;
		this.directive = directive;
	}

	@Override
	public InstructionKind getInstructionKind() {
		return this.directive.getInstructionKind();
	}

	@Override
	public void execute() {

		final RefConditionsWrap conditions = this.ref.getConditions();

		conditions.removeWrapped();

		this.directive.apply(this.ref);
	}

	@Override
	public void execute(Block<?> block) {

		final RefConditionsWrap conditions = this.ref.getConditions();

		conditions.setWrapped(
				block.setConditions(conditions.getInitialConditions()));

		this.directive.apply(block, this.ref);
	}

	@Override
	public String toString() {
		return "ApplyDirective[" + this.directive + ']';
	}

}

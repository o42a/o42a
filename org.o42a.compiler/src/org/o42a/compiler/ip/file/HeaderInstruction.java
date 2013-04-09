/*
    Compiler
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
package org.o42a.compiler.ip.file;

import static org.o42a.core.member.MemberRegistry.skipDeclarations;

import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.st.Instruction;
import org.o42a.core.st.InstructionContext;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.value.directive.Directive;


final class HeaderInstruction implements Instruction, InstructionContext {

	private final Ref ref;
	private final Directive directive;
	private InstructionContext context;

	HeaderInstruction(Ref ref, Directive instruction) {
		this.ref = ref;
		this.directive = instruction;
	}

	@Override
	public void execute(InstructionContext context) {
		this.context = context;
		this.directive.apply(this.ref, this);
	}

	@Override
	public Resolver getResolver() {
		return this.context.getResolver();
	}

	@Override
	public Block<?> getBlock() {
		prohibited();
		return new DeclarativeBlock(
				this.ref,
				this.ref.distribute(),
				skipDeclarations());
	}

	@Override
	public void doNotRemove() {
		prohibited();
	}

	@Override
	public String toString() {
		if (this.ref == null) {
			return super.toString();
		}
		return this.ref.toString();
	}

	private void prohibited() {
		this.ref.getLogger().error(
				"prohibited_header_directive",
				this.ref,
				"This directive directive is not allowed in file header");
	}

}

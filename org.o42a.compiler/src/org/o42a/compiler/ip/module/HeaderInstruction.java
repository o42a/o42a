/*
    Compiler
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.compiler.ip.module;

import static org.o42a.core.member.MemberRegistry.skipDeclarations;

import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.st.Instruction;
import org.o42a.core.st.InstructionContext;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.util.use.UseCase;
import org.o42a.util.use.UseFlag;
import org.o42a.util.use.User;


final class HeaderInstruction implements Instruction, InstructionContext {

	private final Ref ref;
	private final Instruction instruction;
	private InstructionContext context;

	HeaderInstruction(Ref ref, Instruction instruction) {
		this.ref = ref;
		this.instruction = instruction;
	}

	@Override
	public void execute(InstructionContext context) {
		this.context = context;
		this.instruction.execute(this);
	}

	@Override
	public User toUser() {
		return this.context.toUser();
	}

	@Override
	public UseFlag getUseBy(UseCase useCase) {
		return this.context.getUseBy(useCase);
	}

	@Override
	public boolean isUsedBy(UseCase useCase) {
		return getUseBy(useCase).isUsed();
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
				"This directive directive is not allowed in module header");
	}

}

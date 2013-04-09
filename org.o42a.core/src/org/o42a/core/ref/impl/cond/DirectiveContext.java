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

import org.o42a.core.ref.Resolver;
import org.o42a.core.st.InstructionContext;
import org.o42a.core.st.sentence.Block;


final class DirectiveContext implements InstructionContext {

	private final ApplyDirective applyDirective;
	private final InstructionContext context;
	private Block<?> block;

	DirectiveContext(
			ApplyDirective applyDirective,
			InstructionContext context) {
		this.applyDirective = applyDirective;
		this.context = context;
	}

	@Override
	public final Resolver getResolver() {
		return this.applyDirective.getResolver();
	}

	@Override
	public Block<?> getBlock() {
		if (this.block != null) {
			return this.block;
		}
		return this.block = this.context.getBlock();
	}

	@Override
	public void doNotRemove() {
		this.context.doNotRemove();
	}

	@Override
	public String toString() {
		if (this.context == null) {
			return super.toString();
		}
		return this.context.toString();
	}

	void apply() {
		this.applyDirective.getDirective().apply(
				this.applyDirective.getRef(),
				this);
	}

}

/*
    Compiler Core
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
package org.o42a.core.st.impl.cond;

import org.o42a.core.ref.Resolver;
import org.o42a.core.st.InstructionContext;
import org.o42a.core.st.sentence.Block;
import org.o42a.util.use.*;


final class DirectiveContext implements InstructionContext {

	private final ApplyDirective applyDirective;
	private final InstructionContext context;
	private Block<?> block;
	private boolean doNotRemove;

	DirectiveContext(
			ApplyDirective applyDirective,
			InstructionContext context) {
		this.applyDirective = applyDirective;
		this.context = context;
	}

	@Override
	public final User toUser() {
		return getResolver().toUser();
	}

	@Override
	public final UseFlag getUseBy(UseCaseInfo useCase) {
		return toUser().getUseBy(useCase);
	}

	@Override
	public boolean isUsedBy(UseCaseInfo useCase) {
		return getUseBy(useCase).isUsed();
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

		final RefEnvWrap env = this.applyDirective.getEnv();

		this.block = this.context.getBlock();
		this.doNotRemove = true;
		if (env != null) {
			// May be null inside imperative block.
			env.setWrapped(this.block.setEnv(env.getInitialEnv()));
		}

		return this.block;
	}

	@Override
	public void doNotRemove() {
		this.doNotRemove = true;
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
		if (!this.doNotRemove) {
			this.applyDirective.getEnv().removeWrapped();
		}
	}

}

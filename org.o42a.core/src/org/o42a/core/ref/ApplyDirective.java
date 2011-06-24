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

import org.o42a.core.st.Instruction;
import org.o42a.core.st.InstructionContext;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.value.Directive;
import org.o42a.util.use.User;


final class ApplyDirective implements Instruction {

	private final Ref ref;
	private final Directive directive;
	private final Resolver resolver;

	ApplyDirective(Ref ref, Resolver resolver, Directive directive) {
		this.ref = ref;
		this.resolver = resolver;
		this.directive = directive;
	}

	@Override
	public void execute(InstructionContext context) {
		new DirectiveContext(context).apply();
	}

	@Override
	public String toString() {
		return "ApplyDirective[" + this.directive + ']';
	}

	private final class DirectiveContext implements InstructionContext {

		private final InstructionContext context;
		private Block<?> block;
		private boolean doNotRemove;

		DirectiveContext(InstructionContext context) {
			this.context = context;
		}

		@Override
		public final User toUser() {
			return getResolver().toUser();
		}

		@Override
		public final Resolver getResolver() {
			return ApplyDirective.this.resolver;
		}

		@Override
		public Block<?> getBlock() {
			if (this.block != null) {
				return this.block;
			}

			final RefEnvWrap env = ApplyDirective.this.ref.getEnv();

			this.block = this.context.getBlock();
			this.doNotRemove = true;
			env.setWrapped(this.block.setEnv(env.getInitialEnv()));

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
			ApplyDirective.this.directive.apply(ApplyDirective.this.ref, this);
			if (!this.doNotRemove) {
				ApplyDirective.this.ref.getEnv().removeWrapped();
			}
		}

	}

}

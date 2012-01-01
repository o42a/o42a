/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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

import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.st.Instruction;
import org.o42a.core.st.InstructionContext;
import org.o42a.core.value.Directive;


final class ApplyDirective implements Instruction {

	private final RefConditionDefiner definer;
	private final Directive directive;
	private final Resolver resolver;

	ApplyDirective(
			RefConditionDefiner definer,
			Resolver resolver,
			Directive directive) {
		this.definer = definer;
		this.resolver = resolver;
		this.directive = directive;
	}

	@Override
	public void execute(InstructionContext context) {
		new DirectiveContext(this, context).apply();
	}

	@Override
	public String toString() {
		return "ApplyDirective[" + this.directive + ']';
	}

	final Ref getRef() {
		return this.definer.getRef();
	}

	final Directive getDirective() {
		return this.directive;
	}

	final Resolver getResolver() {
		return this.resolver;
	}

}

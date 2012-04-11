/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.core.st.impl.declarative;

import static org.o42a.core.st.DefinitionTargets.noDefinitions;

import org.o42a.core.Scope;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.ref.Resolver;
import org.o42a.core.st.*;
import org.o42a.core.st.sentence.DeclarativeBlock;


abstract class InclusionDefiner<I extends Inclusion>
		extends AbstractDefiner
		implements Instruction {

	private Definer replacement;

	InclusionDefiner(I inclusion, DefinerEnv env) {
		super(inclusion, env);
	}

	@SuppressWarnings("unchecked")
	public final I getInclusion() {
		return (I) getStatement();
	}

	public final Definer getReplacement() {
		return this.replacement;
	}

	@Override
	public DefinerEnv nextEnv() {
		return new InclusionEnv(this);
	}

	@Override
	public final void execute(InstructionContext context) {

		final DeclarativeBlock block = context.getBlock().toDeclarativeBlock();

		includeInto(block);
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {
		return this;
	}

	@Override
	public DefinitionTargets getDefinitionTargets() {
		return noDefinitions();
	}

	@Override
	public Definitions define(Scope scope) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Definer replaceWith(Statement statement) {
		return this.replacement = statement.define(env());
	}

	protected abstract void includeInto(DeclarativeBlock block);

}

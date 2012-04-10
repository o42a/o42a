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
package org.o42a.core.st.impl;

import static org.o42a.core.st.DefinitionTargets.noDefinitions;

import org.o42a.core.Scope;
import org.o42a.core.ref.Resolver;
import org.o42a.core.st.*;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.Sentence;
import org.o42a.core.st.sentence.Statements;
import org.o42a.core.value.ValueStruct;


public abstract class BlockImplication<
		B extends Block<?, ?>,
		L extends Implication<L>>
				extends AbstractImplication<L> {

	private DefinitionTargets definitionTargets;

	public BlockImplication(B block) {
		super(block);
	}

	@SuppressWarnings("unchecked")
	public final B getBlock() {
		return (B) getStatement();
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {
		return new ExecuteInstructions();
	}

	@Override
	public DefinitionTargets getDefinitionTargets() {
		if (this.definitionTargets != null) {
			return this.definitionTargets;
		}
		getBlock().executeInstructions();

		DefinitionTargets result = noDefinitions();

		for (Sentence<?, ?> sentence : getBlock().getSentences()) {
			result = result.add(sentence.getDefinitionTargets());
		}

		return this.definitionTargets = result;
	}

	protected ValueStruct<?, ?> sentencesValueStruct(Scope scope) {
		if (!getDefinitionTargets().haveValue()) {
			return null;
		}

		ValueStruct<?, ?> result = null;

		for (Sentence<?, ?> sentence : getBlock().getSentences()) {

			final ValueStruct<?, ?> struct = valueStruct(sentence, scope);

			if (struct == null) {
				continue;
			}
			if (result == null) {
				result = struct;
				continue;
			}
			if (result.assignableFrom(struct)) {
				continue;
			}
			if (struct.assertAssignableFrom(result)) {
				result = struct;
				continue;
			}

			getLogger().incompatible(sentence, result);
		}

		return result;
	}

	private ValueStruct<?, ?> valueStruct(
			Sentence<?, ?> sentence,
			Scope scope) {

		ValueStruct<?, ?> result = null;

		for (Statements<?, ?> alt : sentence.getAlternatives()) {

			final ValueStruct<?, ?> struct = valueStruct(alt, scope);

			if (struct == null) {
				continue;
			}
			if (result == null) {
				result = struct;
				continue;
			}
			if (result.assignableFrom(struct)) {
				continue;
			}
			if (struct.assignableFrom(result)) {
				result = struct;
				continue;
			}

			getLogger().incompatible(alt, result);
		}

		return result;
	}

	ValueStruct<?, ?> valueStruct(Statements<?, ?> alt, Scope scope) {

		ValueStruct<?, ?> result = null;

		for (Implication<?> implication : alt.getImplications()) {
			if (!implication.getDefinitionTargets().haveValue()) {
				continue;
			}

			final ValueStruct<?, ?> struct = implication.valueStruct(scope);

			if (struct == null) {
				continue;
			}
			if (result == null) {
				result = struct;
				continue;
			}
			if (result.assignableFrom(struct)) {
				continue;
			}
			if (struct.assignableFrom(result)) {
				result = struct;
				continue;
			}

			getLogger().incompatible(implication, result);
		}

		return result;
	}

	private final class ExecuteInstructions implements Instruction {

		@Override
		public void execute(InstructionContext context) {
			context.doNotRemove();
			getBlock().executeInstructions();
		}

		@Override
		public String toString() {
			return "ExecuteInstructions[" + getBlock() + ']';
		}

	}

}

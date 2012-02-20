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

import org.o42a.core.Scope;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.st.StatementEnv;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.impl.BlockDefiner;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.st.sentence.DeclarativeSentence;
import org.o42a.core.value.ValueStruct;


public class DeclarativeBlockDefiner extends BlockDefiner<DeclarativeBlock> {

	public DeclarativeBlockDefiner(DeclarativeBlock block, StatementEnv env) {
		super(block, env);
	}

	@Override
	public StatementEnv nextEnv() {
		return new DeclarativeBlockEnv(this);
	}

	@Override
	public ValueStruct<?, ?> valueStruct(Scope scope) {
		return sentencesValueStruct(scope);
	}

	@Override
	public Definitions define(Scope scope) {
		if (!getDefinitionTargets().haveDefinition()) {
			return null;
		}

		Definitions result = null;

		for (DeclarativeSentence sentence : getBlock().getSentences()) {

			final Definitions definitions = sentence.define(scope);

			if (definitions == null) {
				continue;
			}
			if (result == null) {
				result = definitions;
			} else {
				result = result.refine(definitions);
			}
		}

		assert result != null :
			"Missing definitions: " + this;

		return result;
	}

	@Override
	public Action initialValue(LocalResolver resolver) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Action initialLogicalValue(LocalResolver resolver) {
		throw new UnsupportedOperationException();
	}

}

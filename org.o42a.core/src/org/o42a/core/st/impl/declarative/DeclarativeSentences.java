/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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

import static org.o42a.core.object.def.DefTarget.NO_DEF_TARGET;

import java.util.List;

import org.o42a.core.Scope;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.st.CommandTargets;
import org.o42a.core.st.Definer;
import org.o42a.core.st.impl.cmd.Sentences;
import org.o42a.core.st.sentence.DeclarativeSentence;
import org.o42a.core.st.sentence.Declaratives;


public abstract class DeclarativeSentences extends Sentences {

	@Override
	public abstract List<DeclarativeSentence> getSentences();

	public DefTarget target(Scope origin) {

		final CommandTargets defTargets = getTargets();

		if (!defTargets.defining()) {
			return null;
		}
		if (defTargets.havePrerequisite()) {
			return NO_DEF_TARGET;
		}

		for (DeclarativeSentence sentence : getSentences()) {

			final DefTarget sentenceTarget = sentenceTarget(origin, sentence);

			if (sentenceTarget != null) {
				return sentenceTarget;
			}
		}

		return null;
	}

	private static DefTarget sentenceTarget(
			Scope origin,
			DeclarativeSentence sentence) {

		final CommandTargets defTargets = sentence.getTargets();

		if (!defTargets.defining()) {
			return null;
		}
		if (defTargets.havePrerequisite()) {
			return NO_DEF_TARGET;
		}

		final List<Declaratives> alts = sentence.getAlternatives();
		final int size = alts.size();

		if (size != 1) {
			if (size == 0) {
				return null;
			}
			return NO_DEF_TARGET;
		}

		return statementsTarget(origin, alts.get(0));
	}

	private static DefTarget statementsTarget(
			Scope origin,
			Declaratives statements) {

		final CommandTargets defTargets = statements.getTargets();

		if (!defTargets.defining()) {
			return null;
		}
		if (defTargets.havePrerequisite()) {
			return NO_DEF_TARGET;
		}

		final List<Definer> commands = statements.getImplications();
		final int size = commands.size();

		if (size != 1) {
			if (size == 0) {
				return null;
			}
			return NO_DEF_TARGET;
		}

		return commands.get(0).toTarget(origin);
	}

}

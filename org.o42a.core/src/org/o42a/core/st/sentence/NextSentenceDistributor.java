/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.core.st.sentence;

import java.util.List;

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.source.Location;
import org.o42a.core.st.impl.local.Locals;


class NextSentenceDistributor extends Distributor {

	private final Container container;
	private final Locals locals;

	NextSentenceDistributor(Block block) {

		final List<Sentence> sentences = block.getSentences();
		final int numSentences = sentences.size();

		if (numSentences == 0) {
			this.container = block.getContainer();
			this.locals = block.externalLocals();
			assert validContainer();
			return;
		}

		final Sentence last = sentences.get(numSentences - 1);
		final List<Statements> alts = last.getAlternatives();
		final int numAlts = alts.size();

		if (numAlts > 1) {
			// Locals declared within alternative are visible only inside
			// this alternative, unless the sentence has a single alternative.
			this.container = last.getContainer();
			this.locals = last.externalLocals();
			assert validContainer();
			return;
		}
		if (last.getPrerequisite() != null
				&& !last.getKind().isInterrogative()) {
			// The sentence has prerequisite and is not a prerequisite
			// of another sentence. The locals are not exported, neither from
			// the sentence itself, nor from its prerequisites.

			final Sentence firstPrerequisite = last.firstPrerequisite();

			this.container = firstPrerequisite.getContainer();
			this.locals = firstPrerequisite.externalLocals();
			assert validContainer();
			return;
		}
		if (numAlts == 0) {
			// Empty sentence without prerequisites.
			// The next sentence will see the same locals as this one.
			this.container = last.getContainer();
			this.locals = last.externalLocals();
			assert validContainer();
			return;
		}
		// The sentence has only one alternative and has no prerequisites.
		// The locals declared in it are visible in the next sentences.
		// Even if this sentence is a prerequisite for the next one.
		final Statements singleAlt = alts.get(0);

		this.container = singleAlt.nextContainer();
		this.locals = singleAlt.locals();

		assert validContainer();
	}

	public final Locals locals() {
		return this.locals;
	}

	@Override
	public Location getLocation() {
		return this.container.getLocation();
	}

	@Override
	public Scope getScope() {
		return this.container.getScope();
	}

	@Override
	public Container getContainer() {
		return this.container;
	}

	private boolean validContainer() {
		assert getContainer() == this.locals.getContainer() :
			"Unexpected container";
		return true;
	}

}

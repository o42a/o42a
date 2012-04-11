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
package org.o42a.core.st.sentence;

import static org.o42a.core.st.CommandTarget.noCommand;

import java.util.List;

import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Command;
import org.o42a.core.st.CommandTarget;


public abstract class ImperativeSentence
		extends Sentence<Imperatives, Command> {

	private CommandTarget commandTarget;

	protected ImperativeSentence(
			LocationInfo location,
			ImperativeBlock block,
			ImperativeFactory sentenceFactory) {
		super(location, block, sentenceFactory);
	}

	@Override
	public final ImperativeBlock getBlock() {
		return (ImperativeBlock) super.getBlock();
	}

	@Override
	public final ImperativeFactory getSentenceFactory() {
		return (ImperativeFactory) super.getSentenceFactory();
	}

	@Override
	public final ImperativeSentence getPrerequisite() {
		return (ImperativeSentence) super.getPrerequisite();
	}

	public CommandTarget getCommandTarget() {
		if (this.commandTarget != null) {
			return this.commandTarget;
		}

		CommandTarget result;

		if (getPrerequisite() != null) {
			result = getPrerequisite().getCommandTarget();
		} else {
			result = noCommand();
		}

		for (Imperatives alt : getAlternatives()) {
			result = result.combine(alt.getCommandTarget());
		}

		return this.commandTarget = result;
	}

	public boolean hasOpposite(int altIdx) {

		final List<Imperatives> alternatives = getAlternatives();
		final Imperatives alt = alternatives.get(altIdx);

		if (alt.isOpposite()) {
			return true;
		}

		final int nextIdx = altIdx + 1;

		if (nextIdx >= alternatives.size()) {
			return false;
		}

		return alternatives.get(nextIdx + 1).isOpposite();
	}

}

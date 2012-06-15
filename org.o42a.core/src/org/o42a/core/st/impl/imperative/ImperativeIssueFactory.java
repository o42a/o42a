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
package org.o42a.core.st.impl.imperative;

import static org.o42a.core.st.impl.SentenceErrors.prohibitedIssueBraces;

import org.o42a.core.Distributor;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.*;
import org.o42a.util.string.Name;


public class ImperativeIssueFactory extends ImperativeFactory {

	public ImperativeIssueFactory() {
	}

	@Override
	public ImperativeBlock createBraces(
			LocationInfo location,
			Distributor distributor,
			Imperatives enclosing,
			Name name) {
		prohibitedIssueBraces(enclosing.getLogger(), location);
		return null;
	}

	@Override
	public ImperativeSentence propose(
			LocationInfo location,
			ImperativeBlock block) {
		return new ImperativeIssue(location, block);
	}

	@Override
	public ImperativeSentence claim(
			LocationInfo location,
			ImperativeBlock block) {
		block.getLogger().error(
				"prohibited_issue_exit",
				location,
				"Can not exit the loop from inside an issue");
		return null;
	}

	@Override
	public Imperatives createAlternative(
			LocationInfo location,
			ImperativeSentence sentence) {
		return super.createAlternative(location, sentence);
	}

}

/*
    Compilation Analysis
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
package org.o42a.analysis.escape;

import org.o42a.analysis.use.FlagTracker;


public class EscapeTracker extends FlagTracker<EscapeAnalyzer, EscapeFlag> {

	public final EscapeFlag noEscape() {
		return unused();
	}

	@Override
	protected EscapeAnalyzer useCaseOf(EscapeFlag flag) {
		return flag.getAnalyzer();
	}

	@Override
	protected boolean flagIsActual(EscapeAnalyzer analyzer, EscapeFlag flag) {
		return analyzer.analyzerFlag(flag);
	}

	@Override
	protected boolean flagIsKnown(EscapeFlag flag) {
		return flag.isKnown();
	}

	@Override
	protected boolean flagIsUsed(EscapeFlag flag) {
		return flag.isEscapePossible();
	}

	@Override
	protected EscapeFlag checkFlag(EscapeAnalyzer analyzer) {
		return analyzer.checkEscape();
	}

	@Override
	protected EscapeFlag unusedFlag(EscapeAnalyzer analyzer) {
		return analyzer.escapeImpossible();
	}

}

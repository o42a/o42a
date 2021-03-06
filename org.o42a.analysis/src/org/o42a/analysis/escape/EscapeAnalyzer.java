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

import static org.o42a.analysis.escape.EscapeMode.ESCAPE_IMPOSSIBLE;
import static org.o42a.analysis.escape.EscapeMode.ESCAPE_POSSIBLE;

import org.o42a.analysis.Analyzer;
import org.o42a.analysis.use.*;


public final class EscapeAnalyzer implements UseCaseInfo {

	private final Analyzer analyzer;
	private final EscapeFlag escapePossible;
	private final EscapeFlag escapeImpossible;
	private final EscapeFlag checkEscape;

	public EscapeAnalyzer(Analyzer analyzer) {
		this.analyzer = analyzer;
		this.escapePossible =
				new EscapeFlag(this, ESCAPE_POSSIBLE);
		this.escapeImpossible =
				new EscapeFlag(this, ESCAPE_IMPOSSIBLE);
		this.checkEscape =
				new EscapeFlag(this, null);
	}

	public final Analyzer getAnalyzer() {
		return this.analyzer;
	}

	public final EscapeFlag escapePossible() {
		return this.escapePossible;
	}

	public final EscapeFlag escapeImpossible() {
		return this.escapeImpossible;
	}

	public final EscapeFlag checkEscape() {
		return this.checkEscape;
	}

	public final EscapeFlag escapeFlag(UseFlag useFlag) {
		if (!useFlag.isKnown()) {
			return checkEscape();
		}
		if (useFlag.isUsed()) {
			return escapePossible();
		}
		return escapeImpossible();
	}

	public final boolean analyzerFlag(EscapeFlag flag) {
		return flag != null && flag.getAnalyzer().is(this);
	}

	@Override
	public final User<?> toUser() {
		return getAnalyzer().toUser();
	}

	@Override
	public final UseCase toUseCase() {
		return getAnalyzer().toUseCase();
	}

	public final boolean is(EscapeAnalyzer other) {
		return this == other;
	}

	@Override
	public String toString() {
		if (this.analyzer == null) {
			return super.toString();
		}
		return this.analyzer.toString();
	}

}

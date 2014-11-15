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

import java.util.Objects;
import java.util.function.Function;

import org.o42a.analysis.use.UseCase;


public final class EscapeInit {

	public static EscapeInit escapeInit(
			Function<EscapeAnalyzer, EscapeFlag> detect) {
		return new EscapeInit(detect);
	}

	private final Function<EscapeAnalyzer, EscapeFlag> detect;
	private final EscapeTracker tracker = new EscapeTracker();

	private EscapeInit(Function<EscapeAnalyzer, EscapeFlag> detect) {
		this.detect = detect;
	}

	public final EscapeMode escapeMode(EscapeAnalyzer analyzer) {
		return escapeFlag(analyzer).getEscapeMode();
	}

	public final EscapeFlag escapeFlag(EscapeAnalyzer analyzer) {

		final UseCase uc = analyzer.toUseCase();

		if (uc.isSteady()) {
			return analyzer.escapePossible();
		}
		if (this.tracker.start(analyzer)) {
			return this.tracker.lastFlag();
		}
		if (check(this.detect)) {
			return this.tracker.lastFlag();
		}

		return this.tracker.noEscape();
	}

	public final EscapeFlag lastFlag() {
		return this.tracker.lastFlag();
	}

	public final boolean check(Function<EscapeAnalyzer, EscapeFlag> detect) {
		return this.tracker.check(detect);
	}

	@Override
	public String toString() {
		if (this.tracker == null) {
			return super.toString();
		}
		return Objects.toString(this.tracker.lastFlag());
	}

}

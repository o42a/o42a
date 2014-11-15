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

import java.util.function.Function;

import org.o42a.analysis.use.UseFlag;
import org.o42a.analysis.use.UseTracker;


public class EscapeTracker extends UseTracker {

	private EscapeAnalyzer analyzer;

	public final EscapeAnalyzer getAnalyzer() {
		assert this.analyzer != null :
			"Escape mode analyzer isn't known yet";
		assert this.analyzer.toUseCase().equals(getUseFlag().getUseCase()) :
			"Wrong escape mode analyzer";
		return this.analyzer;
	}

	public final EscapeFlag getEscapeFlag() {

		final UseFlag useFlag = getUseFlag();

		if (useFlag == null) {
			return null;
		}

		return getAnalyzer().escapeFlag(useFlag);
	}

	public final boolean start(EscapeAnalyzer analyzer) {
		this.analyzer = analyzer;
		return start(analyzer.toUseCase());
	}

	public final boolean escapeBy(
			Function<EscapeAnalyzer, EscapeFlag> detect) {
		return useBy(uc -> detect.apply(getAnalyzer()).toUseFlag());
	}

	public final EscapeFlag noEscape() {
		return getAnalyzer().escapeFlag(unused());
	}

}

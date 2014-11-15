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

import java.util.function.Supplier;

import org.o42a.analysis.use.UseFlag;


public final class EscapeFlag {

	private final EscapeAnalyzer analyzer;
	private final UseFlag useFlag;
	private final EscapeMode escapeMode;

	EscapeFlag(
			EscapeAnalyzer analyzer,
			UseFlag useFlag,
			EscapeMode escapeMode) {
		this.analyzer = analyzer;
		this.useFlag = useFlag;
		this.escapeMode = escapeMode;
	}

	public final EscapeAnalyzer getAnalyzer() {
		return this.analyzer;
	}

	public final EscapeMode getEscapeMode() {
		return this.escapeMode;
	}

	public final boolean isKnown() {
		return getEscapeMode() != null;
	}

	public final boolean isEscapePossible() {
		return isKnown() && getEscapeMode().isEscapePossible();
	}

	public final boolean isEscapeImpossible() {
		return isKnown() && !getEscapeMode().isEscapePossible();
	}

	public final UseFlag toUseFlag() {
		return this.useFlag;
	}

	public final EscapeFlag combine(EscapeFlag other) {
		if (!isEscapeImpossible()) {
			return this;
		}
		return other;
	}

	public final EscapeFlag combine(Supplier<EscapeFlag> other) {
		if (!isEscapeImpossible()) {
			return this;
		}
		return other.get();
	}

	@Override
	public String toString() {
		if (this.escapeMode == null) {
			return "CheckEscape[" + this.analyzer + ']';
		}
		if (this.escapeMode.isEscapePossible()) {
			return "EscapePossible[" + this.analyzer + ']';
		}
		return "EscapeImpossible[" + this.analyzer + ']';
	}

}

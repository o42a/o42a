/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.ref;

import org.o42a.analysis.Analyzer;
import org.o42a.core.Scope;


public final class RootNormalizer {

	private final Analyzer analyzer;
	private final Scope normalizedScope;

	public RootNormalizer(Analyzer analyzer, Scope normalizedScope) {
		this.analyzer = analyzer;
		this.normalizedScope = normalizedScope;
	}

	public final Analyzer getAnalyzer() {
		return this.analyzer;
	}

	public final Scope getNormalizedScope() {
		return this.normalizedScope;
	}

	public final Normalizer newNormalizer() {
		return new Normalizer(this);
	}

	@Override
	public String toString() {
		if (this.normalizedScope == null) {
			return super.toString();
		}
		return "RootNormalizer[to " + this.normalizedScope
				+ " by " + this.analyzer + ']';
	}

}

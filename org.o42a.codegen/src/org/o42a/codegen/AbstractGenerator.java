/*
    Compiler Code Generator
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.codegen;

import org.o42a.analysis.Analyzer;
import org.o42a.codegen.debug.Debug;


public abstract class AbstractGenerator extends Generator {

	private final Analyzer analyzer;
	private final Debug debug;

	public AbstractGenerator(Analyzer analyzer) {
		assert analyzer != null :
			"Analysis not specified";
		this.analyzer = analyzer;
		this.debug = new Debug(this);
	}

	@Override
	public final Analyzer getAnalyzer() {
		return this.analyzer;
	}

	@Override
	public final Debug getDebug() {
		return this.debug;
	}

}

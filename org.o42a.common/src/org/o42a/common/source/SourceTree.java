/*
    Modules Commons
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.common.source;

import org.o42a.core.source.SourceFileName;
import org.o42a.util.io.Source;


public abstract class SourceTree<S extends Source> {

	private final S source;
	private final SourceFileName fileName;

	public SourceTree(S source, SourceFileName fileName) {
		assert source != null :
			"Source not specified";
		assert fileName != null :
			"File name not specified";
		this.source = source;
		this.fileName = fileName;
	}

	public final S getSource() {
		return this.source;
	}

	public SourceFileName getFileName() {
		return this.fileName;
	}

	public abstract Iterable<? extends SourceTree<S>> childTrees();

	@Override
	public String toString() {
		if (this.source == null) {
			return super.toString();
		}
		return this.source.toString();
	}

}

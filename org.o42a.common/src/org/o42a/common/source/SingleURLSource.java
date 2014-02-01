/*
    Compiler Commons
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
package org.o42a.common.source;

import java.net.URL;
import java.util.Collections;
import java.util.Iterator;

import org.o42a.util.io.URLSource;


public final class SingleURLSource extends URLSourceTree {

	public SingleURLSource(String name, URL base, String path) {
		super(name, base, path);
	}

	public SingleURLSource(URLSourceTree parent, String path) {
		super(parent, path);
	}

	public SingleURLSource(URLSource parent, String path) {
		super(parent, path);
	}

	public SingleURLSource(URLSource source) {
		super(source);
	}

	@Override
	public Iterator<? extends SourceTree<URLSource>> subTrees() {
		return Collections.<SourceTree<URLSource>>emptyList().iterator();
	}

}

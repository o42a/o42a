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


public final class EmptyURLSource extends URLSourceTree {

	public EmptyURLSource(String name, URL base, String path) {
		super(new EmptySource(name, base, path));
	}

	public EmptyURLSource(URLSource parent, String path) {
		super(new EmptySource(
				parent.getBase(),
				childDirURL(parent.getURL()),
				path));
	}

	public EmptyURLSource(URLSourceTree parent, String path) {
		this(parent.getSource(), path);
	}

	@Override
	public Iterator<? extends SourceTree<URLSource>> subTrees() {
		return Collections.<SourceTree<URLSource>>emptyList().iterator();
	}

	static final class EmptySource extends URLSource {

		public EmptySource(String name, URL base, String path) {
			super(name, base, path);
		}

		public EmptySource(URL base, URL relativeTo, String path) {
			super(base, relativeTo, path);
		}

		public EmptySource(URLSource parent, String path) {
			super(parent, path);
		}

		@Override
		public boolean isEmpty() {
			return true;
		}

	}

}

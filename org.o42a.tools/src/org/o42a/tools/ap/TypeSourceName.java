/*
    Build Tools
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
package org.o42a.tools.ap;

import static org.o42a.util.io.SourceFileName.FILE_SUFFIX;


final class TypeSourceName {

	private final String name;
	private final String key;
	private final SourceKind sourceKind;

	public TypeSourceName(String name) {
		this.name = name;
		if (name.endsWith("/")) {
			this.sourceKind = SourceKind.DIR;
			this.key = name.substring(0, name.length() - 1);
		} else if (name.endsWith(FILE_SUFFIX)) {
			this.sourceKind = SourceKind.FILE;
			this.key = name.substring(0, name.length() - FILE_SUFFIX.length());
		} else {
			this.sourceKind = SourceKind.EMPTY;
			this.key = name;
		}
	}

	public final String getName() {
		return this.name;
	}

	public final String getKey() {
		return this.key;
	}

	public final SourceKind getSourceKind() {
		return this.sourceKind;
	}

	@Override
	public String toString() {
		if (this.name == null) {
			return super.toString();
		}
		return this.name;
	}

}

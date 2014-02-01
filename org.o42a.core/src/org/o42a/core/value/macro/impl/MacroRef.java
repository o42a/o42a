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
package org.o42a.core.value.macro.impl;

import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.value.macro.Macro;
import org.o42a.core.value.macro.MacroExpander;


public class MacroRef implements Macro {

	private final Ref ref;

	public MacroRef(Ref ref) {
		this.ref = ref;
	}

	@Override
	public Path expand(MacroExpander expander) {
		return this.ref.getPath().getPath();
	}

	@Override
	public Path reexpand(MacroExpander expander) {
		return this.ref.getPath().getPath();
	}

	@Override
	public String toString() {
		if (this.ref == null) {
			return super.toString();
		}
		return this.ref.toString();
	}

}

/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.object.macro.impl;

import org.o42a.core.Scope;
import org.o42a.core.ref.Consumer;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathExpander;
import org.o42a.core.ref.path.PathFragment;


final class MacroExpansionTemplate extends PathFragment {

	private final MacroExpansion expansion;
	private final Consumer consumer;

	MacroExpansionTemplate(MacroExpansion expansion, Consumer consumer) {
		this.expansion = expansion;
		this.consumer = consumer;
	}

	@Override
	public boolean isTemplate() {
		return true;
	}

	@Override
	public Path expand(PathExpander expander, int index, Scope start) {
		this.expansion.getMacroRef().assertCompatible(start);

		final Ref consumption =
				this.expansion.expandMacro(this.consumer, start);

		if (consumption == null) {
			return null;
		}

		return consumption.getPath().getRawPath();
	}

	@Override
	public String toString() {
		if (this.expansion == null) {
			return super.toString();
		}
		return this.expansion.toString();
	}

	final Ref toRef() {

		final Ref macroRef = this.expansion.getMacroRef();

		return toPath()
				.bind(macroRef, macroRef.getScope())
				.target(macroRef.distribute());
	}

}

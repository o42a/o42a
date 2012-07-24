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
package org.o42a.core.object.value;

import org.o42a.core.object.meta.Nesting;
import org.o42a.core.object.value.impl.TypeParamMacroDep;
import org.o42a.core.object.value.impl.TypeParamMetaDep;
import org.o42a.core.ref.Consumer;
import org.o42a.core.ref.Ref;


public class TypeParamConsumer implements Consumer {

	private final TypeParamMacroDep macroDep;

	public TypeParamConsumer(Nesting nesting) {
		this.macroDep = new TypeParamMacroDep(nesting, 0);
	}

	private TypeParamConsumer(TypeParamMacroDep macroDep) {
		this.macroDep = macroDep;
	}

	public final TypeParamConsumer nested() {
		return new TypeParamConsumer(new TypeParamMacroDep(
				this.macroDep.getNesting(),
				this.macroDep.getDepth() + 1));
	}

	@Override
	public Ref expandMacro(Ref macroRef, Ref macroExpansion) {

		final TypeParamMetaDep dep = this.macroDep.buildDep(macroRef);

		if (dep == null) {
			return macroExpansion;
		}

		dep.register();

		return dep.expandMacro(macroRef, macroExpansion);
	}

}

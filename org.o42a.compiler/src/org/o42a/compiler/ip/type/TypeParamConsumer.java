/*
    Compiler
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
package org.o42a.compiler.ip.type;

import org.o42a.core.object.meta.Nesting;
import org.o42a.core.ref.Consumer;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PathTemplate;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.value.ValueStructFinder;


final class TypeParamConsumer extends TypeConsumer implements Consumer {

	private final TypeParamMacroDep macroDep;

	TypeParamConsumer(Nesting nesting) {
		this.macroDep = new TypeParamMacroDep(nesting, 0);
	}

	private TypeParamConsumer(TypeParamMacroDep macroDep) {
		this.macroDep = macroDep;
	}

	@Override
	public final TypeParamConsumer paramConsumer() {
		return new TypeParamConsumer(new TypeParamMacroDep(
				this.macroDep.getNesting(),
				this.macroDep.getDepth() + 1));
	}

	@Override
	public TypeRef consumeType(Ref ref, ValueStructFinder valueStruct) {

		final Ref consumption = ref.consume(this);

		if (consumption == null) {
			return null;
		}

		return consumption.toTypeRef(valueStruct);
	}

	@Override
	public Ref expandMacro(
			Ref macroRef,
			Ref macroExpansion,
			PathTemplate template) {

		final TypeParamMetaDep dep = this.macroDep.buildDep(macroRef, template);

		if (dep == null) {
			return macroExpansion;
		}

		dep.register();

		return dep.expandMacro(macroRef, macroExpansion, template);
	}

}

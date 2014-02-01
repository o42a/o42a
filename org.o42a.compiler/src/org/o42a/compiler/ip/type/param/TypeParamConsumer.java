/*
    Compiler
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
package org.o42a.compiler.ip.type.param;

import org.o42a.common.macro.type.TypeParamMacroDep;
import org.o42a.common.macro.type.TypeParameterKey;
import org.o42a.compiler.ip.type.ParamTypeRef;
import org.o42a.compiler.ip.type.TypeConsumer;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.ref.Consumer;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PathTemplate;
import org.o42a.core.ref.type.TypeRefParameters;
import org.o42a.core.value.macro.MacroConsumer;


public class TypeParamConsumer extends TypeConsumer implements Consumer {

	private final TypeParamMacroDep macroDep;

	public TypeParamConsumer(Nesting nesting, TypeParameterKey parameterKey) {
		super(nesting);
		this.macroDep = new TypeParamMacroDep(nesting, parameterKey, 0);
	}

	private TypeParamConsumer(TypeParamMacroDep macroDep) {
		super(macroDep.getNesting());
		this.macroDep = macroDep;
	}

	@Override
	public final TypeParamConsumer paramConsumer(
			TypeParameterKey parameterKey) {
		return new TypeParamConsumer(new TypeParamMacroDep(
				this.macroDep.getNesting(),
				parameterKey,
				this.macroDep.getDepth() + 1));
	}

	@Override
	public ParamTypeRef consumeType(
			Ref ref,
			TypeRefParameters typeParameters) {

		final Ref consumption = ref.consume(this);

		if (consumption == null) {
			return null;
		}

		return new ParamTypeRef(consumption.toTypeRef(), typeParameters);
	}

	@Override
	public MacroConsumer expandMacro(
			Ref macroRef,
			PathTemplate template,
			Ref expansion) {
		return this.macroDep.expandMacro(macroRef, template);
	}

}

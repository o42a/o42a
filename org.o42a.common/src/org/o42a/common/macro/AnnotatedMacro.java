/*
    Compiler Commons
    Copyright (C) 2012,2013 Ruslan Lopatin

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
package org.o42a.common.macro;

import static org.o42a.core.value.TypeParameters.typeParameters;

import org.o42a.common.object.AnnotatedObject;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.macro.Macro;
import org.o42a.core.value.macro.MacroDef;


public abstract class AnnotatedMacro extends AnnotatedObject implements Macro {

	public AnnotatedMacro(Obj owner, AnnotatedSources sources) {
		super(owner, sources);
	}

	@Override
	protected Definitions explicitDefinitions() {
		return new MacroDef(this, this, this)
		.toDefinitions(typeParameters(this, ValueType.MACRO));
	}

}

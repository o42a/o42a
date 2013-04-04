/*
    Compiler Commons
    Copyright (C) 2011-2013 Ruslan Lopatin

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
package org.o42a.common.object;

import static org.o42a.core.value.ValueType.DIRECTIVE;

import org.o42a.core.object.Obj;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.ref.Ref;
import org.o42a.core.value.directive.Directive;


public abstract class DirectiveObject
		extends AnnotatedObject
		implements Directive {

	public DirectiveObject(Obj owner, AnnotatedSources sources) {
		super(owner, sources);
	}

	@Override
	protected Definitions explicitDefinitions() {

		final Ref ref = DIRECTIVE.constantRef(this, distribute(), this);

		return ref.toDefinitions(definitionEnv());
	}

}

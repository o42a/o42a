/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core.artifact.array;

import static org.o42a.core.ref.Cond.trueCondition;

import org.o42a.core.Scope;
import org.o42a.core.def.Definitions;
import org.o42a.core.member.field.FieldVariant;
import org.o42a.core.member.field.FieldVariantDecl;
import org.o42a.core.ref.Cond;
import org.o42a.core.st.DefinitionTarget;


final class ArrayFieldVariantDecl extends FieldVariantDecl<Array> {

	ArrayFieldVariantDecl(
			ArrayFieldDecl fieldDecl,
			FieldVariant<Array> variant) {
		super(fieldDecl, variant);
	}

	@Override
	protected void init() {
	}

	@Override
	protected Cond condition(Scope scope) {
		// TODO array initializer logical value
		return trueCondition(getVariant(), scope);
	}

	@Override
	protected Definitions define(DefinitionTarget scope) {
		throw new UnsupportedOperationException(
				"An attempt to define array: " + this);
	}


}

/*
    Compiler Commons
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
package org.o42a.common.macro.st;

import static org.o42a.core.member.field.DefinitionTarget.objectDefinition;
import static org.o42a.core.value.link.LinkValueType.LINK;

import org.o42a.core.Scope;
import org.o42a.core.member.field.*;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.Ref;


final class TempFieldDefinition extends FieldDefinition {

	private final Ref expansion;
	private final boolean condition;

	TempFieldDefinition(Ref expansion, boolean condition) {
		super(expansion, expansion.distribute());
		this.expansion = expansion;
		this.condition = condition;
	}

	@Override
	public void init(Field field, Ascendants implicitAscendants) {
	}

	@Override
	public DefinitionTarget getDefinitionTarget() {
		return objectDefinition();
	}

	@Override
	public void defineObject(ObjectDefiner definer) {

		final Scope enclosingScope = definer.getField().getEnclosingScope();

		definer.setAncestor(LINK.typeRef(this.expansion, enclosingScope));
		if (!this.condition) {
			definer.setParameters(new ParentTypeParameters(definer));
		}
		definer.define(new ExpandMacroBlock(this.expansion)::definitions);
	}

	@Override
	public void overrideObject(ObjectDefiner definer) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void defineMacro(MacroDefiner definer) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		if (this.expansion == null) {
			return super.toString();
		}
		if (this.condition) {
			return this.expansion.toString();
		}
		return '=' + this.expansion.toString();
	}

}

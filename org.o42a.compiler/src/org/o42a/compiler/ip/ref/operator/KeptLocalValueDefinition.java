/*
    Compiler
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
package org.o42a.compiler.ip.ref.operator;

import org.o42a.core.member.field.*;
import org.o42a.core.object.Obj;
import org.o42a.core.object.state.Keeper;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PrefixPath;


public class KeptLocalValueDefinition extends FieldDefinition {

	private final KeptLocalValue keptValue;
	private FieldDefinition definition;

	public KeptLocalValueDefinition(KeptLocalValue keptValue) {
		super(keptValue.getLocation(), keptValue.getValue().distribute());
		this.keptValue = keptValue;
	}

	@Override
	public void init(Field field, Ascendants implicitAscendants) {

		final Obj object = field.toObject();
		final Ref value = this.keptValue.getValue().rescope(field);
		final Keeper keeper = object.keepers().keep(this, value);
		final PrefixPath prefix =
				field.getKey().toPath().toPrefix(field.getEnclosingScope());

		this.definition = keeper.toRef().prefixWith(prefix).toFieldDefinition();
	}

	@Override
	public DefinitionTarget getDefinitionTarget() {
		return this.definition.getDefinitionTarget();
	}

	@Override
	public void defineObject(ObjectDefiner definer) {
		this.definition.defineObject(definer);
	}

	@Override
	public void overrideObject(ObjectDefiner definer) {
		this.definition.overrideObject(definer);
	}

	@Override
	public void defineLink(LinkDefiner definer) {
		this.definition.defineLink(definer);
	}

	@Override
	public void defineMacro(MacroDefiner definer) {
		this.definition.defineMacro(definer);
	}

	@Override
	public String toString() {
		if (this.keptValue == null) {
			return super.toString();
		}
		return this.keptValue.toString();
	}

}

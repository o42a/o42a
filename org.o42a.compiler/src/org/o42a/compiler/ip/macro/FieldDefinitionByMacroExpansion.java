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
package org.o42a.compiler.ip.macro;

import static org.o42a.compiler.ip.macro.MacroExpansionStep.prohibitedExpansion;
import static org.o42a.core.member.field.DefinitionTarget.linkDefinition;
import static org.o42a.core.member.field.DefinitionTarget.objectDefinition;
import static org.o42a.core.st.sentence.BlockBuilder.valueBlock;

import org.o42a.core.member.field.*;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRef;


final class FieldDefinitionByMacroExpansion extends FieldDefinition {

	private final Ref expansion;
	private Field field;
	private boolean invalid;

	FieldDefinitionByMacroExpansion(Ref expansion) {
		super(expansion, expansion.distribute());
		this.expansion = expansion;
	}

	@Override
	public boolean isValid() {
		return !this.invalid && super.isValid();
	}

	@Override
	public void init(Field field, Ascendants implicitAscendants) {
		this.field = field;
		validate();
	}

	@Override
	public DefinitionTarget getDefinitionTarget() {

		final TypeRef type = this.field.getDeclaration().getType();

		if (type == null) {
			return objectDefinition();
		}

		return linkDefinition(type.getValueStruct().getLinkDepth());
	}

	@Override
	public void defineObject(ObjectDefiner definer) {
		define(definer);
	}

	@Override
	public void overrideObject(ObjectDefiner definer) {
		define(definer);
	}

	@Override
	public void defineLink(LinkDefiner definer) {
		define(definer);
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
		return this.expansion.toString();
	}

	private void validate() {

		final FieldDeclaration declaration = this.field.getDeclaration();

		if (declaration.isMacro()) {
			// Macro can not be defined by macro expansion.
			prohibitedExpansion(getLogger(), declaration);
			this.invalid = true;
			return;
		}
		if (declaration.getLinkType() != null
				&& declaration.getType() == null) {
			// Link without interface can not be defined by macro expansion.
			prohibitedExpansion(getLogger(), declaration);
			this.invalid = true;
			return;
		}
	}

	private void define(FieldDefiner definer) {
		definer.define(valueBlock(this.expansion));
	}

}

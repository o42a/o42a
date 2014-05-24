/*
    Compiler Core
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
package org.o42a.core.member.field.decl;

import static org.o42a.core.st.sentence.BlockBuilder.valueBlock;

import java.util.function.Function;

import org.o42a.core.member.field.MacroDefiner;
import org.o42a.core.object.def.DefinitionsBuilder;
import org.o42a.core.object.def.ObjectToDefine;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.value.ValueType;


final class MacroDefinerImpl implements MacroDefiner {

	private final DeclaredField field;
	private Ascendants ascendants;

	MacroDefinerImpl(DeclaredField field, Ascendants implicitAscendants) {
		this.field = field;
		this.ascendants = implicitAscendants;
	}

	@Override
	public final DeclaredField getField() {
		return this.field;
	}

	@Override
	public void makeEager() {
		this.field.makeEager();
	}

	public final Ascendants getAscendants() {

		final TypeRef ancestor = this.ascendants.getAncestor();

		if (ancestor == null) {
			this.ascendants = this.ascendants.setAncestor(
					ValueType.MACRO.typeRef(
							getField().getDeclaration(),
							getField().getEnclosingScope()));
		} else {
			if (!ancestor.getValueType().isMacro()) {
				getField().getLogger().error(
						"not_macro_field",
						getField().getDeclaration(),
						"Not a macro field");
				getField().invalid();
				return null;
			}
		}

		return this.ascendants;
	}

	@Override
	public void setRef(Ref ref) {
		getField().addDefinitions(valueBlock(ref)::definitions);
	}

	@Override
	public void define(
			Function<ObjectToDefine, DefinitionsBuilder> definitions) {
		getField().addDefinitions(definitions);
	}

	@Override
	public String toString() {
		if (this.field == null) {
			return super.toString();
		}
		return "MacroDefiner[" + this.field + ']';
	}

}

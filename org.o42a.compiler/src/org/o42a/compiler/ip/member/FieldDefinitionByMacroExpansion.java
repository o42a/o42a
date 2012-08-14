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
package org.o42a.compiler.ip.member;

import static org.o42a.compiler.ip.st.macro.StatementConsumer.consumeSelfAssignment;
import static org.o42a.core.member.field.DefinitionTarget.linkDefinition;
import static org.o42a.core.member.field.DefinitionTarget.objectDefinition;

import org.o42a.core.member.field.*;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.BlockBuilder;
import org.o42a.core.st.sentence.Statements;


final class FieldDefinitionByMacroExpansion extends FieldDefinition {

	private final FieldDeclaration declaration;
	private final Ref expansion;

	FieldDefinitionByMacroExpansion(
			FieldDeclaration declaration,
			Ref expansion) {
		super(expansion, expansion.distribute());
		this.declaration = declaration;
		this.expansion = expansion;
	}

	@Override
	public void setImplicitAscendants(Ascendants ascendants) {
	}

	@Override
	public DefinitionTarget getDefinitionTarget() {

		final TypeRef type = this.declaration.getType();

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

	private void define(FieldDefiner definer) {
		definer.define(new MacroExpansionBlock(this.expansion));
	}

	private static final class MacroExpansionBlock extends BlockBuilder {

		private final Ref expansion;

		MacroExpansionBlock(Ref expansion) {
			super(expansion);
			this.expansion = expansion;
		}

		@Override
		public void buildBlock(Block<?, ?> block) {

			final Statements<?, ?> statements =
					block.propose(this).alternative(this);

			statements.selfAssign(
					consumeSelfAssignment(statements, this, this.expansion));
		}

		@Override
		public String toString() {
			if (this.expansion == null) {
				return super.toString();
			}
			return "(= " + this.expansion + ')';
		}

	}

}

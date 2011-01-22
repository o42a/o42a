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
package org.o42a.core.member.field;

import org.o42a.core.artifact.Artifact;
import org.o42a.core.def.Definitions;
import org.o42a.core.st.DefinitionTarget;


public abstract class FieldVariantDecl<A extends Artifact<A>> {

	private final FieldDecl<A> fieldDecl;
	private final FieldVariant<A> variant;

	public FieldVariantDecl(FieldDecl<A> fieldDecl, FieldVariant<A> variant) {
		this.fieldDecl = fieldDecl;
		this.variant = variant;
	}

	public FieldDecl<A> getFieldDecl() {
		return this.fieldDecl;
	}

	public DeclaredField<A> getField() {
		return this.fieldDecl.getField();
	}

	public FieldVariant<A> getVariant() {
		return this.variant;
	}

	@Override
	public String toString() {
		return this.variant.toString();
	}

	protected abstract void init();

	protected abstract void declareMembers();

	protected abstract Definitions define(DefinitionTarget scope);

}

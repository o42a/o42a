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

import org.o42a.core.CompilerLogger;
import org.o42a.core.artifact.Artifact;


public abstract class FieldDecl<A extends Artifact<A>> {

	private final DeclaredField<A> field;

	public FieldDecl(DeclaredField<A> field) {
		this.field = field;
	}

	public DeclaredField<A> getField() {
		return this.field;
	}

	@Override
	public String toString() {
		return this.field.toString();
	}

	protected final CompilerLogger getLogger() {
		return getField().getLogger();
	}

	protected void addVariant(FieldVariant<A> variant) {
		getField().addVariant(variant);
	}

	protected FieldVariantDecl<A> decl(FieldVariant<A> variant) {
		return variant.getDecl();
	}

	protected abstract A declareArtifact();

	protected abstract A overrideArtifact();

	protected abstract A propagateArtifact();

	protected abstract FieldVariantDecl<A> variantDecl(FieldVariant<A> variant);

	protected abstract void merge(FieldDecl<?> decl);

}

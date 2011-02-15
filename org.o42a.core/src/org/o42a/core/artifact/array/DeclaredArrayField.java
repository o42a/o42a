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

import static org.o42a.core.artifact.array.ArrayInitializer.invalidArrayInitializer;
import static org.o42a.core.member.field.FieldDefinition.invalidDefinition;

import java.util.List;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.artifact.TypeRef;
import org.o42a.core.member.field.*;
import org.o42a.core.ref.Ref;


class DeclaredArrayField extends DeclaredField<Array> {

	private FieldDefinition definition;
	private boolean invalid;

	DeclaredArrayField(MemberField member) {
		super(member, ArtifactKind.ARRAY);
	}

	private DeclaredArrayField(
			Container enclosingContainer,
			DeclaredField<Array> sample) {
		super(enclosingContainer, sample);
	}

	@Override
	protected Array declareArtifact() {
		return new DeclaredArray(this);
	}

	@Override
	protected Array overrideArtifact() {
		if (getDeclaration().isPrototype()) {
			getLogger().prohibitedPrototype(this);
			invalid();
		}
		return new OverriddenArray(this);
	}

	@Override
	protected FieldVariant<Array> createVariant(
			FieldDeclaration declaration,
			FieldDefinition definition) {
		return new ArrayFieldVariant(this, declaration, definition);
	}

	@Override
	protected void merge(DeclaredField<Array> other) {
		getLogger().ambiguousMember(other, getDisplayName());
	}

	@Override
	protected DeclaredField<Array> propagate(Scope enclosingScope) {
		return new DeclaredArrayField(enclosingScope.getContainer(), this);
	}

	@Override
	protected Array propagateArtifact(Field<Array> overridden) {
		return new PropagatedArray(this);
	}

	TypeRef declaredItemTypeRef() {

		final FieldDefinition definition = getDefinition();

		if (definition == null) {
			return null;
		}

		final ArrayInitializer arrayInitializer =
			definition.getArrayInitializer();

		if (arrayInitializer == null) {
			return null;
		}

		return arrayInitializer.getItemType();
	}

	ArrayTypeRef inheritedTypeRef() {

		ArrayTypeRef typeRef = null;

		for (Field<Array> field : getOverridden()) {

			final ArrayTypeRef overriddenTypeRef =
				field.getArtifact().getArrayTypeRef();

			if (typeRef == null) {
				typeRef = overriddenTypeRef;
			} else {

				final ArrayTypeRef commonInheritant =
					typeRef.commonDerivative(overriddenTypeRef);

				if (commonInheritant != null) {
					typeRef = commonInheritant;
				} else {
					getLogger().unexpectedType(
							this,
							overriddenTypeRef,
							typeRef);
					invalid();
				}
			}
		}

		return typeRef != null
		? typeRef.toScope(getEnclosingScope()) : null;
	}

	ArrayInitializer declaredInitializer() {

		final FieldDefinition definition = getDefinition();

		if (!definition.isValid()) {
			return invalidArrayInitializer(this, definition.distribute());
		}
		if (definition.isArray()) {
			return definition.getArrayInitializer();
		}

		final Ref value = getDefinition().getValue();

		if (value == null) {
			return null;
		}

		final Array array = value.resolve(getEnclosingScope()).toArray();

		if (array == null) {
			return null;
		}

		return array.getInitializer();
	}

	final void invalid() {
		this.invalid = true;
	}

	final boolean validate() {
		return !this.invalid;
	}

	FieldDefinition getDefinition() {
		if (this.definition == null) {

			final List<FieldVariant<Array>> variants = getVariants();

			if (variants.size() != 1) {
				invalid();
				this.definition = invalidDefinition(this, distribute());
			} else {
				this.definition = variants.get(0).getDefinition();
			}
		}

		return this.definition;
	}

}

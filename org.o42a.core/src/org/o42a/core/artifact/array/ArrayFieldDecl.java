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

import org.o42a.core.artifact.TypeRef;
import org.o42a.core.member.field.*;
import org.o42a.core.ref.Ref;


class ArrayFieldDecl extends FieldDecl<Array> {

	private FieldDefinition definition;
	private boolean invalid;

	ArrayFieldDecl(DeclaredField<Array> field) {
		super(field);
	}

	@Override
	protected Array declareArtifact() {
		return new DeclaredArray(this);
	}

	@Override
	protected Array overrideArtifact() {
		if (getField().getDeclaration().isPrototype()) {
			getLogger().prohibitedPrototype(getField());
			invalid();
		}
		return new OverriddenArray(this);
	}

	@Override
	protected Array propagateArtifact() {
		return new PropagatedArray(this);
	}

	@Override
	protected void merge(FieldDecl<?> decl) {
		getLogger().ambiguousField(
				decl.getField(),
				getField().getDisplayName());
	}

	@Override
	protected FieldVariantDecl<Array> variantDecl(FieldVariant<Array> variant) {
		if (!variant.getInitialConditions().isEmpty(getField())) {
			getLogger().prohibitedConditionalDeclaration(variant);
			invalid();
		}
		return new ArrayFieldVariantDecl(this, variant);
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

		for (Field<Array> field : getField().getOverridden()) {

			final ArrayTypeRef overriddenTypeRef =
				field.getArtifact().getArrayTypeRef();

			if (typeRef == null) {
				typeRef = overriddenTypeRef;
			} else {

				final ArrayTypeRef commonInheritant =
					typeRef.commonInheritant(overriddenTypeRef);

				if (commonInheritant != null) {
					typeRef = commonInheritant;
				} else {
					getLogger().unexpectedType(
							getField(),
							overriddenTypeRef,
							typeRef);
					invalid();
				}
			}
		}

		return typeRef != null
		? typeRef.toScope(getField().getEnclosingScope()) : null;
	}

	ArrayInitializer declaredInitializer() {

		final FieldDefinition definition = getDefinition();

		if (!definition.isValid()) {
			return invalidArrayInitializer(getField(), definition.distribute());
		}
		if (definition.isArray()) {
			return definition.getArrayInitializer();
		}

		final Ref value = getDefinition().getValue();

		if (value == null) {
			return null;
		}

		final Array array =
			value.resolve(getField().getEnclosingScope()).toArray();

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

			final List<FieldVariant<Array>> variants = getField().getVariants();

			if (variants.size() != 1) {
				invalid();
				this.definition =
					invalidDefinition(getField(), getField().distribute());
			} else {
				this.definition = variants.get(0).getDefinition();
			}
		}

		return this.definition;
	}

}

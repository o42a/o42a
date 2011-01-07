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
package org.o42a.core.artifact.object;

import java.util.List;

import org.o42a.core.Scope;
import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.member.AdapterId;
import org.o42a.core.member.field.*;
import org.o42a.core.st.sentence.*;


class ObjectFieldDecl extends FieldDecl<Obj> {

	private Block<Declaratives> definitionBlock;
	private Block<?> enclosing;
	private Registry fieldRegistry;

	ObjectFieldDecl(DeclaredField<Obj> field) {
		super(field);
	}

	@Override
	protected Obj declareArtifact() {

		final Ascendants ascendants =
			buildAscendants(new Ascendants(getField()));

		if (ascendants.getAncestor() != null) {
			return new DeclaredCall(this, ascendants);
		}

		return new DeclaredObject(this);
	}

	@Override
	protected Obj overrideArtifact() {
		return new OverriddenObject(this);
	}

	@Override
	protected Obj propagateArtifact() {
		return new PropagatedObject(getField());
	}

	@Override
	protected FieldVariantDecl<Obj> variantDecl(FieldVariant<Obj> variant) {
		return new ObjectFieldVariantDecl(this, variant);
	}

	@Override
	protected void merge(FieldDecl<?> decl) {
		if (decl.getField().getArtifact().getKind() != ArtifactKind.OBJECT) {
			getField().getLogger().notObjectDeclaration(decl.getField());
			return;
		}

		@SuppressWarnings("unchecked")
		final DeclaredField<Obj> other = (DeclaredField<Obj>) decl.getField();
		final DeclaredField<Obj> field = getField();

		for (FieldVariant<Obj> variant : other.getVariants()) {
			if (variant.getDeclaration().validateVariantDeclaration(field)) {
				addVariant(variant);
			}
		}
	}

	ObjectMemberRegistry getFieldRegistry() {
		if (this.fieldRegistry == null) {
			this.fieldRegistry = new Registry();
		}
		return this.fieldRegistry;
	}

	Block<?> getEnclosing() {
		if (this.enclosing == null) {

			Block<?> allVariantsContainer = allVariantsContainer();

			if (allVariantsContainer == null) {
				this.enclosing = getDefinitionBlock();
			} else {
				this.enclosing = nonConditionlContainer(allVariantsContainer);
			}
		}

		return this.enclosing;
	}

	Block<Declaratives> getDefinitionBlock() {
		if (this.definitionBlock == null) {

			final DeclaredField<Obj> field = getField();

			if (field.getVariants().size() > 1) {
				throw new IllegalStateException(
						"More than one variant of field " + this);
			}
			this.definitionBlock = new DeclarativeBlock(
					field,
					field.isOverride()
					? field.getContainer() : field.getEnclosingContainer(),
					getFieldRegistry());
		}

		return this.definitionBlock;
	}

	Ascendants buildAscendants(Ascendants ascendants) {
		for (FieldVariant<Obj> variant : getField().getVariants()) {
			updateAscendants(ascendants, variant);
		}

		return ascendants;
	}

	private Block<?> allVariantsContainer() {

		final List<FieldVariant<Obj>> variants = getField().getVariants();

		Block<?> enclosing = null;

		for (FieldVariant<Obj> variant : variants) {

			final Statements<?> variantEnclosing = variant.getEnclosing();

			if (variantEnclosing == null) {
				return null;
			}

			final Block<?> block = variantEnclosing.getSentence().getBlock();

			if (enclosing == null) {
				enclosing = block;
				continue;
			}
			if (enclosing.contains(block)) {
				continue;
			}

			enclosing = enclosingBlock(block, enclosing);
		}

		return enclosing;
	}

	private Block<?> nonConditionlContainer(Block<?> enclosing) {
		while (enclosing.isConditional()) {
			enclosing =
				enclosing.getEnclosing().getSentence().getBlock();
		}
		return enclosing;
	}

	private Block<?> enclosingBlock(Block<?> block1, Block<?> block2) {
		if (block1.contains(block2)) {
			return block1;
		}

		final Statements<?> enclosing = block1.getEnclosing();

		if (enclosing == null) {
			throw new IllegalStateException(
					"Can not find enclosing block for " + this);
		}

		return enclosingBlock(enclosing.getSentence().getBlock(), block2);
	}

	private void updateAscendants(
			Ascendants ascendants,
			FieldVariant<Obj> variant) {

		final Scope scope = getField().getEnclosingScope();

		variant.getDefinition().getAscendants().updateAscendants(
				scope,
				ascendants);

		final AdapterId adapterId =
			getField().toMember().getId().getAdapterId();

		if (adapterId != null) {
			ascendants.addExplicitSample(adapterId.adapterType(scope));
		}
	}

	private final class Registry extends OwnerFieldRegistry {

		@Override
		protected Obj findOwner() {
			return getField().getArtifact();
		}

	}

}

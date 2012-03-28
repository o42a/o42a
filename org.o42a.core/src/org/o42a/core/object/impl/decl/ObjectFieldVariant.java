/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.core.object.impl.decl;

import org.o42a.core.Container;
import org.o42a.core.Namespace;
import org.o42a.core.Scope;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.member.field.FieldVariant;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.Logical;
import org.o42a.core.st.Definer;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.StatementEnv;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.value.ValueStruct;


final class ObjectFieldVariant extends FieldVariant<Obj> {

	private DeclarativeBlock content;
	private Definer definer;
	private Ascendants ascendants;

	ObjectFieldVariant(
			DeclaredObjectField field,
			FieldDeclaration declaration,
			FieldDefinition definition) {
		super(field, declaration, definition);
	}

	public final Ascendants getAscendants() {
		return this.ascendants;
	}

	public DeclarativeBlock getContent() {
		if (this.content != null) {
			return this.content;
		}

		final Container container;

		if (getField().ownsCompilerContext()) {
			container = new Namespace(
					getDefinition(),
					getField().getContainer());
		} else {
			container = getField().getContainer();
		}

		this.content = new DeclarativeBlock(
				container,
				container,
				getObjectField().getMemberRegistry());
		this.definer = this.content.define(new VariantEnv(this));

		return this.content;
	}

	@Override
	protected FieldDefinition reproduceDefinition(Reproducer reproducer) {
		return new ReproducedObjectDefinition(this, reproducer);
	}

	Ascendants buildAscendants(
			Ascendants implicitAscendants,
			Ascendants ascendants) {
		if (!getDeclaration().isLink() && !getDeclaration().isVariable()) {

			final ObjectDefinerImpl definer =
					new ObjectDefinerImpl(this, implicitAscendants, ascendants);

			getDefinition().setImplicitAscendants(implicitAscendants);
			if (getField().isOverride()) {
				getDefinition().overrideObject(definer);
			} else {
				getDefinition().defineObject(definer);
			}

			return this.ascendants = definer.getAscendants();
		}

		final LinkDefinerImpl definer =
				new LinkDefinerImpl(this, ascendants);

		getDefinition().defineLink(definer);

		return this.ascendants = definer.getAscendants();
	}

	void declareMembers() {
		getContent().executeInstructions();
	}

	Definitions define(Definitions definitions, Scope scope) {

		final Definitions variantDefinitions =
				getContentDefiner().define(scope);

		if (variantDefinitions == null) {
			return definitions;
		}
		if (definitions == null) {
			return variantDefinitions;
		}

		return definitions.refine(variantDefinitions);
	}

	private Definer getContentDefiner() {
		if (this.definer == null) {
			getContent();
		}
		return this.definer;
	}

	final DeclaredObjectField getObjectField() {
		return (DeclaredObjectField) getField();
	}

	private static final class VariantEnv extends StatementEnv {

		private final ObjectFieldVariant variant;
		private ValueStruct<?, ?> expectedValueStruct;

		VariantEnv(ObjectFieldVariant variant) {
			this.variant = variant;
		}

		@Override
		public boolean hasPrerequisite() {
			return this.variant.getInitialEnv().hasPrerequisite();
		}

		@Override
		public Logical prerequisite(Scope scope) {
			return this.variant.getInitialEnv().prerequisite(scope);
		}

		@Override
		public boolean hasPrecondition() {
			return this.variant.getInitialEnv().hasPrecondition();
		}

		@Override
		public Logical precondition(Scope scope) {
			return this.variant.getInitialEnv().precondition(scope);
		}

		@Override
		protected ValueStruct<?, ?> expectedValueStruct() {
			if (this.expectedValueStruct != null) {
				return this.expectedValueStruct;
			}

			final ValueStruct<?, ?> ancestorValueStruct =
					this.variant.getField().toObject().value().getValueStruct();

			if (!ancestorValueStruct.isScoped()) {
				return this.expectedValueStruct = ancestorValueStruct;
			}

			return this.expectedValueStruct =
					ancestorValueStruct.upgradeScope(this.variant.getField());
		}

	}

}

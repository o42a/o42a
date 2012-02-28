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

import static org.o42a.analysis.use.User.dummyUser;

import org.o42a.core.Container;
import org.o42a.core.Namespace;
import org.o42a.core.Scope;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.*;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectType;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.Definer;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.StatementEnv;
import org.o42a.core.st.sentence.BlockBuilder;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.ValueStruct;


final class ObjectFieldVariant
		extends FieldVariant<Obj>
		implements ObjectDefiner {

	private DeclarativeBlock content;
	private Ascendants implicitAscendants;
	private Ascendants ascendants;
	private Definer definer;

	public ObjectFieldVariant(
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
	public final Ascendants getImplicitAscendants() {
		return this.implicitAscendants;
	}

	@Override
	public ObjectDefiner setAncestor(TypeRef explicitAncestor) {
		this.ascendants = this.ascendants.setAncestor(explicitAncestor);
		return this;
	}

	@Override
	public ObjectDefiner setTypeParameters(TypeParameters typeParameters) {
		this.ascendants = this.ascendants.setTypeParameters(typeParameters);
		return this;
	}

	@Override
	public ObjectDefiner addExplicitSample(StaticTypeRef explicitAscendant) {
		this.ascendants = this.ascendants.addExplicitSample(explicitAscendant);
		return this;
	}

	@Override
	public ObjectDefiner addImplicitSample(StaticTypeRef implicitAscendant) {
		this.ascendants = this.ascendants.addImplicitSample(implicitAscendant);
		return this;
	}

	@Override
	public ObjectDefiner addMemberOverride(Member overriddenMember) {
		this.ascendants = this.ascendants.addMemberOverride(overriddenMember);
		return this;
	}

	@Override
	public void define(BlockBuilder definitions) {
		definitions.buildBlock(getContent());
	}

	@Override
	protected FieldDefinition reproduceDefinition(Reproducer reproducer) {
		return new ReproducedObjectDefinition(this, reproducer);
	}

	Ascendants buildAscendants(
			Ascendants implicitAscendants,
			Ascendants ascendants) {
		this.implicitAscendants = implicitAscendants;
		this.ascendants = ascendants;
		getDefinition().defineObject(this);
		return this.ascendants;
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

	private final DeclaredObjectField getObjectField() {
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

			final ObjectType objectType =
					this.variant.getField().getArtifact().type();
			final Obj ancestorObject =
					objectType.getAncestor().typeObject(dummyUser());
			final ValueStruct<?, ?> ancestorValueStruct =
					ancestorObject.value().getValueStruct();

			if (!ancestorValueStruct.isScoped()) {
				return this.expectedValueStruct = ancestorValueStruct;
			}

			return this.expectedValueStruct =
					ancestorValueStruct.upgradeScope(this.variant.getField());
		}

	}

}

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

import org.o42a.core.LocationSpec;
import org.o42a.core.artifact.StaticTypeRef;
import org.o42a.core.def.Definitions;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.*;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.DefinitionTarget;
import org.o42a.core.st.sentence.*;


final class ObjectFieldVariantDecl extends FieldVariantDecl<Obj> {

	private Block<Declaratives> content;

	ObjectFieldVariantDecl(
			FieldDecl<Obj> fieldDecl,
			FieldVariant<Obj> variant) {
		super(fieldDecl, variant);
	}

	@Override
	public ObjectFieldDecl getFieldDecl() {
		return (ObjectFieldDecl) super.getFieldDecl();
	}

	public Block<Declaratives> getContent() {
		if (this.content == null) {
			buildContent();
		}
		return this.content;
	}

	@Override
	protected void init() {
		getContent();
	}

	@Override
	protected void declareMembers() {
		getContent().executeInstructions();
	}

	@Override
	protected Definitions define(DefinitionTarget target) {
		return getContent().define(target);
	}

	private void buildContent() {

		final FieldVariant<Obj> variant = getVariant();
		final MemberRegistry memberRegistry;
		final StaticTypeRef ascendant =
			getVariant().getDefinition().getAscendants().getAscendant();

		if (ascendant == null) {
			memberRegistry = getFieldDecl().getMemberRegistry();
		} else if (ascendant.getType().derivedFrom(getField().getArtifact())) {
			memberRegistry = getFieldDecl().getMemberRegistry();
		} else {
			memberRegistry = new ScopedRegistry(
					getFieldDecl().getMemberRegistry(),
					ascendant);
		}

		final FieldDefinition definition = variant.getDefinition();

		this.content = new DeclarativeBlock(
				definition,
				getField(),
				null,
				memberRegistry);
		this.content.setConditions(getVariant().getInitialConditions());

		final BlockBuilder declarations = definition.getDeclarations();

		if (declarations != null) {
			declarations.buildBlock(this.content);
		} else {

			final Ref value = definition.getValue();

			if (value != null) {
				this.content.propose(value).alternative(value).assign(value);
			}
		}
	}

	private static final class ScopedRegistry extends MemberRegistry {

		private final MemberRegistry registry;
		private final StaticTypeRef declaredIn;

		ScopedRegistry(MemberRegistry registry, StaticTypeRef declaredIn) {
			this.registry = registry;
			this.declaredIn = declaredIn;
		}

		@Override
		public Obj getOwner() {
			return this.registry.getOwner();
		}

		@Override
		public FieldVariant<?> declareField(FieldDeclaration declaration, FieldDefinition definition) {
			if (declaration.getDeclaredIn() != null) {
				return this.registry.declareField(declaration, definition);
			}
			if (!declaration.isOverride()) {
				return this.registry.declareField(declaration, definition);
			}
			return this.registry.declareField(
					new FieldDeclaration(
							declaration,
							declaration.distribute(),
							declaration).setDeclaredIn(this.declaredIn), definition);
		}

		@Override
		public void declareMember(Member member) {
			this.registry.declareMember(member);
		}

		@Override
		public boolean declareBlock(LocationSpec location, String name) {
			return this.registry.declareBlock(location, name);
		}

		@Override
		public String anonymousBlockName() {
			return this.registry.anonymousBlockName();
		}

	}

}

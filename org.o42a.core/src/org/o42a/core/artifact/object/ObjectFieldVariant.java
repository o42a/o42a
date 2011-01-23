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
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.member.field.*;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.DefinitionTarget;
import org.o42a.core.st.sentence.*;


final class ObjectFieldVariant extends FieldVariant<Obj> {

	private Block<Declaratives> content;

	public ObjectFieldVariant(
			DeclaredObjectField field,
			FieldDeclaration declaration,
			FieldDefinition definition) {
		super(field, declaration, definition);
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

		final MemberRegistry memberRegistry;
		final StaticTypeRef ascendant =
			getDefinition().getAscendants().getAscendant();

		if (ascendant == null) {
			memberRegistry = getObjectField().getMemberRegistry();
		} else if (ascendant.getType().derivedFrom(getField().getArtifact())) {
			memberRegistry = getObjectField().getMemberRegistry();
		} else {
			memberRegistry = new ScopedRegistry(
					getObjectField().getMemberRegistry(),
					ascendant);
		}

		final FieldDefinition definition = getDefinition();

		this.content = new DeclarativeBlock(
				definition,
				getField(),
				null,
				memberRegistry);
		this.content.setConditions(getInitialConditions());

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

	private final DeclaredObjectField getObjectField() {
		return (DeclaredObjectField) getField();
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
		public FieldBuilder newField(
				FieldDeclaration declaration,
				FieldDefinition definition) {
			if (declaration.getDeclaredIn() != null) {
				return this.registry.newField(declaration, definition);
			}
			if (!declaration.isOverride()) {
				return this.registry.newField(declaration, definition);
			}
			return this.registry.newField(
					new FieldDeclaration(
							declaration,
							declaration.distribute(),
							declaration).setDeclaredIn(this.declaredIn),
					definition);
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

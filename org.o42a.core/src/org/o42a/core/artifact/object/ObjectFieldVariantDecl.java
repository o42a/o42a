/*
    Compiler Core
    Copyright (C) 2010 Ruslan Lopatin

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
import org.o42a.core.Scope;
import org.o42a.core.artifact.StaticTypeRef;
import org.o42a.core.def.Definitions;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.*;
import org.o42a.core.ref.Cond;
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
	protected Cond condition(Scope scope) {
		return getContent().condition(scope);
	}

	@Override
	protected Definitions define(DefinitionTarget target) {
		return getContent().define(target);
	}

	@Override
	protected void init() {
		getContent();
	}

	private void buildContent() {

		final FieldVariant<Obj> variant = getVariant();
		final MemberRegistry fieldRegistry;
		final StaticTypeRef ascendant =
			getVariant().getDefinition().getAscendants().getAscendant();

		if (ascendant == null) {
			fieldRegistry = getFieldDecl().getFieldRegistry();
		} else if (ascendant.getType().derivedFrom(getField().getArtifact())) {
			fieldRegistry = getFieldDecl().getFieldRegistry();
		} else {
			fieldRegistry = new ScopedRegistry(
					getFieldDecl().getFieldRegistry(),
					ascendant);
		}

		final Statements<?> enclosing = variant.getEnclosing();
		final FieldDefinition definition = variant.getDefinition();

		if (enclosing == null) {
			this.content = getFieldDecl().getDefinitionBlock();
		} else {
			this.content = new DeclarativeBlock(
					definition,
					getField(),
					enclosing,
					fieldRegistry);
		}

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
		public DeclaredField<?> declareField(FieldDeclaration declaration) {
			if (declaration.getDeclaredIn() != null) {
				return this.registry.declareField(declaration);
			}
			if (!declaration.isOverride()) {
				return this.registry.declareField(declaration);
			}
			return this.registry.declareField(
					new FieldDeclaration(
							declaration,
							declaration.distribute(),
							declaration).setDeclaredIn(this.declaredIn));
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

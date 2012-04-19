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
package org.o42a.core.member.field.decl;

import static org.o42a.core.member.Inclusions.noInclusions;

import org.o42a.core.Container;
import org.o42a.core.Namespace;
import org.o42a.core.Scope;
import org.o42a.core.member.Inclusions;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.member.field.impl.FieldInclusions;
import org.o42a.core.object.Obj;
import org.o42a.core.object.common.ObjectMemberRegistry;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.object.type.FieldAscendants;
import org.o42a.core.ref.Logical;
import org.o42a.core.st.DefinerEnv;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.st.sentence.MainDefiner;
import org.o42a.core.value.ValueStruct;


public final class DeclaredField extends Field implements FieldAscendants {

	private final FieldDefinition definition;
	private Ascendants ascendants;
	private Registry memberRegistry;
	private DeclarativeBlock content;
	private MainDefiner definer;
	private boolean invalid;

	DeclaredField(DeclaredMemberField member, FieldDefinition definition) {
		super(member);
		this.definition = definition;
	}

	public final FieldDefinition getDefinition() {
		return this.definition;
	}

	public final Ascendants getAscendants() {
		return this.ascendants;
	}

	@Override
	public boolean isLinkAscendants() {
		return getDefinition().getLinkDepth() > 0;
	}

	@Override
	public Ascendants updateAscendants(Ascendants ascendants) {
		this.ascendants = ascendants;
		return this.ascendants = buildAscendants(ascendants);
	}

	@Override
	public Obj toObject() {
		if (getScopeObject() == null) {
			if (!getKey().isValid()) {

				final Obj falseObject = getContext().getFalse();

				setScopeObject(falseObject);
			} else {
				setScopeObject(new DeclaredObject(this));
			}
		}

		return getScopeObject();
	}

	public final boolean ownsCompilerContext() {

		final Scope enclosingScope = getEnclosingScope();
		final Member enclosingMember = enclosingScope.toMember();

		if (enclosingMember == null) {
			return enclosingScope.getContext() != getContext();
		}

		return enclosingMember.getContext() != getContext();
	}

	public final Inclusions newInclusions() {
		if (!ownsCompilerContext()) {
			return noInclusions();
		}
		return new FieldInclusions(this);
	}

	public DeclarativeBlock getContent() {
		if (this.content != null) {
			return this.content;
		}

		final Container container;

		if (ownsCompilerContext()) {
			container = new Namespace(getDefinition(), getContainer());
		} else {
			container = getContainer();
		}

		this.content = new DeclarativeBlock(
				container,
				container,
				getMemberRegistry());
		this.definer = this.content.define(new DeclarationEnv(this));

		return this.content;
	}

	void initDefinitions(Obj object) {

		final Ascendants ascendants =
				new Ascendants(object).declareField(NO_FIELD_ASCENDANTS);

		getDefinition().setImplicitAscendants(ascendants);
	}

	ObjectMemberRegistry getMemberRegistry() {
		if (this.memberRegistry == null) {
			this.memberRegistry = new Registry();
		}
		return this.memberRegistry;
	}

	Definitions define(Scope scope) {
		return getContentDefiner().createDefinitions();
	}

	void updateMembers() {
		getContent().executeInstructions();
	}

	final void invalid() {
		this.invalid = true;
	}

	final boolean validate() {
		if (!getDefinition().isValid()) {
			return false;
		}
		return !this.invalid;
	}

	private final DeclaredMemberField member() {
		return (DeclaredMemberField) toMember();
	}

	private MainDefiner getContentDefiner() {
		if (this.definer == null) {
			getContent();
		}
		return this.definer;
	}

	private final DefinerEnv getInitialEnv() {
		return member().getStatement().getInitialEnv();
	}

	private Ascendants buildAscendants(Ascendants implicitAscendants) {
		if (getDeclaration().getLinkType() == null) {

			final ObjectDefinerImpl definer =
					new ObjectDefinerImpl(this, implicitAscendants);

			getDefinition().setImplicitAscendants(implicitAscendants);
			if (isOverride()) {
				getDefinition().overrideObject(definer);
			} else {
				getDefinition().defineObject(definer);
			}

			return this.ascendants = definer.getAscendants();
		}

		final LinkDefinerImpl definer =
				new LinkDefinerImpl(this, implicitAscendants);

		getDefinition().defineLink(definer);

		return this.ascendants = definer.getAscendants();
	}

	private final class Registry extends ObjectMemberRegistry {

		Registry() {
			super(newInclusions());
		}

		@Override
		public Obj getOwner() {

			final Obj owner = super.getOwner();

			if (owner != null) {
				return owner;
			}
			return setOwner(toObject());
		}

	}

	private static final class DeclarationEnv extends DefinerEnv {

		private final DeclaredField field;
		private ValueStruct<?, ?> expectedValueStruct;

		DeclarationEnv(DeclaredField field) {
			this.field = field;
		}

		@Override
		public boolean hasPrerequisite() {
			return this.field.getInitialEnv().hasPrerequisite();
		}

		@Override
		public Logical prerequisite(Scope scope) {
			return this.field.getInitialEnv().prerequisite(scope);
		}

		@Override
		public boolean hasPrecondition() {
			return this.field.getInitialEnv().hasPrecondition();
		}

		@Override
		public Logical precondition(Scope scope) {
			return this.field.getInitialEnv().precondition(scope);
		}

		@Override
		protected ValueStruct<?, ?> expectedValueStruct() {
			if (this.expectedValueStruct != null) {
				return this.expectedValueStruct;
			}

			final ValueStruct<?, ?> ancestorValueStruct =
					this.field.toObject().value().getValueStruct();

			return this.expectedValueStruct = ancestorValueStruct;
		}

	}

}

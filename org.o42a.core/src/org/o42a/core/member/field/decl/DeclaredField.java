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
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.DefinerEnv;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.st.sentence.MainDefiner;
import org.o42a.core.value.ValueRequest;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;


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
		return getDefinition().getDefinitionTarget().isLink();
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
				setScopeObject(getContext().getNone());
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

	final boolean initDefinition(Obj object) {

		final Ascendants ascendants =
				new Ascendants(object).declareField(NO_FIELD_ASCENDANTS);
		final FieldDefinition definition = getDefinition();

		definition.init(this, ascendants);

		return definition.isValid();
	}

	final ObjectMemberRegistry getMemberRegistry() {
		if (this.memberRegistry == null) {
			this.memberRegistry = new Registry();
		}
		return this.memberRegistry;
	}

	final Definitions createDefinitions() {
		return getContentDefiner().createDefinitions();
	}

	final void updateMembers() {
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

	private MainDefiner getContentDefiner() {
		if (this.definer == null) {
			getContent();
		}
		return this.definer;
	}

	private Ascendants buildAscendants(Ascendants implicitAscendants) {
		if (getDeclaration().isMacro()) {

			final Ascendants macroAscendants =
					macroAscendants(implicitAscendants);

			if (macroAscendants != null) {
				return this.ascendants = macroAscendants;
			}
		}

		if (getDeclaration().getLinkType() == null) {
			return this.ascendants = objectAscendants(implicitAscendants);
		}

		return this.ascendants = linkAscendants(implicitAscendants);
	}

	private Ascendants macroAscendants(Ascendants implicitAscendants) {

		final boolean needAncestor;
		final TypeRef ancestor = implicitAscendants.getAncestor();

		if (ancestor == null) {
			needAncestor = true;
		} else {
			if (!ancestor.getValueType().isMacro()) {
				getLogger().error(
						"not_macro_field",
						getDeclaration(),
						"Not a macro field");
				invalid();
				return null;
			}
			needAncestor = false;
		}

		final MacroDefinerImpl definer = new MacroDefinerImpl(this);

		getDefinition().defineMacro(definer);

		if (!needAncestor) {
			return implicitAscendants;
		}

		return implicitAscendants.setAncestor(
				ValueType.MACRO.typeRef(getDeclaration(), getEnclosingScope()));
	}

	private Ascendants objectAscendants(Ascendants implicitAscendants) {

		final ObjectDefinerImpl definer =
				new ObjectDefinerImpl(this, implicitAscendants);

		if (isOverride()) {
			getDefinition().overrideObject(definer);
		} else {
			getDefinition().defineObject(definer);
		}

		return definer.getAscendants();
	}

	private Ascendants linkAscendants(Ascendants implicitAscendants) {

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

		DeclarationEnv(DeclaredField field) {
			this.field = field;
		}

		@Override
		protected ValueRequest buildValueRequest() {

			final ValueStruct<?, ?> ancestorValueStruct =
					this.field.toObject().value().getValueStruct();

			return new ValueRequest(ancestorValueStruct);
		}

	}

}

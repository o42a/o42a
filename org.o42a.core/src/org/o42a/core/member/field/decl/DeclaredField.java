/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import java.util.function.Function;

import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.DefinitionsBuilder;
import org.o42a.core.object.def.ObjectToDefine;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.object.type.FieldAscendants;


public final class DeclaredField extends Field implements FieldAscendants {

	private final FieldDefinition definition;
	private Ascendants ascendants;
	private Function<ObjectToDefine, DefinitionsBuilder> definitions;
	private boolean stateful;
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

	public final boolean isStateful() {
		return this.stateful;
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
	protected Obj createObject() {
		if (!getKey().isValid()) {
			return getContext().getNone();
		}
		return new DeclaredObject(this);
	}

	final boolean initDefinition(Obj object) {

		final Ascendants ascendants =
				new Ascendants(object).declareField(NO_FIELD_ASCENDANTS);
		final FieldDefinition definition = getDefinition();

		definition.init(this, ascendants);

		return definition.isValid();
	}

	final Function<ObjectToDefine, DefinitionsBuilder> getDefinitions() {
		return this.definitions;
	}

	final void addDefinitions(
			Function<ObjectToDefine, DefinitionsBuilder> definitions) {
		assert this.definitions == null :
			"Field already defined";
		this.definitions = definitions;
	}

	final void makeStateful() {
		this.stateful = true;
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

	private Ascendants buildAscendants(Ascendants implicitAscendants) {
		if (getDeclaration().isMacro()) {

			final Ascendants macroAscendants =
					macroAscendants(implicitAscendants);

			if (macroAscendants != null) {
				return this.ascendants = macroAscendants;
			}
		}

		return this.ascendants = objectAscendants(implicitAscendants);
	}

	private Ascendants macroAscendants(Ascendants implicitAscendants) {

		final MacroDefinerImpl definer =
				new MacroDefinerImpl(this, implicitAscendants);

		getDefinition().defineMacro(definer);

		return definer.getAscendants();
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

}

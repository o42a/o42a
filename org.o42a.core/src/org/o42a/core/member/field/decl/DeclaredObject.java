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

import static org.o42a.core.member.Inclusions.INCLUSIONS;
import static org.o42a.core.member.Inclusions.NO_INCLUSIONS;
import static org.o42a.core.object.def.DefinitionsBuilder.NO_DEFINITIONS_BUILDER;

import java.util.function.Function;

import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectMembers;
import org.o42a.core.object.common.ObjectMemberRegistry;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.def.DefinitionsBuilder;
import org.o42a.core.object.def.ObjectToDefine;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.object.value.Statefulness;
import org.o42a.core.st.CommandEnv;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.ValueRequest;


class DeclaredObject extends Obj implements ObjectToDefine {

	private final DeclaredField field;
	private Registry memberRegistry;
	private DefinitionsBuilder definitionsBuilder;

	DeclaredObject(DeclaredField field) {
		super(field);
		this.field = field;
	}

	@Override
	public boolean isValid() {
		return this.field.validate();
	}

	@Override
	public Registry getMemberRegistry() {
		if (this.memberRegistry == null) {
			this.memberRegistry = new Registry();
		}
		return this.memberRegistry;
	}

	@Override
	public CommandEnv definitionsEnv() {
		return new DeclarationEnv(this.field);
	}

	@Override
	public String toString() {
		return this.field != null ? this.field.toString() : super.toString();
	}

	@Override
	protected Nesting createNesting() {
		return this.field.toMember().getNesting();
	}

	@Override
	protected Ascendants buildAscendants() {
		this.field.initDefinition(this);
		return new Ascendants(this).declareField(this.field);
	}

	@Override
	protected Statefulness determineStatefulness() {
		return super.determineStatefulness()
				.setStateful(this.field.isStateful())
				.setEager(this.field.isEager());
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
		getMemberRegistry().registerMembers(members);
	}

	@Override
	protected void updateMembers() {
		getDefinitionsBuilder().updateMembers();
	}

	@Override
	protected Definitions explicitDefinitions() {
		return getDefinitionsBuilder().buildDefinitions();
	}

	private DefinitionsBuilder getDefinitionsBuilder() {
		if (this.definitionsBuilder != null) {
			return this.definitionsBuilder;
		}

		final Function<ObjectToDefine, DefinitionsBuilder> definitions =
				this.field.getDefinitions();

		if (definitions == null) {
			return this.definitionsBuilder = NO_DEFINITIONS_BUILDER;
		}

		return this.definitionsBuilder = definitions.apply(this);
	}

	private final class Registry extends ObjectMemberRegistry {

		Registry() {
			super(
					getScope().ownsCompilerContext()
					? INCLUSIONS : NO_INCLUSIONS);
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

	private static final class DeclarationEnv extends CommandEnv {

		private final DeclaredField field;
		private ValueRequest valueRequest;

		DeclarationEnv(DeclaredField field) {
			this.field = field;
		}

		@Override
		public ValueRequest getValueRequest() {
			if (this.valueRequest != null) {
				return this.valueRequest;
			}

			final TypeParameters<?> ancestorParameters =
					this.field.toObject().type().getParameters();

			return this.valueRequest = new ValueRequest(
					ancestorParameters,
					this.field.getContext().getLogger());
		}

	}

}

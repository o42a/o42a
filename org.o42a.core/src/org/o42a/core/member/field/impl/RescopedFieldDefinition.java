/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.core.member.field.impl;

import java.util.function.Function;

import org.o42a.core.member.Member;
import org.o42a.core.member.field.*;
import org.o42a.core.object.def.DefinitionsBuilder;
import org.o42a.core.object.def.ObjectToDefine;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.value.ObjectTypeParameters;


public final class RescopedFieldDefinition extends FieldDefinition {

	private final PrefixPath prefix;
	private final FieldDefinition definition;

	public RescopedFieldDefinition(
			FieldDefinition definition,
			PrefixPath prefix) {
		super(
				definition,
				definition.distributeIn(prefix.getStart().getContainer()));
		this.prefix = prefix;
		this.definition = definition;
	}

	@Override
	public void init(Field field, Ascendants implicitAscendants) {
		this.definition.init(field, implicitAscendants);
	}

	@Override
	public DefinitionTarget getDefinitionTarget() {
		return this.definition.getDefinitionTarget();
	}

	@Override
	public void defineObject(ObjectDefiner definer) {
		this.definition.defineObject(new RescopedObjectDefiner(definer));
	}

	@Override
	public void overrideObject(ObjectDefiner definer) {
		this.definition.overrideObject(new RescopedObjectDefiner(definer));
	}

	@Override
	public void defineMacro(MacroDefiner definer) {
		this.definition.defineMacro(new RescopedMacroDefiner(definer));
	}

	@Override
	public FieldDefinition prefixWith(PrefixPath prefix) {

		final PrefixPath oldPrefix = this.prefix;
		final PrefixPath newPrefix = oldPrefix.and(prefix);

		if (oldPrefix == newPrefix) {
			return this;
		}

		return new RescopedFieldDefinition(this.definition, newPrefix);
	}

	@Override
	public String toString() {
		return this.definition.toString();
	}

	private final PrefixPath getPrefix() {
		return this.prefix;
	}

	private final class RescopedObjectDefiner implements ObjectDefiner {

		private final ObjectDefiner definer;

		RescopedObjectDefiner(ObjectDefiner definer) {
			this.definer = definer;
		}

		@Override
		public Field getField() {
			return this.definer.getField();
		}

		@Override
		public Ascendants getImplicitAscendants() {
			return this.definer.getImplicitAscendants();
		}

		@Override
		public void makeStateful() {
			this.definer.makeStateful();
		}

		@Override
		public ObjectDefiner setAncestor(TypeRef ancestor) {
			this.definer.setAncestor(ancestor.prefixWith(getPrefix()));
			return this;
		}

		@Override
		public ObjectDefiner setParameters(ObjectTypeParameters parameters) {
			this.definer.setParameters(parameters.prefixWith(getPrefix()));
			return this;
		}

		@Override
		public ObjectDefiner addImplicitSample(
				StaticTypeRef implicitAscendant,
				TypeRef overriddenAncestor) {
			this.definer.addImplicitSample(
					implicitAscendant.prefixWith(getPrefix()),
					overriddenAncestor.prefixWith(getPrefix()));
			return this;
		}

		@Override
		public ObjectDefiner addMemberOverride(Member overriddenMember) {
			this.definer.addMemberOverride(overriddenMember);
			return this;
		}

		@Override
		public void define(
				Function<ObjectToDefine, DefinitionsBuilder> definitions) {
			this.definer.define(definitions);
		}

		@Override
		public String toString() {
			if (this.definer == null) {
				return super.toString();
			}
			return this.definer.toString();
		}

	}

	private final class RescopedMacroDefiner implements MacroDefiner {

		private final MacroDefiner definer;

		RescopedMacroDefiner(MacroDefiner definer) {
			this.definer = definer;
		}

		@Override
		public Field getField() {
			return this.definer.getField();
		}

		@Override
		public void makeStateful() {
			this.definer.makeStateful();
		}

		@Override
		public void setRef(Ref ref) {
			this.definer.setRef(ref.prefixWith(getPrefix()));
		}

		@Override
		public void define(
				Function<ObjectToDefine, DefinitionsBuilder> definitions) {
			this.definer.define(definitions);
		}

		@Override
		public String toString() {
			if (this.definer == null) {
				return super.toString();
			}
			return this.definer.toString();
		}

	}

}

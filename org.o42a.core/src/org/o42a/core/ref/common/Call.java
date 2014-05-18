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
package org.o42a.core.ref.common;

import java.util.function.Function;

import org.o42a.core.Distributor;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.object.common.DefinedObject;
import org.o42a.core.object.def.DefinitionsBuilder;
import org.o42a.core.object.def.ObjectToDefine;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.object.value.Statefulness;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.ObjectConstructor;
import org.o42a.core.ref.path.PathReproducer;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;


public class Call extends ObjectConstructor {

	private final AscendantsDefinition ascendants;
	private final Function<ObjectToDefine, DefinitionsBuilder> definitions;

	public Call(
			LocationInfo location,
			Distributor distributor,
			AscendantsDefinition ascendants,
			Function<ObjectToDefine, DefinitionsBuilder> definitions) {
		super(location, distributor, ascendants.isStateful());
		this.ascendants = ascendants;
		this.definitions = definitions;
	}

	public final AscendantsDefinition getAscendants() {
		return this.ascendants;
	}

	public final Function<ObjectToDefine, DefinitionsBuilder> getDefinitions() {
		return this.definitions;
	}

	@Override
	public TypeRef ancestor(LocationInfo location, Ref ref) {
		return getAscendants().getAncestor()
				.setParameters(toSynthetic().toRef().typeParameters());
	}

	@Override
	public TypeRef iface(Ref ref) {
		return ancestor(ref, ref);
	}

	@Override
	public FieldDefinition fieldDefinition(Ref ref) {
		return getAscendants().fieldDefinition(this, getDefinitions());
	}

	@Override
	public Call reproduce(PathReproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final AscendantsDefinition ascendants =
				getAscendants().reproduce(reproducer.getReproducer());

		if (ascendants == null) {
			return null;
		}

		return new Call(
				this,
				reproducer.distribute(),
				ascendants,
				getDefinitions());
	}

	@Override
	public String toString() {
		if (this.ascendants == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder();

		out.append(this.ascendants).append('(');
		if (this.definitions != null) {
			out.append(this.definitions);
		}
		out.append(')');

		return out.toString();
	}

	@Override
	protected ObjectConstructor createStateful() {

		final AscendantsDefinition oldAscendants = getAscendants();
		final AscendantsDefinition newAscendants =
				oldAscendants.setStateful(true);

		if (oldAscendants == newAscendants) {
			return this;
		}

		return new Call(this, distribute(), newAscendants, getDefinitions());
	}

	@Override
	protected Obj createObject() {
		return new CallObject(this, distribute());
	}

	private static final class CallObject extends DefinedObject {

		private final Call call;

		CallObject(Call call, Distributor enclosing) {
			super(call, enclosing);
			this.call = call;
		}

		@Override
		public String toString() {
			if (this.call == null) {
				return super.toString();
			}
			return this.call.toString();
		}

		@Override
		protected Nesting createNesting() {
			return this.call.getNesting();
		}

		@Override
		protected Ascendants buildAscendants() {
			return this.call.getAscendants().updateAscendants(
					new Ascendants(this));
		}

		@Override
		protected Statefulness determineStatefulness() {
			return super.determineStatefulness()
					.setStateful(this.call.getAscendants().isStateful());
		}

		@Override
		protected DefinitionsBuilder createDefinitionsBuilder() {
			return definitionsBuilder(this.call.getDefinitions());
		}


	}

}

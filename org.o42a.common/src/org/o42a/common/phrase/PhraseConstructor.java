/*
    Compiler Commons
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
package org.o42a.common.phrase;

import org.o42a.core.Distributor;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.object.common.DefinedObject;
import org.o42a.core.object.def.DefinitionsBuilder;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.object.value.Statefulness;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.ObjectConstructor;
import org.o42a.core.ref.path.PathReproducer;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;


class PhraseConstructor extends ObjectConstructor {

	private final Phrase phrase;
	private final AscendantsDefinition ascendants;

	PhraseConstructor(Phrase phrase) {
		super(phrase, phrase.distribute());
		this.phrase = phrase;
		this.ascendants = phrase.getMainContext().getAscendants();
		this.ascendants.assertCompatibleScope(this);
	}

	private PhraseConstructor(
			LocationInfo location,
			Distributor distributor,
			Phrase phrase,
			AscendantsDefinition ascendants) {
		super(location, distributor);
		this.phrase = phrase;
		this.ascendants = ascendants;
	}

	@Override
	public TypeRef ancestor(LocationInfo location, Ref ref) {
		return this.ascendants.getAncestor();
	}

	@Override
	public TypeRef iface(Ref ref) {
		return ancestor(ref, ref)
				.setParameters(toSynthetic().toRef().typeParameters());
	}

	@Override
	public FieldDefinition fieldDefinition(Ref ref) {

		final PhraseFieldDefinition definition =
				new PhraseFieldDefinition(this.phrase);
		final PrefixPath prefix =
				ref.getPath().cut(1).toPrefix(ref.getScope());

		return definition.prefixWith(prefix);
	}

	@Override
	public PhraseConstructor reproduce(PathReproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final AscendantsDefinition ascendants =
				this.ascendants.reproduce(reproducer.getReproducer());

		if (ascendants == null) {
			return null;
		}

		return new PhraseConstructor(
				this,
				reproducer.distribute(),
				this.phrase,
				ascendants);
	}

	@Override
	public String toString() {
		if (this.phrase == null) {
			return super.toString();
		}
		return this.phrase.toString();
	}

	@Override
	protected Obj createObject() {
		return new PhraseObject(this);
	}

	private static final class PhraseObject extends DefinedObject {

		private final PhraseConstructor constructor;

		private PhraseObject(PhraseConstructor constructor) {
			super(
					constructor.phrase,
					constructor.distribute());
			this.constructor = constructor;
		}

		@Override
		public String toString() {
			if (this.constructor == null) {
				return super.toString();
			}
			return this.constructor.phrase.toString();
		}

		@Override
		protected Nesting createNesting() {
			return this.constructor.getNesting();
		}

		@Override
		protected Ascendants buildAscendants() {
			return this.constructor.ascendants.updateAscendants(
					new Ascendants(this));
		}

		@Override
		protected Statefulness determineStatefulness() {
			return super.determineStatefulness()
					.setEager(this.constructor.isEager());
		}

		@Override
		protected DefinitionsBuilder createDefinitionsBuilder() {
			return blockDefinitions(
					this.constructor.phrase.getMainContext()
					.getInstances()[0].getDefinition());
		}

	}

}

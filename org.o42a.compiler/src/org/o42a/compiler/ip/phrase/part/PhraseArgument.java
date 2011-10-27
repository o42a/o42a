/*
    Compiler
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
package org.o42a.compiler.ip.phrase.part;

import static org.o42a.compiler.ip.phrase.part.NextClause.terminatePhrase;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.compiler.ip.phrase.ref.Phrase;
import org.o42a.compiler.ip.phrase.ref.PhraseContext;
import org.o42a.core.Distributor;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Sample;
import org.o42a.core.member.clause.ClauseId;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.Statements;
import org.o42a.core.value.ValueType;


public class PhraseArgument extends PhraseContinuation {

	private final Ref value;

	PhraseArgument(PhrasePart preceding, LocationInfo location, Ref value) {
		super(location, preceding);
		this.value = value;
	}

	public final Ref getValue() {
		return this.value;
	}

	@Override
	public NextClause nextClause(PhraseContext context) {

		final NextClause next = context.clauseById(this, ClauseId.ARGUMENT);

		if (next.found()) {
			return next;
		}
		if (this.value == null) {
			return next;
		}
		if (!context.isObject()) {
			return next;
		}
		if (!isArray(context.getPhrase())) {
			return next;
		}

		return terminatePhrase(new GetArrayItem(), true);
	}

	@Override
	public Ref substitute(Distributor distributor) {
		if (this.value == null) {
			return null;
		}
		return this.value.rescope(distributor.getScope());
	}

	@Override
	public void define(Block<?> definition) {
		if (this.value == null) {
			return;// Do not assign any value.
		}

		final Statements<?> statements =
				definition.propose(this).alternative(this);

		statements.selfAssign(this.value);
	}

	@Override
	public String toString() {
		if (this.value == null) {
			return "[]";
		}
		return '[' + this.value.toString() + ']';
	}

	private boolean isArray(Phrase phrase) {

		final TypeRef ancestor = phrase.getAncestor();

		if (ancestor != null && isArray(ancestor)) {
			return true;
		}
		for (StaticTypeRef sample : phrase.getSamples()) {
			if (isArray(sample)) {
				return true;
			}
		}

		final Ascendants implicitAscendants = phrase.getImplicitAscendants();

		if (implicitAscendants == null) {
			return false;
		}

		final TypeRef implicitAncestor = implicitAscendants.getAncestor();

		if (implicitAncestor != null && isArray(implicitAncestor)) {
			return true;
		}

		for (Sample implicitSample : implicitAscendants.getSamples()) {
			if (isArray(implicitSample.getTypeRef())) {
				return true;
			}
		}

		return false;
	}

	private boolean isArray(TypeRef typeRef) {

		final ValueType<?> valueType =
				typeRef.typeObject(dummyUser()).value().getValueType();

		if (valueType == ValueType.VAR_ARRAY) {
			return true;
		}

		return valueType == ValueType.CONST_ARRAY;
	}

	private final class GetArrayItem implements PhraseTerminator {

		@Override
		public boolean requiresInstance() {
			return false;
		}

		@Override
		public Ref terminate(Ref prefix) {

			final BoundPath itemPath =
					prefix.getPath().arrayItem(PhraseArgument.this.value);

			return itemPath.target(prefix.distribute());
		}

		@Override
		public String toString() {
			return PhraseArgument.this.toString();
		}

	}

}

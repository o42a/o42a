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
import org.o42a.core.Scope;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.sentence.Statements;


final class ClauseAscendantsReproducer extends Reproducer {

	private final PhraseContext context;

	static AscendantsDefinition ascendants(
			LocationInfo location,
			Distributor distributor,
			PhraseContext context) {

		AscendantsDefinition ascendants = new AscendantsDefinition(
				location,
				distributor);

		final ClauseAscendantsReproducer ascendantsReproducer =
				new ClauseAscendantsReproducer(context, distributor);
		final AscendantsDefinition oldAscendants =
				context.getClause().toPlainClause().getAscendants();

		if (oldAscendants != null) {

			final TypeRef oldAncestor = oldAscendants.getAncestor();

			if (oldAncestor != null) {

				final TypeRef newAncestor =
						oldAncestor.reproduce(ascendantsReproducer);

				if (newAncestor == null) {
					return null;
				}

				ascendants = ascendants.setAncestor(newAncestor);
			}
		}

		return ascendants;
	}

	private ClauseAscendantsReproducer(
			PhraseContext context,
			Distributor distributor) {
		super(context.getClause().getEnclosingScope(), distributor);
		this.context = context;
	}

	@Override
	public boolean phraseCreatesObject() {
		return this.context.getMainContext().createsObject();
	}

	@Override
	public Ref getPhrasePrefix() {
		return this.context.getMainContext().getPhrase().getAncestor()
				.getRef();
	}

	@Override
	public MemberRegistry getMemberRegistry() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Statements<?> getStatements() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Reproducer reproducerOf(Scope reproducingScope) {
		if (reproducingScope.is(getReproducingScope())) {
			return this;
		}
		return null;
	}

	@Override
	public void applyClause(
			LocationInfo location,
			Statements<?> statements,
			Clause clause) {
		throw new UnsupportedOperationException();
	}

}

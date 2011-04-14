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
package org.o42a.compiler.ip.phrase;

import static org.o42a.compiler.ip.phrase.NextClause.clauseNotFound;
import static org.o42a.compiler.ip.phrase.NextClause.nextClause;
import static org.o42a.core.member.AdapterId.adapterId;

import org.o42a.compiler.ip.phrase.part.PhraseContinuation;
import org.o42a.core.*;
import org.o42a.core.member.AdapterId;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.clause.*;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.ref.path.Path;
import org.o42a.core.st.sentence.Block;
import org.o42a.util.ArrayUtil;


public abstract class PhraseContext {

	private final Phrase phrase;
	private final PhraseContext enclosing;
	private final LocationInfo location;
	private ClauseInstance[] instances;

	public PhraseContext(Phrase phrase, LocationInfo location) {
		assert location != null :
			"Location not specified";
		this.phrase = phrase;
		this.enclosing = null;
		this.location = location;
		this.instances = new ClauseInstance[] {new ClauseInstance(this)};
	}

	public PhraseContext(PhraseContext enclosing, LocationInfo location) {
		assert location != null :
			"Location not specified";
		this.phrase = enclosing.getPhrase();
		this.enclosing = enclosing;
		this.location = location;
		this.instances = new ClauseInstance[] {new ClauseInstance(this)};
	}

	public final Phrase getPhrase() {
		return this.phrase;
	}

	public final PhraseContext getEnclosing() {
		return this.enclosing;
	}

	public final LocationInfo getLocation() {
		return this.location;
	}

	public abstract Clause getClause();

	public final ClauseInstance[] getInstances() {
		return this.instances;
	}

	public abstract NextClause clauseByName(
			LocationInfo location,
			String name);

	public abstract NextClause clauseById(
			LocationInfo location,
			ClauseId clauseId);

	public abstract Path pathToObject(Scope scope);

	@Override
	public String toString() {

		final PhraseContinuation[] content = this.instances[0].getContent();

		if (content.length == 0) {
			return this.location.toString();
		}

		return content[0].toString();
	}

	protected final CompilerLogger getLogger() {
		return getPhrase().getLogger();
	}

	protected abstract void define(
			ClauseInstance instance,
			Block<?> definition);

	abstract MainPhraseContext getMainContext();

	final ClauseInstance incompleteInstance() {

		final ClauseInstance lastInstance = lastInstance();

		assert !lastInstance.isComplete() :
			"Instance already complete: " + lastInstance;

		return lastInstance;
	}

	final Iterable<? extends PhraseSubContext> subContexts() {
		return lastInstance().subContexts();
	}

	void reuse(LocationInfo location) {
		if (!lastInstance().isComplete()) {
			return;
		}

		final ClauseInstance newInstance = new ClauseInstance(this, location);

		this.instances = ArrayUtil.append(this.instances, newInstance);
	}

	final AdapterId clauseId(LocationInfo location, ClauseId clauseId) {
		return adapterId(clauseId.adapterType(
				location,
				getPhrase().distribute()));
	}

	NextClause findClause(
			ClauseContainer container,
			LocationInfo location,
			MemberId memberId) {
		if (container == null) {
			return clauseNotFound(memberId);
		}

		final NextClause found = findClauseIn(container, location, memberId);

		if (found.found()) {
			return found;
		}

		final Clause clause = container.toClause();

		if (clause == null) {
			return found;
		}

		return findReusedClause(clause, location, memberId);
	}

	final AscendantsDefinition ascendants(
			LocationInfo location,
			Distributor distributor) {
		return ClauseAscendantsReproducer.ascendants(
				location,
				distributor,
				this);
	}

	private NextClause findClauseIn(
			ClauseContainer container,
			LocationInfo location,
			MemberId memberId) {

		final Clause found = container.clause(memberId, null);

		if (found != null) {
			return nextClause(memberId, found, container.toClause());
		}

		for (Clause implicit : container.getImplicitClauses()) {

			final NextClause foundInImplicit =
				findClause(implicit.getClauseContainer(), location, memberId);

			if (foundInImplicit.found()) {
				return foundInImplicit.setImplicit(
						nextClause(
								implicit.getKey().getMemberId(),
								implicit,
								container.toClause()));
			}
		}

		return clauseNotFound(memberId);
	}

	private NextClause findReusedClause(
			Clause clause,
			LocationInfo location,
			MemberId memberId) {

		final ReusedClause[] reused = clause.getReusedClauses();

		for (int i = 0; i < reused.length; ++i) {

			final ReusedClause reusedClause = reused[i];
			final NextClause found;

			if (reusedClause.isObject()) {
				found = getMainContext().findObjectClause(
						location,
						memberId);
			} else {
				found = findClause(
						reusedClause.getClause().getClauseContainer(),
						location,
						memberId).setContainer(reusedClause.getContainer());
			}

			if (found.found()) {
				return found;
			}
		}

		return clauseNotFound(memberId);
	}

	private final ClauseInstance lastInstance() {
		return this.instances[this.instances.length - 1];
	}

}

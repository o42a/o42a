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

import static org.o42a.common.phrase.part.NextClause.clauseNotFound;
import static org.o42a.common.phrase.part.NextClause.nextClause;

import org.o42a.common.phrase.part.NextClause;
import org.o42a.common.phrase.part.PhraseContinuation;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.clause.*;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.Block;
import org.o42a.util.ArrayUtil;
import org.o42a.util.string.Name;


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

	public final boolean isObject() {
		return getClause() == null;
	}

	public final boolean isMain() {
		return this.phrase.getMainContext() == this;
	}

	public abstract Clause getClause();

	public final ClauseInstance[] getInstances() {
		return this.instances;
	}

	public abstract NextClause clauseByName(LocationInfo location, Name name);

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

	protected final CompilerLogger getResolutionLogger() {
		return getPhrase().getResolutionLogger();
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

	NextClause findClause(
			ClauseContainer container,
			LocationInfo location,
			MemberId memberId,
			Object what) {
		if (container == null) {
			return clauseNotFound(what);
		}

		final NextClause found =
				findClauseIn(container, location, memberId, what);

		if (found.found()) {
			return found;
		}

		final Clause clause = container.toClause();

		if (clause == null) {
			return found;
		}

		return findReusedClause(clause, location, memberId, what);
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
			MemberId memberId,
			Object what) {

		final MemberClause found = container.clause(memberId, null);

		if (found != null) {
			return nextClause(memberId, found.clause(), container.toClause());
		}

		for (MemberClause implicit : container.getImplicitClauses()) {

			final Clause implicitClause = implicit.clause();
			final NextClause foundInImplicit = findClause(
					implicitClause.getClauseContainer(),
					location,
					memberId,
					what);

			if (foundInImplicit.found()) {
				return foundInImplicit.setImplicit(
						nextClause(
								implicit.getMemberKey().getMemberId(),
								implicitClause,
								container.toClause()));
			}
		}

		return clauseNotFound(memberId);
	}

	private NextClause findReusedClause(
			Clause clause,
			LocationInfo location,
			MemberId memberId,
			Object what) {

		final ReusedClause[] reused = clause.getReusedClauses();

		for (int i = 0; i < reused.length; ++i) {

			final ReusedClause reusedClause = reused[i];
			final NextClause found;

			if (reusedClause.isObject()) {
				found = getMainContext().findObjectClause(
						location,
						memberId,
						what);
			} else if (reusedClause.reuseContents()) {
				found = findClause(
						reusedClause.getClause().clause().getClauseContainer(),
						location,
						memberId,
						what).setContainer(reusedClause.getContainer());
			} else {

				final MemberClause c = reusedClause.getClause();

				if (c.getMemberId().getLocalId().equals(memberId)) {
					found = nextClause(memberId, c.clause());
				} else {
					found = clauseNotFound(what);
				}
			}

			if (found.found()) {
				return found;
			}
		}

		return clauseNotFound(what);
	}

	private final ClauseInstance lastInstance() {
		return this.instances[this.instances.length - 1];
	}

}

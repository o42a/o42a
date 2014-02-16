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
import static org.o42a.core.member.MemberName.clauseName;

import java.util.LinkedList;

import org.o42a.common.phrase.part.*;
import org.o42a.core.Scope;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.clause.ClauseId;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.member.field.DefinitionTarget;
import org.o42a.core.object.Obj;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.object.type.Sample;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.Statements;
import org.o42a.core.value.ObjectTypeParameters;
import org.o42a.core.value.link.LinkValueType;
import org.o42a.util.string.Name;


final class MainPhraseContext extends PhraseContext {

	private int createsObject;
	private boolean standalone;
	private boolean firstClauseFound;
	private PhraseContext nextContext;
	private Ascendants implicitAscendants;
	private PhraseContinuation nextPart;

	MainPhraseContext(Phrase phrase) {
		super(phrase, phrase.getPrefix());
	}

	public final AscendantsDefinition getAscendants() {
		return getPhrase().getPrefix().getAscendants();
	}

	public final Ascendants getImplicitAscendants() {
		return this.implicitAscendants;
	}

	public final PhraseContinuation getNextPart() {
		return this.nextPart;
	}

	public boolean createsObject() {
		build();
		return this.createsObject > 0;
	}

	public Ref standalone() {
		this.standalone = true;

		if (this.nextContext == null) {
			return getPhrase().getPrefix().getAncestor().getRef();
		}

		final ClauseInstance[] instances = this.nextContext.getInstances();

		assert instances.length == 1 :
			"Wrong number of clause instances: " + instances.length;

		final ClauseInstance instance = instances[0];

		return instance.instantiateObject(getPhrase().distribute());
	}

	@Override
	public final Clause getClause() {
		return null;
	}

	@Override
	public NextClause clauseByName(LocationInfo location, Name name) {
		return findObjectClause(location, clauseName(name), name);
	}

	@Override
	public NextClause clauseById(LocationInfo location, ClauseId clauseId) {
		return findObjectClause(location, clauseId.getMemberId(), clauseId);
	}

	@Override
	public Path pathToObject(Scope scope) {
		if (createsObject()) {
			return scope.getEnclosingScopePath();
		}
		if (this.standalone) {

			final Path enclosingScopePath = scope.getEnclosingScopePath();

			if (enclosingScopePath != null) {
				return enclosingScopePath;
			}
		}
		return Path.SELF_PATH;
	}

	@Override
	public String toString() {
		return getPhrase().getPrefix().toString();
	}

	@Override
	protected void define(ClauseInstance instance, Block<?> definition) {
		for (PhraseSubContext subContext : subContexts()) {

			final LocationInfo location = instance.getLocation();
			final Statements<?> statements =
					definition.declare(location).alternative(location);

			subContext.applyCause(statements);
		}
	}

	@Override
	final MainPhraseContext getMainContext() {
		return this;
	}

	void setImplicitAscendants(Ascendants implicitAscendants) {
		this.implicitAscendants = implicitAscendants;
	}

	NextClause findObjectClause(
			LocationInfo location,
			MemberId memberId,
			Object what) {
		assert this.createsObject >= 0 :
			"Object won't be constructed, so resolution of \"" + location
			+ "\" is impossible";

		if (!this.firstClauseFound) {
			establishLinkTargetSearch();
		}

		final Ascendants implicitAscendants = effectiveImplicitAscendants();
		final NextClause foundInImplicitAscendants = findInImplicitSamples(
				location,
				memberId,
				what,
				implicitAscendants);

		if (foundInImplicitAscendants != null) {
			this.firstClauseFound = true;
			return foundInImplicitAscendants;
		}

		final NextClause foundInAncestor =
				findInAncestor(location, memberId, what);

		if (foundInAncestor != null) {
			if (foundInAncestor.found()) {
				this.firstClauseFound = true;
			}
			return foundInAncestor;
		}

		final NextClause foundInImplicitAncestor = findInImplicitAncestor(
				location,
				memberId,
				what,
				implicitAscendants);

		if (foundInImplicitAncestor != null) {
			this.firstClauseFound = true;
			return foundInImplicitAncestor;
		}

		return clauseNotFound(what);
	}

	private void establishLinkTargetSearch() {
		if (getAscendants().getTypeParameters() != null) {
			this.firstClauseFound = true;
		}
	}

	private Ascendants effectiveImplicitAscendants() {
		if (this.implicitAscendants == null) {
			return null;
		}

		final DefinitionTarget definitionTarget =
				getAscendants().getDefinitionTarget();

		if (definitionTarget.isDefault()) {
			// Only implicit ascendants are known.
			// Search for clauses there.
			return this.implicitAscendants;
		}
		if (definitionTarget.is(
				this.implicitAscendants.getDefinitionTarget())) {
			// Declaring with object constructor expression.
			// Implicit ascendants will be searched for clauses.
			return this.implicitAscendants;
		}

		// Declaring link or macro by value.
		// Clauses should be looked for in the link target,
		// but not in the link body
		return null;
	}

	private NextClause findInImplicitSamples(
			LocationInfo location,
			MemberId memberId,
			Object what,
			Ascendants implicitAscendants) {
		if (implicitAscendants == null) {
			return null;
		}

		final Sample sample = implicitAscendants.getSample();

		if (sample != null) {

			final NextClause found = findClause(
					sample.getObject(),
					location,
					memberId,
					what);

			if (found.found()) {
				return found;
			}
		}

		return null;
	}

	private NextClause findInAncestor(
			LocationInfo location,
			MemberId memberId,
			Object what) {

		final TypeRef ancestorRef = getAscendants().getAncestor();

		if (ancestorRef == null) {
			return null;
		}

		final Obj ancestor = ancestorRef.getType();
		final NextClause found = findClause(
				ancestor,
				location,
				memberId,
				what);

		if (found.found()) {
			return found;
		}

		final NextClause foundInTarget = findInAncestorTarget(
				location,
				memberId,
				what,
				ancestorRef,
				ancestor);

		if (foundInTarget != null) {
			return foundInTarget;
		}

		return clauseNotFound(what);// Prevent an implicit ancestor search.
	}

	private NextClause findInAncestorTarget(
			LocationInfo location,
			MemberId memberId,
			Object what,
			TypeRef ancestorRef,
			Obj ancestor) {
		if (this.firstClauseFound) {
			// This is only possible for the very first phrase part.
			return null;
		}

		final LinkValueType linkType =
				ancestor.type().getValueType().toLinkType();

		if (linkType == null) {
			return null;
		}

		final TypeRef iface =
				linkType.interfaceRef(ancestor.type().getParameters());
		final NextClause foundInIface = findClause(
				iface.getType(),
				location,
				memberId,
				what);

		if (!foundInIface.found()) {
			return null;
		}

		getPhrase().getPrefix().setAncestor(
				ancestorRef.getRef().dereference().toTypeRef());

		return foundInIface;
	}

	private NextClause findInImplicitAncestor(
			LocationInfo location,
			MemberId memberId,
			Object what,
			Ascendants implicitAscendants) {
		if (implicitAscendants == null) {
			return null;
		}

		final TypeRef implicitAncestor =
				implicitAscendants.getExplicitAncestor();

		if (implicitAncestor == null) {
			return null;
		}

		final NextClause found = findClause(
				implicitAncestor.getType(),
				location,
				memberId,
				what);

		if (found.found()) {
			return found;
		}

		return null;
	}

	private void build() {
		if (this.createsObject != 0) {
			return;
		}

		final LinkedList<PhraseContext> stack = new LinkedList<>();
		final PhrasePrefix prefix = getPhrase().getPrefix();
		PhraseContext context = this;
		PhraseContinuation continuation = prefix.getFollowing();
		boolean hasParts = false;

		if (continuation == null) {
			// Phrase with only prefix and no parts.
			// It probably has a type parameters.
			// It creates an object.
			this.createsObject = 1;
			return;
		}
		do {

			final NextClause nextClause = continuation.nextClause(context);

			if (nextClause.isError()) {
				break;
			}
			if (!nextClause.found()) {
				if (stack.isEmpty()) {
					this.createsObject = 1;
				}

				final Clause clause = context.getClause();

				if (clause != null && clause.requiresContinuation()) {
					getLogger().error(
							"incomplete_phrase",
							continuation.getLocation().addAnother(clause),
							"Incomplete phrase");
					break;
				}
				if (!nextClause.isError()) {
					getResolutionLogger().error(
							"unresolved_clause",
							continuation,
							"Unsupported clause: %s",
							nextClause.what());
				}
				break;
			}

			if (context.isObject()
					&& hasParts
					&& !nextClause.requiresInstance()) {
				// Expression clause after declaration part.
				// Construct object and use this constructor as a new phrase
				// prefix.
				this.nextPart = continuation;
				break;
			}

			hasParts = true;
			for (NextClause implicit : nextClause.getImplicit()) {
				context = nextContext(stack, continuation, implicit);
			}

			final PartsAsPrefix partsAsPrefix = nextClause.partsAsPrefix();

			if (partsAsPrefix.includeLast()) {
				context = nextContext(stack, continuation, nextClause);
				context.incompleteInstance().addContent(continuation);
			}

			if (partsAsPrefix.isPrefix()) {
				// Phrase terminator encountered.
				// Use the phrase prefix with preceding parts
				// as a new phrase prefix.
				if (partsAsPrefix.startNewPhraseFromLast()) {
					this.nextPart = continuation;
				} else {
					this.nextPart = continuation.getFollowing();
				}
				break;
			}

			continuation = continuation.getFollowing();
		} while (continuation != null);
	}

	private PhraseContext nextContext(
			LinkedList<PhraseContext> stack,
			PhraseContinuation continuation,
			NextClause nextClause) {
		if (stack.isEmpty()) {
			stack.push(this);
			if (nextClause.requiresInstance()) {
				this.createsObject = 1;
			} else {
				// Next clause is expression.
				// It will construct an object by itself.
				this.createsObject = -1;

				final ObjectTypeParameters typeParameters =
						getPhrase().getTypeParameters();

				if (typeParameters != null) {
					getLogger().error(
							"prohibited_phrase_type_parameters",
							typeParameters,
							"The value type is prohibited when the top-level"
							+ " clause of phrase is an expression");
				}
			}
		}

		final PhraseContext containerContext =
				popTill(stack, nextClause.getContainer());
		final PhraseContext context = push(
				stack,
				containerContext,
				continuation,
				nextClause.getClause());

		if (this.nextContext == null) {
			this.nextContext = context;
		}

		return context;
	}

	private PhraseContext popTill(
			LinkedList<PhraseContext> stack,
			Clause clause) {
		for (;;) {

			final PhraseContext top = stack.peek();

			assert top != null :
				"Can not find enclosing clause " + clause;

			final Clause topClause = top.getClause();

			if (sameClause(topClause, clause)) {
				return top;
			}

			stack.pop();

			if (topClause != null) {
				top.incompleteInstance().complete();
			}
		}
	}

	private PhraseContext push(
			LinkedList<PhraseContext> stack,
			PhraseContext containerContext,
			PhraseContinuation continuation,
			Clause clause) {
		if (sameClause(containerContext.getClause(), clause)) {
			return containerContext;
		}

		final PhraseContext enclosingContext = push(
				stack,
				containerContext,
				continuation,
				clause.getEnclosingClause());
		final PhraseSubContext subContext =
				enclosingContext.incompleteInstance().addSubContext(
						continuation,
						clause);

		stack.push(subContext);

		return subContext;
	}

	private static boolean sameClause(Clause clause1, Clause clause2) {
		if (clause1 == null) {
			if (clause2 == null) {
				return true;
			}
		} else if (clause2 != null) {
			if (clause1.getKey().equals(clause2.getKey())) {
				return true;
			}
		}
		return false;
	}

}

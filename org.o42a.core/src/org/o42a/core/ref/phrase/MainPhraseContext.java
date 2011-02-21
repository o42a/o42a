/*
    Compiler Core
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
package org.o42a.core.ref.phrase;

import static org.o42a.core.member.MemberId.memberName;
import static org.o42a.core.ref.phrase.NextClause.clauseNotFound;

import java.util.LinkedList;

import org.o42a.core.LocationSpec;
import org.o42a.core.Scope;
import org.o42a.core.member.AdapterId;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.clause.ClauseId;
import org.o42a.core.member.clause.ClauseKind;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.Statements;


final class MainPhraseContext extends PhraseContext {

	private int createsObject;
	private boolean standaloneRef;
	private PhraseContext nextContext;
	private FieldDeclaration fieldDeclaration;

	MainPhraseContext(Phrase phrase) {
		super(phrase, phrase.getPrefix());
	}

	public final AscendantsDefinition getAscendants() {
		return getPhrase().getPrefix().getAscendants();
	}

	public boolean createsObject() {
		if (this.createsObject == 0) {
			build();
		}
		return this.createsObject > 0;
	}

	public Ref createRef() {
		if (!createsObject()) {
			return standaloneRef();
		}
		return new PhraseObject.Ex(this);
	}

	public Ref standaloneRef() {
		this.standaloneRef = true;

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
	public NextClause clauseByName(LocationSpec location, String name) {
		return findObjectClause(location, memberName(name));
	}

	@Override
	public NextClause clauseById(LocationSpec location, ClauseId clauseId) {
		return findObjectClause(location, clauseId(location, clauseId));
	}

	@Override
	public Path pathToObject(Scope scope) {
		if (createsObject()) {
			return scope.getEnclosingScopePath();
		}
		if (this.standaloneRef) {

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

			final LocationSpec location = instance.getLocation();
			final Statements<?> statements =
				definition.propose(location).alternative(location);

			subContext.applyCause(statements);
		}
	}

	@Override
	final MainPhraseContext getMainContext() {
		return this;
	}

	void setFieldDeclaration(FieldDeclaration fieldDeclaration) {
		this.fieldDeclaration = fieldDeclaration;
	}

	NextClause findObjectClause(LocationSpec location, MemberId memberId) {
		assert this.createsObject >= 0 :
			"Object won't be constructed, so resolution of \"" + location
			+ "\" is impossible";

		for (StaticTypeRef sample : getAscendants().getSamples()) {

			final NextClause found =
				findClause(sample.getType(), location, memberId);

			if (found.found()) {
				return found;
			}
		}

		if (this.fieldDeclaration != null) {

			final AdapterId adapterId =
				this.fieldDeclaration.getMemberId().getAdapterId();

			if (adapterId != null) {

				final StaticTypeRef adapterType =
					adapterId.adapterType(getPhrase().getScope());

				if (adapterType != null) {

					final NextClause found =
						findClause(adapterType.getType(), location, memberId);

					if (found.found()) {
						return found;
					}
				}
			}
		}

		final TypeRef ancestor = getAscendants().getAncestor();

		if (ancestor != null) {

			final NextClause found =
				findClause(ancestor.getType(), location, memberId);

			if (found.found()) {
				return found;
			}
		}

		return clauseNotFound(memberId);
	}

	private void build() {

		final LinkedList<PhraseContext> stack = new LinkedList<PhraseContext>();
		final PhrasePrefix prefix = getPhrase().getPrefix();
		PhraseContext context = this;
		PhraseContinuation continuation = prefix.getFollowing();

		while (continuation != null) {

			final NextClause nextClause = continuation.nextClause(context);

			if (!nextClause.found()) {
				if (stack.isEmpty()) {
					this.createsObject = 1;
				}
				getLogger().unresolved(continuation, nextClause.getMemberId());
				break;
			}

			for (NextClause implicit : nextClause.getImplicit()) {
				context = nextContext(stack, continuation, implicit);
			}

			context = nextContext(stack, continuation, nextClause);
			context.incompleteInstance().addContent(continuation);
			continuation = continuation.getFollowing();
		}
	}

	private PhraseContext nextContext(
			LinkedList<PhraseContext> stack,
			PhraseContinuation continuation,
			NextClause nextClause) {
		if (stack.isEmpty()) {
			stack.push(this);
			if (nextClause.getClause() == null) {
				// Next clause is an object itself.
				// New object will be constructed.
				this.createsObject = 1;
			} else if (nextClause.getClause().requiresInstance()) {
				// Next clause requires enclosing object instance to be created.
				this.createsObject = 1;
			} else {
				// Next clause is expression.
				// It will construct an object by itself.
				this.createsObject = -1;

				final StaticTypeRef[] samples = getPhrase().getSamples();

				if (samples.length != 0) {
					getLogger().prohibitedSamples(samples[0]);
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

			if (top.getClause() == clause) {
				return top;
			}

			stack.pop();

			final Clause topClause = top.getClause();

			if (topClause != null
					&& topClause.getKind() == ClauseKind.EXPRESSION) {
				top.incompleteInstance().complete();
			}
		}
	}

	private PhraseContext push(
			LinkedList<PhraseContext> stack,
			PhraseContext containerContext,
			PhraseContinuation continuation,
			Clause clause) {
		if (containerContext.getClause() == clause) {
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

}

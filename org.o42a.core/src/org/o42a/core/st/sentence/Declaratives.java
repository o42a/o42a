/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.core.st.sentence;

import static org.o42a.core.st.DefValue.TRUE_DEF_VALUE;
import static org.o42a.core.st.Definer.noDefs;
import static org.o42a.core.st.impl.SentenceErrors.declarationNotAlone;

import org.o42a.core.Container;
import org.o42a.core.member.field.FieldBuilder;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.Resolver;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.*;
import org.o42a.core.st.impl.declarative.ExplicitInclusion;
import org.o42a.core.st.impl.imperative.BracesWithinDeclaratives;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.string.Name;


public final class Declaratives extends Statements<Declaratives, Definer> {

	private final DeclarativesEnv env = new DeclarativesEnv(this);
	private DefTargets targets;

	Declaratives(LocationInfo location, DeclarativeSentence sentence) {
		super(location, sentence);
	}

	@Override
	public final LocalScope getLocalScope() {
		return null;
	}

	@Override
	public final boolean isInsideLoop() {
		return false;
	}

	public final boolean isInsideClaim() {
		return getSentence().isInsideClaim();
	}

	@Override
	public final DeclarativeSentence getSentence() {
		return (DeclarativeSentence) super.getSentence();
	}

	@Override
	public final DeclarativeFactory getSentenceFactory() {
		return super.getSentenceFactory().toDeclarativeFactory();
	}

	public final DefTargets getDefTargets() {
		if (this.targets != null) {
			return this.targets;
		}
		executeInstructions();
		return this.targets = definerTargets();
	}

	@Override
	public FieldBuilder field(
			FieldDeclaration declaration,
			FieldDefinition definition) {
		if (getSentence().isInsideClaim()) {
			getLogger().error(
					"prohibited_claim_field",
					declaration,
					"Field can not be declared inside the claim");
			dropStatement();
			return null;
		}
		if (getSentence().isConditional()) {
			getLogger().error(
					"prohibited_conditional_field",
					declaration,
					"Field declaration can not be conditional");
			dropStatement();
			return null;
		}
		return super.field(declaration, definition);
	}

	@Override
	public final DeclarativeBlock parentheses(LocationInfo location) {
		return parentheses(location, getContainer());
	}

	@Override
	public final DeclarativeBlock parentheses(
			LocationInfo location,
			Container container) {
		return (DeclarativeBlock) super.parentheses(location, container);
	}

	@Override
	public void ellipsis(LocationInfo location, Name name) {
		dropStatement();
		getLogger().prohibitedDeclarativeEllipsis(location);
	}

	@Override
	public void include(LocationInfo location, Name tag) {
		if (!getMemberRegistry().inclusions().include(location, tag)) {
			return;
		}
		statement(new ExplicitInclusion(location, this, tag));
	}

	@Override
	protected void braces(ImperativeBlock braces) {
		statement(new BracesWithinDeclaratives(
				braces,
				nextDistributor(),
				braces));
	}

	@Override
	protected Definer implicate(Statement statement) {
		return statement.define(this.env);
	}

	DefValue value(Resolver resolver) {
		for (Definer definer : getImplications()) {

			final DefValue value = definer.value(resolver);

			if (value.hasValue()) {
				return value;
			}
			if (!value.getCondition().isTrue()) {
				return value;
			}
		}
		return TRUE_DEF_VALUE;
	}

	private DefTargets definerTargets() {

		DefTargets result = noDefs();
		DefTargets prev = noDefs();
		DefTargets firstDeclaring = null;

		for (Definer definer : getImplications()) {

			final DefTargets targets = definer.getDefTargets();

			if (targets.declaring()) {
				if (firstDeclaring != null) {
					if (!result.haveError()) {
						declarationNotAlone(getLogger(), targets);
						result = result.addError();
					}
					continue;
				}
				firstDeclaring = targets;
				if (result.defining() && !result.haveError()) {
					declarationNotAlone(getLogger(), firstDeclaring);
					result = result.addError();
				}
				continue;
			}
			if (firstDeclaring != null && !targets.isEmpty()) {
				if (!result.haveError()) {
					declarationNotAlone(getLogger(), firstDeclaring);
					result = result.addError();
				}
				continue;
			}
			if (!prev.breaking() || prev.havePrerequisite()) {
				if (targets.breaking()) {
					prev = targets;
				} else {
					prev = targets.toPreconditions();
				}
				result = result.add(prev);
				continue;
			}
			if (result.haveError()) {
				continue;
			}
			result = result.addError();
			getLogger().error(
					"redundant_statement",
					targets,
					"Redundant statement");
		}

		if (firstDeclaring != null && result.isEmpty()) {
			return result.add(firstDeclaring);
		}

		return result;
	}

	private static final class DeclarativesEnv extends DefinerEnv {

		private final Declaratives statements;

		DeclarativesEnv(Declaratives statements) {
			this.statements = statements;
		}

		@Override
		public String toString() {
			if (this.statements == null) {
				return super.toString();
			}
			return this.statements.toString();
		}

		@Override
		protected ValueStruct<?, ?> expectedValueStruct() {
			return this.statements.getSentence()
					.getAltEnv()
					.getExpectedValueStruct();
		}

	}

}

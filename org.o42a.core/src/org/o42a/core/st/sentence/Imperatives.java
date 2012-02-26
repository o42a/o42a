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

import static org.o42a.core.st.DefinitionTargets.noDefinitions;

import org.o42a.core.Container;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Definer;
import org.o42a.core.st.DefinitionTargets;
import org.o42a.core.st.Statement;
import org.o42a.core.st.impl.imperative.AssignmentStatement;
import org.o42a.core.st.impl.imperative.EllipsisStatement;
import org.o42a.core.st.impl.imperative.ImperativeStatementEnv;


public class Imperatives extends Statements<Imperatives> {

	private DefinitionTargets kinds;

	Imperatives(
			LocationInfo location,
			ImperativeSentence sentence,
			boolean opposite) {
		super(location, sentence, opposite);
	}

	@Override
	public final ImperativeSentence getSentence() {
		return (ImperativeSentence) super.getSentence();
	}

	@Override
	public final ImperativeFactory getSentenceFactory() {
		return getSentence().getSentenceFactory();
	}

	@Override
	public DefinitionTargets getDefinitionTargets() {
		if (this.kinds != null) {
			return this.kinds;
		}

		executeInstructions();

		DefinitionTargets result = noDefinitions();

		for (Definer definer : getDefiners()) {
			result = result.add(definer.getDefinitionTargets());
		}

		return this.kinds = result;
	}

	@Override
	public void assign(LocationInfo location, Ref destination, Ref value) {
		assert destination.getContext() == getContext() :
			destination + " has wrong context: " + destination.getContext()
			+ ", but " + getContext() + " expected";
		assert value.getContext() == getContext() :
			value + " has wrong context: " + value.getContext()
			+ ", but " + getContext() + " expected";
		if (getSentence().isIssue()) {
			getLogger().error(
					"prohibited_issue_assignment",
					location,
					"Assignment is prohibited within issue");
			return;
		}
		statement(new AssignmentStatement(location, destination, value));
	}

	@Override
	public final ImperativeBlock parentheses(LocationInfo location) {
		return parentheses(location, getContainer());
	}

	@Override
	public final ImperativeBlock parentheses(
			LocationInfo location,
			Container container) {
		return (ImperativeBlock) super.parentheses(location, container);
	}

	@Override
	public void ellipsis(LocationInfo location, String name) {
		if (getSentence().isIssue()) {
			getLogger().error(
					"prohibited_issue_ellipsis",
					location,
					"Ellipsis is prohibited within issue");
			return;
		}

		final Block<?> block = blockByName(location, name);

		if (block == null) {
			return;
		}
		statement(new EllipsisStatement(location, this, name));
	}

	@Override
	public void include(LocationInfo location, String name) {
		getLogger().error(
				"prohibited_imperative_inclusion",
				location,
				"Inclusion into imperative block is not possible");
	}

	@Override
	protected void braces(ImperativeBlock braces) {
		statement(braces);
	}

	@Override
	protected Definer define(Statement statement) {

		final ImperativeStatementEnv env = new ImperativeStatementEnv(
				this,
				getSentence().getBlock().getInitialEnv());

		return statement.define(env);
	}

	final void normalizeImperatives(Normalizer normalizer) {
		for (Definer definer : getDefiners()) {
			definer.getStatement().normalizeImperative(normalizer);
		}
	}

	private Block<?> blockByName(LocationInfo location, String name) {
		if (name == null) {
			return getSentence().getBlock();
		}

		Block<?> block = getSentence().getBlock();

		for (;;) {
			if (name.equals(block.getName())) {
				return block;
			}

			final Statements<?> enclosing = block.getEnclosing();

			if (enclosing == null) {
				getLogger().unresolved(location, name);
				return null;
			}
		}
	}

}

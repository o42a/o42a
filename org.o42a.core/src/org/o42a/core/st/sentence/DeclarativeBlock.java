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
package org.o42a.core.st.sentence;

import static org.o42a.core.Distributor.declarativeDistributor;
import static org.o42a.core.def.Definitions.postConditionDefinitions;
import static org.o42a.core.st.sentence.SentenceFactory.DECLARATIVE_FACTORY;

import java.util.List;

import org.o42a.ast.Node;
import org.o42a.core.*;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.local.StOp;
import org.o42a.core.member.field.DeclaredField;
import org.o42a.core.member.field.MemberRegistry;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.Cond;
import org.o42a.core.st.DefinitionTarget;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.action.Action;
import org.o42a.util.Place.Trace;


public final class DeclarativeBlock extends Block<Declaratives> {

	static DeclarativeBlock declarativeBlock(
			LocationSpec location,
			Distributor distributor,
			Statements<?> enclosing,
			DeclarativeFactory sentenceFactory) {
		return new DeclarativeBlock(
				location,
				distributor,
				enclosing,
				sentenceFactory);
	}

	public DeclarativeBlock(
			LocationSpec location,
			Container container,
			MemberRegistry memberRegistry) {
		this(
				location,
				declarativeDistributor(container),
				memberRegistry);
	}

	public DeclarativeBlock(
			LocationSpec location,
			Distributor distributor,
			MemberRegistry memberRegistry) {
		super(
				location,
				distributor,
				memberRegistry,
				DECLARATIVE_FACTORY);
	}

	public DeclarativeBlock(
			LocationSpec location,
			DeclaredField<?> field,
			Statements<?> enclosing,
			MemberRegistry memberRegistry) {
		super(
				location,
				declarativeDistributor(field.getContainer()),
				enclosing,
				memberRegistry,
				DECLARATIVE_FACTORY);
	}

	private DeclarativeBlock(
			LocationSpec location,
			Distributor distributor,
			Statements<?> enclosing,
			DeclarativeFactory sentenceFactory) {
		super(
				location,
				distributor,
				enclosing,
				enclosing.getMemberRegistry(),
				sentenceFactory);
	}

	@Override
	public boolean isParentheses() {
		return true;
	}

	@Override
	public String getName() {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DeclarativeSentence> getSentences() {
		return (List<DeclarativeSentence>) super.getSentences();
	}

	@Override
	public Cond condition(Scope scope) {
		if (!getKind().hasCondition()) {
			return null;
		}

		Cond req = null;
		Cond cond = null;

		for (DeclarativeSentence sentence : getSentences()) {

			final Cond condition = sentence.condition(scope);

			if (sentence.getPrerequisite() == null) {
				req = Cond.and(req, condition);
			} else {
				cond = Cond.or(cond, condition);
			}
		}

		return Cond.and(req, cond);
	}

	@Override
	public Definitions define(DefinitionTarget target) {
		if (!getKind().hasCondition()) {
			return null;
		}
		if (!getKind().hasDefinition()) {
			if (target.isField()) {
				return null;
			}

			final Cond condition = condition(target.getScope());

			return postConditionDefinitions(
					condition,
					target.getScope(),
					condition);
		}

		final List<DeclarativeSentence> sentences = getSentences();
		Definitions result = null;

		for (DeclarativeSentence sentence : sentences) {

			final Definitions definitions = sentence.define(target);

			if (definitions == null) {
				continue;
			}

			if (result == null) {
				result = definitions;
			} else {
				result = result.refine(definitions);
			}
		}

		return result;
	}

	@Override
	public Action initialValue(LocalScope scope) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Action initialCondition(LocalScope scope) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DeclarativeBlock reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final DeclarativeBlock reproduction;

		if (getEnclosing() == null) {
			reproduction = new DeclarativeBlock(
					this,
					reproducer.getContainer(),
					reproducer.getMemberRegistry());
			reproduceSentences(reproducer, reproduction);
			return reproduction;
		}

		reproduction =
			(DeclarativeBlock) reproducer.getStatements().parentheses(this);
		reproduceSentences(reproducer, reproduction);

		return null;
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();
		final Node node = getNode();

		if (node != null) {
			node.printContent(out);
			if (out.length() < 2 || out.charAt(0) != '(') {
				out.insert(0, '(').append(')');
			}
		} else {
			out.append("(...)");
		}

		return out.toString();
	}

	@Override
	protected StOp createOp(LocalBuilder builder) {
		throw new UnsupportedOperationException();
	}

	@Override
	final Trace getTrace() {
		return null;
	}

}

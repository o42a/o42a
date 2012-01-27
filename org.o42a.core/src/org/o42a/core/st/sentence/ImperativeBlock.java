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

import static org.o42a.core.ScopePlace.localPlace;
import static org.o42a.core.st.StatementEnv.defaultEnv;
import static org.o42a.core.st.impl.imperative.InlineBlock.inlineBlock;
import static org.o42a.util.Place.FIRST_PLACE;

import java.util.List;

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.ScopePlace;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.local.StOp;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.member.local.LocalRegistry;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.*;
import org.o42a.core.st.impl.BlockDefiner;
import org.o42a.core.st.impl.imperative.*;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.Lambda;
import org.o42a.util.Place.Trace;
import org.o42a.util.log.Loggable;


public final class ImperativeBlock extends Block<Imperatives> {

	public static ImperativeBlock topLevelImperativeBlock(
			LocationInfo location,
			Distributor distributor,
			Statements<?> enclosing,
			String name,
			ImperativeFactory sentenceFactory,
			Lambda<MemberRegistry, LocalScope> memberRegistry) {

		final LocalScope scope = enclosing.getMemberRegistry().newLocalScope(
				location,
				distributor,
				name);

		if (scope == null) {
			return null;
		}

		final MemberRegistry registry;

		if (memberRegistry != null) {
			registry = memberRegistry.get(scope);
		} else {
			registry = new LocalRegistry(scope, enclosing.getMemberRegistry());
		}

		return new ImperativeBlock(
				location,
				scope,
				registry,
				sentenceFactory);
	}

	public static ImperativeBlock nestedImperativeBlock(
			LocationInfo location,
			Distributor distributor,
			Statements<?> enclosing,
			boolean parentheses,
			String name,
			MemberRegistry memberRegistry,
			ImperativeFactory sentenceFactory) {
		return new ImperativeBlock(
				location,
				distributor,
				enclosing,
				parentheses,
				name,
				memberRegistry,
				sentenceFactory);
	}

	private final boolean parentheses;
	private final String name;
	private final boolean topLevel;
	private final Trace trace;
	private Locals locals;

	public ImperativeBlock(
			LocationInfo location,
			LocalScope scope,
			MemberRegistry memberRegistry,
			ImperativeFactory sentenceFactory) {
		this(
				location,
				new BlockDistributor(location, scope),
				memberRegistry,
				sentenceFactory);
	}

	private ImperativeBlock(
			LocationInfo location,
			Distributor distributor,
			MemberRegistry memberRegistry,
			ImperativeFactory sentenceFactory) {
		super(location, distributor, memberRegistry, sentenceFactory);
		this.parentheses = false;
		this.name = distributor.getScope().toLocal().getName();
		this.topLevel = true;
		this.trace = getPlace().nestedTrace();

		final LocalScopeBase scope = getScope();

		scope.setBlock(this);
	}

	private ImperativeBlock(
			LocationInfo location,
			Distributor distributor,
			Statements<?> enclosing,
			boolean parentheses,
			String name,
			MemberRegistry memberRegistry,
			ImperativeFactory sentenceFactory) {
		super(
				location,
				distributor,
				enclosing,
				memberRegistry,
				sentenceFactory);
		this.parentheses = parentheses;
		this.name = name;
		this.topLevel = false;
		this.trace = getPlace().nestedTrace();
	}

	public final boolean isTopLevel() {
		return this.topLevel;
	}

	@Override
	public LocalScope getScope() {
		return super.getScope().toLocal();
	}

	@Override
	public ImperativeFactory getSentenceFactory() {
		return (ImperativeFactory) super.getSentenceFactory();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ImperativeSentence> getSentences() {
		return (List<ImperativeSentence>) super.getSentences();
	}

	@Override
	public final boolean isParentheses() {
		return this.parentheses;
	}

	@Override
	public final String getName() {
		return this.name;
	}

	@Override
	public final ImperativeBlock toImperativeBlock() {
		return this;
	}

	@Override
	public final ImperativeSentence propose(LocationInfo location) {
		return (ImperativeSentence) super.propose(location);
	}

	@Override
	public final ImperativeSentence claim(LocationInfo location) {
		return (ImperativeSentence) super.claim(location);
	}

	@Override
	public final ImperativeSentence issue(LocationInfo location) {
		return (ImperativeSentence) super.issue(location);
	}

	@Override
	public ImperativeBlock reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final Statements<?> enclosing = reproducer.getStatements();

		if (enclosing != null) {

			final ImperativeBlock reproduction =
					enclosing.braces(this, getName());

			reproduceSentences(reproducer, reproduction);

			return null;
		}

		final ImperativeBlock reproduction = new ImperativeBlock(
				this,
				reproducer.distribute(),
				reproducer.getMemberRegistry(),
				getSentenceFactory());

		reproduction.define(defaultEnv(this));
		reproduceSentences(reproducer, reproduction);

		return reproduction;
	}

	@Override
	public InlineCommand inlineImperative(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct) {
		return inlineBlock(normalizer, valueStruct, this);
	}

	@Override
	public void normalizeImperative(Normalizer normalizer) {
		for (ImperativeSentence sentence : getSentences()) {
			sentence.normalizeImperatives(normalizer);
		}
	}

	public Statement wrap(Distributor distributor) {
		if (!isTopLevel()) {
			return this;
		}
		return new BracesWithinDeclaratives(this, distributor, this);
	}

	@Override
	protected StOp createOp(CodeBuilder builder) {
		return new ImperativeBlockOp(builder, this);
	}

	@Override
	Trace getTrace() {
		return this.trace;
	}

	@Override
	Locals getLocals() {
		if (this.locals != null) {
			return this.locals;
		}
		return this.locals =
				getEnclosing().getSentence().getBlock().getLocals();
	}

	@Override
	BlockDefiner<?> createDefiner(StatementEnv env) {
		return new ImperativeBlockDefiner(this, env);
	}

	public static final class BlockDistributor extends Distributor {

		private final LocationInfo location;
		private final LocalScope scope;

		public BlockDistributor(LocationInfo location, LocalScope scope) {
			this.location = location;
			this.scope = scope;
		}

		@Override
		public Loggable getLoggable() {
			return this.location.getLoggable();
		}

		@Override
		public CompilerContext getContext() {
			return this.location.getContext();
		}

		@Override
		public LocalScope getScope() {
			return this.scope;
		}

		@Override
		public Container getContainer() {
			return this.scope;
		}

		@Override
		public ScopePlace getPlace() {
			return localPlace(getScope(), FIRST_PLACE);
		}

	}

}

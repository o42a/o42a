/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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
import static org.o42a.core.st.DefinerEnv.defaultEnv;
import static org.o42a.util.Place.FIRST_PLACE;

import java.util.List;

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.ScopePlace;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.member.local.LocalScopeRegistry;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.*;
import org.o42a.core.st.impl.imperative.*;
import org.o42a.core.value.ValueRequest;
import org.o42a.util.Place.Trace;
import org.o42a.util.fn.Lambda;
import org.o42a.util.string.Name;


public final class ImperativeBlock extends Block<Imperatives, Command> {

	public static ImperativeBlock topLevelImperativeBlock(
			LocationInfo location,
			Distributor distributor,
			Statements<?, ?> enclosing,
			Name name,
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
			registry = new LocalScopeRegistry(scope, enclosing.getMemberRegistry());
		}

		return new ImperativeBlock(
				location,
				new BlockDistributor(location, scope),
				enclosing,
				false,
				scope.getName(),
				registry,
				sentenceFactory,
				true);
	}

	public static ImperativeBlock nestedImperativeBlock(
			LocationInfo location,
			Distributor distributor,
			Statements<?, ?> enclosing,
			boolean parentheses,
			Name name,
			MemberRegistry memberRegistry,
			ImperativeFactory sentenceFactory) {
		return new ImperativeBlock(
				location,
				distributor,
				enclosing,
				parentheses,
				name,
				memberRegistry,
				sentenceFactory,
				false);
	}

	private final boolean parentheses;
	private final Name name;
	private final SentencesEnv sentencesEnv = new SentencesEnv();
	private final boolean topLevel;
	private final Trace trace;
	private NamedBlocks namedBlocks;
	private ImplicationEnv initialEnv;
	private boolean loop;

	private ImperativeBlock(
			LocationInfo location,
			Distributor distributor,
			Statements<?, ?> enclosing,
			boolean parentheses,
			Name name,
			MemberRegistry memberRegistry,
			ImperativeFactory sentenceFactory,
			boolean topLevel) {
		super(
				location,
				distributor,
				enclosing,
				memberRegistry,
				sentenceFactory);
		this.parentheses = parentheses;
		this.name = name;
		this.topLevel = topLevel;
		this.trace = getPlace().nestedTrace();
	}

	private ImperativeBlock(
			LocationInfo location,
			Distributor distributor,
			MemberRegistry memberRegistry,
			ImperativeFactory sentenceFactory) {
		super(location, distributor, memberRegistry, sentenceFactory);
		this.parentheses = false;
		this.name = distributor.getScope().toLocalScope().getName();
		this.topLevel = true;
		this.trace = getPlace().nestedTrace();

		final LocalScopeBase scope = getScope();

		scope.setBlock(this);
	}

	public final boolean isTopLevel() {
		return this.topLevel;
	}

	public final LocalScope getLocalScope() {
		return getScope();
	}

	public final boolean isLoop() {
		return this.loop;
	}

	@Override
	public LocalScope getScope() {
		return super.getScope().toLocalScope();
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

	public final Name getName() {
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
	public final Definer define(DefinerEnv env) {
		this.initialEnv = env;
		assert isTopLevel() :
			"Not a top-level imperative block";
		return new ImperativeDefiner(this, env);
	}

	@Override
	public final Command command(CommandEnv env) {
		this.initialEnv = env;
		return new BlockCommand(this, env);
	}

	@Override
	public ImperativeBlock reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final Statements<?, ?> enclosing = reproducer.getStatements();

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

		reproduction.define(defaultEnv(reproducer.getLogger()));
		reproduceSentences(reproducer, reproduction);

		return reproduction;
	}

	public Statement wrap(Distributor distributor) {
		if (!isTopLevel()) {
			return this;
		}
		return new BracesWithinDeclaratives(this, distributor, this);
	}

	@Override
	Trace getTrace() {
		return this.trace;
	}

	@Override
	NamedBlocks getNamedBlocks() {
		if (this.namedBlocks != null) {
			return this.namedBlocks;
		}
		return this.namedBlocks =
				getEnclosing().getSentence().getBlock().getNamedBlocks();
	}

	final CommandEnv sentencesEnv() {
		return this.sentencesEnv;
	}

	final void loop() {
		this.loop = true;
	}

	private final ImplicationEnv getInitialEnv() {
		return this.initialEnv;
	}

	private final class SentencesEnv extends CommandEnv {

		@Override
		public ValueRequest getValueRequest() {
			return getInitialEnv().getValueRequest();
		}

	}

	public static final class BlockDistributor extends Distributor {

		private final Location location;
		private final LocalScope scope;

		public BlockDistributor(LocationInfo location, LocalScope scope) {
			this.location = location.getLocation();
			this.scope = scope;
		}

		@Override
		public final Location getLocation() {
			return this.location;
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

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

import org.o42a.core.Distributor;
import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.local.StOp;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.local.LocalRegistry;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.value.ValueType;


final class BracesWithinDeclaratives extends Statement {

	private final ImperativeBlock block;

	BracesWithinDeclaratives(
			LocationInfo location,
			Distributor distributor,
			ImperativeBlock block) {
		super(location, distributor);
		this.block = block;
	}

	@Override
	public StatementKinds getStatementKinds() {
		return this.block.getStatementKinds();
	}

	@Override
	public ValueType<?> getValueType() {
		return this.block.getValueType();
	}

	@Override
	public Conditions setConditions(Conditions conditions) {
		return this.block.setConditions(conditions);
	}

	@Override
	public Definitions define(Scope scope) {
		return this.block.define(scope);
	}

	@Override
	public Action initialValue(LocalScope scope) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Action initialLogicalValue(LocalScope scope) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Statement reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final LocalScope reproducedScope =
			reproducer.getMemberRegistry().reproduceLocalScope(
					reproducer,
					this.block.getScope());

		if (reproducedScope == null) {
			return null;
		}

		final ImperativeReproducer imperativeReproducer =
			new ImperativeReproducer(
					this.block.getScope(),
					reproducedScope,
					reproducer);

		final ImperativeBlock block =
			this.block.reproduce(imperativeReproducer);

		if (block == null) {
			return null;
		}

		return new BracesWithinDeclaratives(
				this,
				reproducer.distribute(),
				block);
	}

	@Override
	public String toString() {
		return this.block.toString();
	}

	@Override
	protected StOp createOp(LocalBuilder builder) {
		throw new UnsupportedOperationException();
	}

	private static final class ImperativeReproducer extends Reproducer {

		private final Reproducer reproducer;
		private final LocalRegistry localRegistry;

		ImperativeReproducer(
				LocalScope reproducingScope,
				LocalScope reproducedScope,
				Reproducer reproducer) {
			super(
					reproducingScope,
					new ImperativeBlock.BlockDistributor(reproducedScope));
			this.reproducer = reproducer;
			this.localRegistry = new LocalRegistry(
					reproducedScope,
					reproducer.getMemberRegistry());
		}

		@Override
		public Ref getPhrasePrefix() {
			return this.reproducer.getPhrasePrefix();
		}

		@Override
		public MemberRegistry getMemberRegistry() {
			return this.localRegistry;
		}

		@Override
		public Statements<?> getStatements() {
			return null;
		}

		@Override
		public Reproducer reproducerOf(Scope reproducingScope) {
			if (getReproducingScope() == reproducingScope) {
				return this;
			}
			return this.reproducer.reproducerOf(reproducingScope);
		}

		@Override
		public String toString() {
			return this.reproducer.toString() + '[' + getScope() + ']';
		}

		@Override
		public void applyClause(
				LocationInfo location,
				Statements<?> statements,
				Clause clause) {
			throw new UnsupportedOperationException();
		}

	}

}

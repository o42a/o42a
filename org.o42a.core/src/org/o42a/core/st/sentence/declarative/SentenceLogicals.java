/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.core.st.sentence.declarative;

import java.util.ArrayList;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ref.Logical;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.StatementEnv;
import org.o42a.core.st.sentence.DeclarativeSentence;
import org.o42a.core.value.LogicalValue;


final class SentenceLogicals {

	private final LocationInfo location;
	private final Scope scope;

	private Logical common = null;
	private Result logicals = null;

	SentenceLogicals(LocationInfo location, Scope scope) {
		this.location = location;
		this.scope = scope;
	}

	public final Logical build() {

		final Logical result = Logical.and(this.common, this.logicals);

		this.logicals = null;

		return result;
	}

	public void addSentence(DeclarativeSentence sentence) {
		if (sentence.getPrerequisite() == null) {
			if (this.logicals == null) {
				this.logicals = new Result(this.location, this.scope);
			}
			this.logicals.otherwise(sentence);
			return;
		}
		if (this.logicals == null) {
			this.logicals = new Result(this.location, this.scope);
			this.logicals.addVariant(sentence);
			return;
		}
		if (!this.logicals.otherwisePresent()) {
			this.logicals.addVariant(sentence);
			return;
		}

		this.common = build();
		this.logicals = new Result(this.location, this.scope);
		this.logicals.addVariant(sentence);

		return;
	}

	@Override
	public String toString() {
		if (this.common != null) {
			if (this.logicals == null) {
				return "TRUE";
			}
			return this.logicals.toString();
		}

		return this.common + " & (" + this.logicals + ")";
	}

	private static final class Result extends Logical {

		private final ArrayList<StatementEnv> variants =
			new ArrayList<StatementEnv>();
		private Logical otherwise;

		private LogicalValue constantValue;

		private Result(LocationInfo location, Scope scope) {
			super(location, scope);
		}

		@Override
		public LogicalValue getConstantValue() {
			if (this.constantValue != null) {
				return this.constantValue;
			}

			if (this.variants.isEmpty()) {
				if (this.otherwise == null) {
					return this.constantValue = LogicalValue.TRUE;
				}
				return this.constantValue = this.otherwise.getConstantValue();
			}

			for (StatementEnv conditions : this.variants) {

				final Logical prerequisite =
					conditions.prerequisite(getScope());

				if (!prerequisite.isConstant()) {
					return this.constantValue = LogicalValue.RUNTIME;
				}
				if (prerequisite.isFalse()) {
					continue;
				}

				final Logical precondition =
					conditions.precondition(getScope());

				return this.constantValue = precondition.getConstantValue();
			}

			if (this.otherwise == null) {
				return this.constantValue = LogicalValue.FALSE;
			}

			return this.constantValue = this.otherwise.getConstantValue();
		}

		@Override
		public LogicalValue logicalValue(Scope scope) {
			assertCompatible(scope);
			if (this.variants.isEmpty()) {
				if (this.otherwise == null) {
					return LogicalValue.TRUE;
				}
				return this.otherwise.logicalValue(scope);
			}

			for (StatementEnv conditions : this.variants) {

				final LogicalValue prerequisite =
					conditions.prerequisite(getScope()).logicalValue(scope);

				if (!prerequisite.isConstant()) {
					return prerequisite;
				}
				if (prerequisite.isFalse()) {
					continue;
				}

				return conditions.precondition(getScope()).logicalValue(scope);
			}

			if (this.otherwise == null) {
				return LogicalValue.FALSE;
			}

			return this.otherwise.logicalValue(scope);
		}

		@Override
		public Logical reproduce(Reproducer reproducer) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void write(Code code, CodePos exit, HostOp host) {

			final int size = this.variants.size();

			if (size == 0) {
				if (this.otherwise != null) {
					this.otherwise.write(code, exit, host);
				}
				return;
			}

			CodePos otherwise;

			if (this.otherwise == null) {
				otherwise = exit;
			} else {

				final Code otherwiseBlock = code.addBlock("otherwise");

				this.otherwise.write(otherwiseBlock, exit, host);

				otherwise = otherwiseBlock.head();
			}

			int idx = 0;
			Code prereq = code;

			for (;;) {

				final StatementEnv conditions = this.variants.get(idx);
				final Logical prerequisite =
					conditions.prerequisite(getScope());
				final Logical precondition =
					conditions.precondition(getScope());
				final int nextIdx = idx + 1;

				if (nextIdx >= size) {
					prerequisite.write(prereq, otherwise, host);
					precondition.write(prereq, exit, host);
					return;
				}

				final Code next = code.addBlock(nextIdx + "_prereq");

				prerequisite.write(prereq, next.head(), host);
				precondition.write(prereq, exit, host);

				prereq = next;
				idx = nextIdx;
			}
		}

		@Override
		public String toString() {
			if (this.variants.isEmpty()) {
				if (this.otherwise == null) {
					return "TRUE";
				}
				return this.otherwise.toString();
			}

			final StringBuilder out = new StringBuilder();

			for (StatementEnv conditions : this.variants) {
				out.append('(').append(conditions.prerequisite(getScope()));
				out.append(")? (");
				out.append(conditions.precondition(getScope())).append(").");
			}

			if (this.otherwise != null) {
				out.append(" OTHERWISE(").append(this.otherwise).append(").");
			}

			return out.toString();
		}

		private final boolean otherwisePresent() {
			return this.otherwise != null;
		}

		private final void addVariant(DeclarativeSentence setence) {
			assert !otherwisePresent() :
				"Can not add conditional sentence"
				+ " when otherwise condition already present";
			this.variants.add(setence.getEnv());
		}

		private final void otherwise(DeclarativeSentence sentence) {
			this.otherwise = Logical.and(
					this.otherwise,
					sentence.getEnv().fullLogical(getScope()));
		}

	}

}

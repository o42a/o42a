/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.core.st.impl.declarative;

import static org.o42a.core.ref.InlineCond.cancelUpToNull;

import java.util.ArrayList;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ref.*;
import org.o42a.core.source.LocationInfo;
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
		public LogicalValue logicalValue(Resolver resolver) {
			assertCompatible(resolver.getScope());
			if (this.variants.isEmpty()) {
				if (this.otherwise == null) {
					return LogicalValue.TRUE;
				}
				return this.otherwise.logicalValue(resolver);
			}

			for (StatementEnv env : this.variants) {

				final LogicalValue prerequisite =
						env.prerequisite(getScope()).logicalValue(resolver);

				if (!prerequisite.isConstant()) {
					return prerequisite;
				}
				if (prerequisite.isFalse()) {
					continue;
				}

				return env.precondition(getScope()).logicalValue(resolver);
			}

			if (this.otherwise == null) {
				return LogicalValue.FALSE;
			}

			return this.otherwise.logicalValue(resolver);
		}

		@Override
		public Logical reproduce(Reproducer reproducer) {
			throw new UnsupportedOperationException();
		}

		@Override
		public InlineCond inline(Normalizer normalizer, Scope origin) {
			if (this.variants.isEmpty()) {
				if (this.otherwise == null) {
					return InlineCond.INLINE_TRUE;
				}
				return this.otherwise.inline(normalizer, origin);
			}

			final InlineCond otherwise;

			if (this.otherwise == null) {
				otherwise = null;
			} else {
				otherwise = this.otherwise.inline(normalizer, origin);
				if (otherwise == null) {
					return null;
				}
			}

			final InlineCond[] prereqs = new InlineCond[this.variants.size()];
			final InlineCond[] preconds = new InlineCond[prereqs.length];
			int i = 0;

			for (StatementEnv variant : this.variants) {

				final InlineCond prereq =
						variant.prerequisite(getScope())
						.inline(normalizer, origin);
				final InlineCond precond =
						variant.precondition(getScope())
						.inline(normalizer, origin);

				prereqs[i] = prereq;
				preconds[i] = precond;
				++i;

				if (prereq == null || precond == null) {
					if (otherwise != null) {
						otherwise.cancel();
					}
					cancelUpToNull(prereqs);
					cancelUpToNull(preconds);
					return null;
				}
			}

			return new Inline(prereqs, preconds, otherwise);
		}

		@Override
		public void write(CodeDirs dirs, HostOp host) {
			assert assertFullyResolved();

			final int size = this.variants.size();

			if (size == 0) {
				if (this.otherwise != null) {
					this.otherwise.write(dirs, host);
				}
				return;
			}

			final Code code = dirs.code();
			final CodePos exit = dirs.falseDir();
			CodePos otherwise;

			if (this.otherwise == null) {
				otherwise = exit;
			} else {

				final Code otherwiseBlock = code.addBlock("otherwise");

				this.otherwise.write(
						dirs.getBuilder().falseWhenUnknown(
								otherwiseBlock,
								exit),
						host);

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
					prerequisite.write(
							dirs.getBuilder().falseWhenUnknown(
									prereq,
									otherwise),
							host);
					precondition.write(
							dirs.getBuilder().falseWhenUnknown(prereq, exit),
							host);
					break;
				}

				final Code next = code.addBlock(nextIdx + "_prereq");

				prerequisite.write(
						dirs.getBuilder().falseWhenUnknown(prereq, next.head()),
						host);
				precondition.write(
						dirs.getBuilder().falseWhenUnknown(prereq, exit),
						host);

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

		@Override
		protected void fullyResolve(Resolver resolver) {
			for (StatementEnv env : this.variants) {
				env.prerequisite(getScope()).resolveAll(resolver);
				env.precondition(getScope()).resolveAll(resolver);
			}
			if (this.otherwise != null) {
				this.otherwise.resolveAll(resolver);
			}
		}

		private final boolean otherwisePresent() {
			return this.otherwise != null;
		}

		private final void addVariant(DeclarativeSentence setence) {
			assert !otherwisePresent() :
				"Can not add conditional sentence"
				+ " when otherwise condition already present";
			this.variants.add(setence.getFinalEnv());
		}

		private final void otherwise(DeclarativeSentence sentence) {
			this.otherwise = Logical.and(
					this.otherwise,
					sentence.getFinalEnv().fullLogical(getScope()));
		}

	}

	private static final class Inline extends InlineCond {

		private final InlineCond[] prereqs;
		private final InlineCond[] preconds;
		private final InlineCond otherwise;

		Inline(
				InlineCond[] prereqs,
				InlineCond[] preconds,
				InlineCond otherwise) {
			this.prereqs = prereqs;
			this.preconds = preconds;
			this.otherwise = otherwise;
		}

		@Override
		public void writeCond(CodeDirs dirs, HostOp host) {

			final Code code = dirs.code();
			final CodePos exit = dirs.falseDir();
			CodePos otherwise;

			if (this.otherwise == null) {
				otherwise = exit;
			} else {

				final Code otherwiseBlock = code.addBlock("otherwise");

				this.otherwise.writeCond(
						dirs.getBuilder().falseWhenUnknown(
								otherwiseBlock,
								exit),
						host);

				otherwise = otherwiseBlock.head();
			}

			int idx = 0;
			Code prereq = code;

			for (;;) {

				final int nextIdx = idx + 1;

				if (nextIdx >= this.preconds.length) {
					this.prereqs[idx].writeCond(
							dirs.getBuilder().falseWhenUnknown(
									prereq,
									otherwise),
							host);
					this.preconds[idx].writeCond(
							dirs.getBuilder().falseWhenUnknown(prereq, exit),
							host);
					break;
				}

				final Code next = code.addBlock(nextIdx + "_prereq");

				this.prereqs[idx].writeCond(
						dirs.getBuilder().falseWhenUnknown(prereq, next.head()),
						host);
				this.preconds[idx].writeCond(
						dirs.getBuilder().falseWhenUnknown(prereq, exit),
						host);

				prereq = next;
				idx = nextIdx;
			}
		}

		@Override
		public void cancel() {
			cancelAll(this.prereqs);
			cancelAll(this.preconds);
			if (this.otherwise != null) {
				this.otherwise.cancel();
			}
		}

		@Override
		public String toString() {
			if (this.preconds == null) {
				return super.toString();
			}
			final StringBuilder out = new StringBuilder();

			for (int i = 0; i < this.preconds.length; ++i) {
				out.append('(').append(this.prereqs[i]);
				out.append(")? (");
				out.append(this.preconds[i]).append(").");
			}

			if (this.otherwise != null) {
				out.append(" OTHERWISE(").append(this.otherwise).append(").");
			}

			return out.toString();
		}

	}

}

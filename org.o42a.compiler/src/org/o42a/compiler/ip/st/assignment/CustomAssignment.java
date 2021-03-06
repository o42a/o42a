/*
    Compiler
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.compiler.ip.st.assignment;

import static org.o42a.compiler.ip.Interpreter.PLAIN_IP;
import static org.o42a.compiler.ip.type.TypeConsumer.EXPRESSION_TYPE_CONSUMER;
import static org.o42a.core.ref.RefUsage.CONDITION_REF_USAGE;

import org.o42a.analysis.escape.EscapeAnalyzer;
import org.o42a.analysis.escape.EscapeFlag;
import org.o42a.compiler.ip.phrase.PhraseBuilder;
import org.o42a.core.Scope;
import org.o42a.core.ir.cmd.Cmd;
import org.o42a.core.ir.cmd.Control;
import org.o42a.core.ir.cmd.InlineCmd;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.*;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.action.ExecuteCommand;
import org.o42a.core.value.link.LinkValueType;
import org.o42a.util.fn.Cancelable;


final class CustomAssignment extends AssignmentKind {

	static AssignmentKind customAssignment(
			AssignmentStatement statement,
			Obj destination) {

		final LinkValueType destType =
				destination.type().getValueType().toLinkType();

		if (destType != null && destType.isVariable()) {
			return null;
		}

		final PhraseBuilder phrase = new PhraseBuilder(
				PLAIN_IP,
				statement,
				statement.distributeAccess(),
				EXPRESSION_TYPE_CONSUMER);

		phrase.setAncestor(statement.getDestination().toTypeRef());
		phrase.assign(statement);

		return new CustomAssignment(statement, phrase.toRef());
	}

	private final Ref ref;

	private CustomAssignment(AssignmentStatement statement, Ref ref) {
		super(statement);
		this.ref = ref;
	}

	private CustomAssignment(CustomAssignment prototype, Ref ref) {
		super(prototype.getStatement());
		this.ref = ref;
	}

	public final Ref getRef() {
		return this.ref;
	}

	@Override
	public Action action(Resolver resolver) {
		return new ExecuteCommand(
				getStatement(),
				getRef().value(resolver).getKnowledge().getCondition());
	}

	@Override
	public EscapeFlag escapeFlag(EscapeAnalyzer analyzer, Scope scope) {
		return getRef().escapeFlag(analyzer, scope);
	}

	@Override
	public void resolve(FullResolver resolver) {
		getRef().resolveAll(resolver.setRefUsage(CONDITION_REF_USAGE));
	}

	@Override
	public InlineCmd inline(Normalizer normalizer, Scope origin) {

		final InlineValue value = this.ref.inline(normalizer, origin);

		if (value == null) {
			this.ref.normalize(normalizer.getAnalyzer());
			return null;
		}

		return new InlineAssignCmd(value);
	}

	@Override
	public InlineCmd normalize(RootNormalizer normalizer, Scope origin) {
		return inline(normalizer.newNormalizer(), origin);
	}

	@Override
	public AssignmentStatement reproduce(
			Reproducer reproducer,
			AssignmentStatement prototype) {
		if (this.ref == null) {
			return null;
		}

		final CustomAssignment customAssignment =
				new CustomAssignment(this, this.ref);

		return new AssignmentStatement(
				prototype,
				reproducer,
				customAssignment,
				null,
				null);
	}

	@Override
	public Cmd cmd() {
		return new AssignCmd(this.ref);
	}

	@Override
	public String toString() {
		if (this.ref == null) {
			return super.toString();
		}
		return this.ref.toString();
	}

	private static final class InlineAssignCmd extends InlineCmd {

		private final InlineValue value;

		InlineAssignCmd(InlineValue value) {
			super(null);
			this.value = value;
		}

		@Override
		public void write(Control control) {

			final CodeDirs dirs = control.dirs();

			this.value.writeCond(dirs, control.host());

			dirs.done();
		}

		@Override
		public String toString() {
			if (this.value == null) {
				return super.toString();
			}
			return "(++" + this.value + ")";
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

	private static final class AssignCmd implements Cmd {

		private final Ref ref;

		AssignCmd(Ref ref) {
			this.ref = ref;
		}

		@Override
		public void write(Control control) {

			final CodeDirs dirs = control.dirs();

			this.ref.op(control.host()).writeCond(dirs);

			dirs.done();
		}

		@Override
		public String toString() {
			if (this.ref == null) {
				return super.toString();
			}
			return this.ref.toString();
		}

	}

}

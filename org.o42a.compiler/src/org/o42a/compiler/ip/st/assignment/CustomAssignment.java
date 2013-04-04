/*
    Compiler
    Copyright (C) 2012,2013 Ruslan Lopatin

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
import static org.o42a.compiler.ip.access.AccessRules.ACCESS_FROM_DEFINITION;
import static org.o42a.compiler.ip.type.TypeConsumer.EXPRESSION_TYPE_CONSUMER;
import static org.o42a.core.ref.RefUsage.CONDITION_REF_USAGE;

import org.o42a.compiler.ip.phrase.PhraseBuilder;
import org.o42a.core.Scope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.local.Cmd;
import org.o42a.core.ir.local.Control;
import org.o42a.core.ir.local.InlineCmd;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.*;
import org.o42a.core.st.DefValue;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.action.ExecuteCommand;
import org.o42a.core.value.Value;
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
				ACCESS_FROM_DEFINITION.distribute(statement.distribute()),
				EXPRESSION_TYPE_CONSUMER);

		phrase.setAncestor(statement.getDestination().toTypeRef());
		phrase.assign(statement);

		return new CustomAssignment(statement, phrase.toRef());
	}

	private final Ref ref;
	private InlineValue normal;

	private CustomAssignment(AssignmentStatement statement, Ref ref) {
		super(statement);
		this.ref = ref;
	}

	public final Ref getRef() {
		return this.ref;
	}

	@Override
	public DefValue value(Resolver resolver) {

		final Value<?> value = getRef().value(resolver);

		return value.getKnowledge().getCondition().toDefValue();
	}

	@Override
	public Action initialValue(Resolver resolver) {
		return new ExecuteCommand(
				getStatement(),
				getRef().value(resolver).getKnowledge().getCondition());
	}

	@Override
	public void resolve(FullResolver resolver) {
		getRef().resolveAll(resolver.setRefUsage(CONDITION_REF_USAGE));
	}

	@Override
	public InlineEval inline(Normalizer normalizer, Scope origin) {

		final InlineValue inlineRef = getRef().inline(normalizer, origin);

		if (inlineRef == null) {
			return null;
		}

		return new InlineAssignEval(inlineRef);
	}

	@Override
	public Eval eval(CodeBuilder builder, Scope origin) {
		return new AssignEval(getRef());
	}

	@Override
	public InlineEval normalize(RootNormalizer normalizer, Scope origin) {
		return inline(normalizer.newNormalizer(), origin);
	}

	@Override
	public InlineCmd inlineCommand(Normalizer normalizer, Scope origin) {

		final InlineValue value = this.ref.inline(normalizer, origin);

		if (value == null) {
			this.ref.normalize(normalizer.getAnalyzer());
			return null;
		}

		return new InlineAssignCmd(value);
	}

	@Override
	public void normalizeCommand(RootNormalizer normalizer) {
		this.normal = this.ref.inline(
				normalizer.newNormalizer(),
				normalizer.getNormalizedScope());
	}

	@Override
	public AssignmentKind reproduce(
			AssignmentStatement statement,
			Reproducer reproducer) {

		final Ref ref = this.ref.reproduce(reproducer);

		if (ref == null) {
			return new AssignmentError(statement);
		}

		return new CustomAssignment(statement, ref);
	}

	@Override
	public Cmd cmd() {
		if (this.normal == null) {
			return new AssignCmd(this.ref);
		}
		return new NormalAssignCmd(this.normal);
	}

	private static final class InlineAssignEval extends InlineEval {

		private final InlineValue value;

		InlineAssignEval(InlineValue value) {
			super(null);
			this.value = value;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {
			this.value.writeCond(dirs.dirs(), host);
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

	private static final class InlineAssignCmd extends InlineCmd {

		private final InlineValue value;

		InlineAssignCmd(InlineValue value) {
			super(null);
			this.value = value;
		}

		@Override
		public void write(Control control) {

			final CodeDirs dirs = control.getBuilder().dirs(
					control.code(),
					control.falseDir());

			this.value.writeCond(dirs, control.host());
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

	public static final class AssignEval implements Eval {

		private final Ref ref;

		AssignEval(Ref ref) {
			this.ref = ref;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {
			this.ref.op(host).writeCond(dirs.dirs());
		}

		@Override
		public String toString() {
			if (this.ref == null) {
				return super.toString();
			}
			return this.ref.toString();
		}

	}

	private static final class AssignCmd implements Cmd {

		private final Ref ref;

		AssignCmd(Ref ref) {
			this.ref = ref;
		}

		@Override
		public void write(Control control) {
			this.ref.op(control.host()).writeCond(control.dirs());
		}

		@Override
		public String toString() {
			if (this.ref == null) {
				return super.toString();
			}
			return this.ref.toString();
		}

	}

	private static final class NormalAssignCmd implements Cmd {

		private final InlineValue value;

		NormalAssignCmd(InlineValue value) {
			this.value = value;
		}

		@Override
		public void write(Control control) {
			this.value.writeCond(control.dirs(), control.host());
		}

		@Override
		public String toString() {
			if (this.value == null) {
				return super.toString();
			}
			return this.value.toString();
		}

	}

}

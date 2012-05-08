/*
    Compiler
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.compiler.ip.assignment;

import static org.o42a.compiler.ip.Interpreter.PLAIN_IP;

import org.o42a.compiler.ip.phrase.ref.Phrase;
import org.o42a.core.Scope;
import org.o42a.core.ir.local.Cmd;
import org.o42a.core.ir.local.Control;
import org.o42a.core.ir.local.InlineCmd;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.object.Obj;
import org.o42a.core.object.link.LinkValueType;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RootNormalizer;
import org.o42a.core.st.Reproducer;
import org.o42a.util.fn.Cancelable;


final class CustomAssignment extends AssignmentKind {

	static AssignmentKind customAssignment(
			AssignmentStatement statement,
			Obj destination) {

		final LinkValueType destType =
				destination.value().getValueType().toLinkType();

		if (destType != LinkValueType.GETTER) {
			return null;
		}

		final Phrase phrase =
				new Phrase(PLAIN_IP, statement, statement.distribute());

		phrase.setAncestor(statement.getDestination().toTypeRef());
		phrase.assign(statement.getNode());
		phrase.operand(statement.getValue());

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
	public void resolve(LocalResolver resolver) {
		getRef().resolve(resolver).resolveCondition();
	}

	@Override
	public InlineCmd inline(Normalizer normalizer, Scope origin) {

		final InlineValue value = this.ref.inline(normalizer, origin);

		if (value == null) {
			this.ref.normalize(normalizer.getAnalyzer());
			return null;
		}

		return new Inline(value);
	}

	@Override
	public void normalize(RootNormalizer normalizer) {
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

	private static final class Inline extends InlineCmd {

		private final InlineValue value;

		Inline(InlineValue value) {
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

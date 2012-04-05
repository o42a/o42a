/*
    Compiler
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
package org.o42a.compiler.ip.statement;

import static org.o42a.core.ir.local.Cmd.noCmd;

import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.local.Cmd;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;


enum AssignmentKind {

	ASSIGNMENT_ERROR() {

		@Override
		public void resolve(
				LocalResolver resolver,
				Ref destination,
				Ref value) {
		}

		@Override
		public Cmd op(CodeBuilder builder, AssignmentStatement assignment) {
			return noCmd(builder, assignment);
		}

	},

	VALUE_ASSIGNMENT() {

		@Override
		public void resolve(
				LocalResolver resolver,
				Ref destination,
				Ref value) {
			destination.resolve(resolver).resolveAssignee();

			final Ref destTarget =
					destination.getPath()
					.dereference()
					.target(destination.distribute());
			final Resolution val =
					value.resolve(resolver).resolveTarget();
			final Resolution dest =
					destTarget.resolve(resolver).resolveTarget();

			if (dest.isError() || val.isError()) {
				return;
			}

			final Obj destObj = dest.toObject();
			final Obj valObj = val.toObject();

			valObj.value().wrapBy(destObj.value());
			valObj.type().wrapBy(destObj.type());
		}

		@Override
		public Cmd op(CodeBuilder builder, AssignmentStatement assignment) {
			return new AssignmentCmd(builder, assignment);
		}

	},

	DEREF_ASSIGNMENT() {

		@Override
		public void resolve(
				LocalResolver resolver,
				Ref destination,
				Ref value) {
			destination.resolve(resolver).resolveAssignee();

			final Ref destTarget =
					destination.getPath().target(destination.distribute());
			final Resolution val =
					value.resolve(resolver).resolveTarget();
			final Resolution dest =
					destTarget.resolve(resolver).resolveTarget();

			if (dest.isError() || val.isError()) {
				return;
			}

			final Obj destObj = dest.toObject();
			final Obj valObj = val.toObject();

			valObj.value().wrapBy(destObj.value());
			valObj.type().wrapBy(destObj.type());
		}

		@Override
		public Cmd op(CodeBuilder builder, AssignmentStatement assignment) {
			return new AssignmentCmd(builder, assignment);
		}

	};

	public final boolean isError() {
		return this == ASSIGNMENT_ERROR;
	}

	public abstract void resolve(
			LocalResolver resolver,
			Ref destination,
			Ref value);

	public abstract Cmd op(
			CodeBuilder builder,
			AssignmentStatement assignment);

}

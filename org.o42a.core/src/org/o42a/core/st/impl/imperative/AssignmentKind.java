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
package org.o42a.core.st.impl.imperative;

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

	VARIABLE_ASSIGNMENT() {

		@Override
		public void resolve(
				LocalResolver resolver,
				Ref destination,
				Ref value) {

			final Resolution val = value.resolve(resolver).resolveValue();
			final Resolution dest =
					destination.resolve(resolver).resolveAssignee();

			if (dest.isError() || val.isError()) {
				return;
			}

			dest.materialize().value().wrapBy(
					val.materialize().value());
			if (resolver.getScope() == destination.getScope()) {
				dest.toLink().assign(value);
			}
		}

		@Override
		public Cmd op(CodeBuilder builder, AssignmentStatement assignment) {
			return new VariableAssignmentCmd(builder, assignment);
		}

	},

	VALUE_ASSIGNMENT() {

		@Override
		public void resolve(
				LocalResolver resolver,
				Ref destination,
				Ref value) {

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

			final Obj destObj = dest.materialize();
			final Obj valObj = val.materialize();

			valObj.value().wrapBy(destObj.value());
			destObj.value().wrapBy(valObj.value());
			valObj.type().wrapBy(destObj.type());
			destObj.type().wrapBy(valObj.type());
		}

		@Override
		public Cmd op(CodeBuilder builder, AssignmentStatement assignment) {
			return new VariableAssignmentCmd(builder, assignment);
		}

	},


	TARGET_ASSIGNMENT() {

		@Override
		public void resolve(
				LocalResolver resolver,
				Ref destination,
				Ref value) {

			final Ref destTarget =
					destination.getPath()
					.dereference()
					.target(destination.distribute());
			final Ref valTarget =
					value.getPath()
					.dereference()
					.target(value.distribute());
			final Resolution val =
					valTarget.resolve(resolver).resolveTarget();
			final Resolution dest =
					destTarget.resolve(resolver).resolveTarget();

			if (dest.isError() || val.isError()) {
				return;
			}

			final Obj destObj = dest.materialize();
			final Obj valObj = val.materialize();

			valObj.value().wrapBy(destObj.value());
			destObj.value().wrapBy(valObj.value());
			valObj.type().wrapBy(destObj.type());
			destObj.type().wrapBy(valObj.type());
		}

		@Override
		public Cmd op(CodeBuilder builder, AssignmentStatement assignment) {
			return new TargetAssignmentCmd(builder, assignment);
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

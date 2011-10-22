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
package org.o42a.core.ref.impl.path;

import static org.o42a.core.ref.path.PathReproduction.reproducedPath;

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.PathOp;
import org.o42a.core.ir.op.StepOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.path.*;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.util.Deferred;


public class StaticStep extends Step {

	private final Deferred<Scope> deferredScope;
	private Scope scope;

	public StaticStep(Scope scope) {
		this.deferredScope = null;
		this.scope = scope;
	}

	public StaticStep(Deferred<Scope> deferredScope) {
		this.deferredScope = deferredScope;
	}

	public final Scope getScope() {
		if (this.scope != null) {
			return this.scope;
		}
		return this.scope = this.deferredScope.get();
	}

	@Override
	public StepKind getStepKind() {
		return StepKind.STATIC_STEP;
	}

	@Override
	public PathKind getPathKind() {
		return PathKind.RELATIVE_PATH;
	}

	@Override
	public boolean isMaterial() {
		return true;
	}

	@Override
	public Container resolve(
			PathResolver resolver,
			BoundPath path,
			int index,
			Scope start,
			PathWalker walker) {

		final Scope scope = getScope();

		scope.assertCompatible(start);

		walker.staticScope(this, this.scope);

		return scope.getContainer();
	}

	@Override
	public PathReproduction reproduce(
			LocationInfo location,
			Reproducer reproducer) {
		getScope().assertCompatible(reproducer.getReproducingScope());
		return reproducedPath(new StaticStep(reproducer.getScope()).toPath());
	}

	@Override
	public PathOp op(PathOp start) {
		return new Op(start, this);
	}

	@Override
	public String toString() {
		if (this.scope != null) {
			return '<' + this.scope.toString() + '>';
		}
		return '<' + this.deferredScope.toString() + '>';
	}

	@Override
	protected FieldDefinition fieldDefinition(
			BoundPath path,
			Distributor distributor) {
		return defaultFieldDefinition(path, distributor);
	}

	private static final class Op extends StepOp<StaticStep> {

		Op(PathOp start, StaticStep step) {
			super(start, step);
		}

		@Override
		public HostOp target(CodeDirs dirs) {
			// This should only be called for object scope.

			final ObjectIR ir =
					getStep().getScope().toObject().ir(getGenerator());

			return ir.op(getBuilder(), dirs.code());
		}

	}

}

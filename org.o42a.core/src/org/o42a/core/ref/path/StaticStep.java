/*
    Compiler Core
    Copyright (C) 2011-2013 Ruslan Lopatin

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
package org.o42a.core.ref.path;

import static org.o42a.core.ref.Prediction.exactPrediction;
import static org.o42a.core.ref.path.PathReproduction.reproducedPath;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.op.*;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Prediction;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.impl.normalizer.SameNormalStep;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;


final class StaticStep extends Step {

	private final Scope expectedScope;
	private final Scope finalScope;

	StaticStep(Scope scope) {
		this.expectedScope = this.finalScope = scope;
	}

	StaticStep(Scope expectedScope, Scope finalScope) {
		this.expectedScope = expectedScope;
		this.finalScope = finalScope;
	}

	public final Scope getExpectedScope() {
		return this.expectedScope;
	}

	public final Scope getFinalScope() {
		return this.finalScope;
	}

	@Override
	public PathKind getPathKind() {
		return PathKind.RELATIVE_PATH;
	}

	@Override
	public RefUsage getObjectUsage() {
		return null;
	}

	@Override
	public Path toPath() {
		return new Path(getPathKind(), true, null, this);
	}

	@Override
	public String toString() {
		if (this.finalScope == null) {
			return super.toString();
		}
		if (this.expectedScope.is(this.finalScope)) {
			return '<' + this.finalScope.toString() + '>';
		}
		return ('<' + this.expectedScope.toString() + "--"
				+ this.finalScope + '>');
	}

	@Override
	protected FieldDefinition fieldDefinition(Ref ref) {
		return defaultFieldDefinition(ref);
	}

	@Override
	protected TypeRef ancestor(LocationInfo location, Ref ref) {
		return defaultAncestor(location, ref);
	}

	@Override
	protected TypeRef iface(Ref ref) {
		return ref.toTypeRef();
	}

	@Override
	protected Container resolve(StepResolver resolver) {
		getExpectedScope().assertCompatible(resolver.getStart());

		resolver.getWalker().staticScope(this, getFinalScope());

		return getFinalScope().getContainer();
	}

	@Override
	protected void normalize(PathNormalizer normalizer) {

		final Prediction lastPrediction = normalizer.lastPrediction();

		normalizer.skip(
				exactPrediction(lastPrediction, lastPrediction.getScope()),
				new SameNormalStep(this));
	}

	@Override
	protected void normalizeStatic(PathNormalizer normalizer) {
		normalizer.finish();
	}

	@Override
	protected PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer) {
		getExpectedScope().assertCompatible(reproducer.getReproducingScope());
		return reproducedPath(
				new StaticStep(
						reproducer.getScope(),
						reproducer.getReproducer().reproducerOf(
								getFinalScope()).getScope())
				.toPath());
	}

	@Override
	protected PathOp op(PathOp start) {
		return new Op(start, this);
	}

	private static final class Op extends StepOp<StaticStep> {

		Op(PathOp start, StaticStep step) {
			super(start, step);
		}

		@Override
		public HostValueOp value() {
			return targetValueOp();
		}

		@Override
		public HostOp pathTarget(CodeDirs dirs) {
			// This should only be called for object scope.

			final ObjectIR ir =
					getStep().getFinalScope().toObject().ir(getGenerator());

			return ir.op(getBuilder(), dirs.code());
		}

	}

}

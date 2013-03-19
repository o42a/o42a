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
package org.o42a.core.member.local.impl;

import static org.o42a.core.ref.path.PathReproduction.reproducedPath;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.HostValueOp;
import org.o42a.core.ir.local.LocalScopeOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.PathOp;
import org.o42a.core.ir.op.StepOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.ReversePath;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;


public final class LocalScopeOwnerStep extends Step implements ReversePath {

	private final LocalScope local;

	public LocalScopeOwnerStep(LocalScope local) {
		this.local = local;
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
	public Scope revert(Scope target) {
		return target.toObject().member(
				this.local.toMember().getMemberKey())
				.toLocalScope()
				.localScope();
	}

	@Override
	public String toString() {
		return "Owner[" + this.local + ']';
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

		final LocalScope local = resolver.getStart().toLocalScope();

		local.assertDerivedFrom(this.local);

		final Obj owner = local.getOwner();

		resolver.getWalker().up(local, this, owner, this);

		return owner;
	}

	@Override
	protected PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer) {
		return reproducedPath(
				reproducer.getScope().toLocalScope().getEnclosingScopePath());
	}

	@Override
	protected void normalize(PathNormalizer normalizer) {
		normalizer.up(
				normalizer.lastPrediction().getScope()
				.toLocalScope().getOwner().getScope(),
				toPath(),
				this);
	}

	@Override
	protected void normalizeStatic(PathNormalizer normalizer) {
		normalize(normalizer);
	}

	@Override
	protected PathOp op(PathOp start) {
		return new Op(start, this);
	}

	private static final class Op extends StepOp<LocalScopeOwnerStep> {

		Op(PathOp start, LocalScopeOwnerStep step) {
			super(start, step);
		}

		@Override
		public HostValueOp value() {
			return targetValueOp();
		}

		@Override
		public HostOp target(CodeDirs dirs) {

			final LocalScopeOp local = start().toLocalScope();

			assert local != null :
				start() + " is not local";

			return local.owner();
		}

	}

}

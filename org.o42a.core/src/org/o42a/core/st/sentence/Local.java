/*
    Compiler Core
    Copyright (C) 2013 Ruslan Lopatin

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
package org.o42a.core.st.sentence;

import static org.o42a.core.member.MemberName.fieldName;
import static org.o42a.core.ref.RefUsage.CONTAINER_REF_USAGE;
import static org.o42a.core.ref.path.PathReproduction.reproducedPath;

import org.o42a.analysis.Analyzer;
import org.o42a.core.*;
import org.o42a.core.ir.op.PathOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Accessor;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.path.impl.ObjectStepUses;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.string.Name;


public final class Local extends Step implements PlaceInfo {

	private final Location location;
	private final Name name;
	private final Ref ref;
	private ObjectStepUses uses;

	Local(LocationInfo location, Name name, Ref ref) {
		assert name != null :
			"Local name not specified";
		assert ref != null :
			"Local reference not specified";
		this.location = location.getLocation();
		this.name = name;
		this.ref = ref;
	}

	public final Name getName() {
		return this.name;
	}

	public final Ref ref() {
		return this.ref;
	}

	@Override
	public final PathKind getPathKind() {
		return PathKind.RELATIVE_PATH;
	}

	@Override
	public final RefUsage getObjectUsage() {
		return RefUsage.CONTAINER_REF_USAGE;
	}

	@Override
	public final Location getLocation() {
		return this.location;
	}

	@Override
	public final Scope getScope() {
		return ref().getScope();
	}

	@Override
	public final ScopePlace getPlace() {
		return ref().getPlace();
	}

	@Override
	public final Container getContainer() {
		return ref().getContainer();
	}

	@Override
	public final Distributor distribute() {
		return Placed.distribute(this);
	}

	@Override
	public final Distributor distributeIn(Container container) {
		return Placed.distributeIn(this, container);
	}

	@Override
	public final void assertScopeIs(Scope scope) {
		Scoped.assertScopeIs(this, scope);
	}

	@Override
	public final void assertCompatible(Scope scope) {
		Scoped.assertCompatible(this, scope);
	}

	@Override
	public final void assertSameScope(ScopeInfo other) {
		Scoped.assertSameScope(this, other);
	}

	@Override
	public final void assertCompatibleScope(ScopeInfo other) {
		Scoped.assertCompatibleScope(this, other);
	}

	@Override
	public String toString() {
		if (this.name == null) {
			return super.toString();
		}
		return this.name.toString();
	}

	@Override
	protected void rebuild(PathRebuilder rebuilder) {
		if (!rebuilder.isStatic()) {
			// Locals should never be statically referenced.
			rebuilder.combinePreviousWithLocal(this);
		}
	}

	@Override
	protected FieldDefinition fieldDefinition(Ref ref) {
		return ref().toFieldDefinition().prefixWith(refPrefix(ref));
	}

	@Override
	protected TypeRef ancestor(LocationInfo location, Ref ref) {
		return ref().ancestor(location).prefixWith(refPrefix(ref));
	}

	@Override
	protected TypeRef iface(Ref ref) {
		return ref().getInterface().prefixWith(refPrefix(ref));
	}

	@Override
	protected Container resolve(StepResolver resolver) {

		final Scope start = resolver.getStart();
		final Scope enclosingScope = start.getEnclosingScope();
		final Resolver enclosingResolver = enclosingScope.resolver();

		if (resolver.isFullResolution()) {
			uses().useBy(resolver);

			final RefUsage usage;

			if (resolver.isLastStep()) {
				// Resolve only the last value.
				usage = resolver.refUsage();
			} else {
				usage = CONTAINER_REF_USAGE;
			}

			ref().resolveAll(
					enclosingResolver.fullResolver(resolver.refUser(), usage));
		}

		final Obj resolution = ref().resolve(enclosingResolver).toObject();

		resolver.getWalker().local(start, this);

		return resolution.toObject();
	}

	@Override
	protected void normalize(PathNormalizer normalizer) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void normalizeStatic(PathNormalizer normalizer) {
		normalizer.cancel();// Locals should never be statically referenced.
	}

	@Override
	protected Path nonNormalizedRemainder(PathNormalizer normalizer) {
		return ref().getPath().getPath();
	}

	@Override
	protected void normalizeStep(Analyzer analyzer) {
		ref().normalize(analyzer);
	}

	@Override
	protected PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer) {

		final Path path =
				reproducer.getReproducer()
				.getContainer()
				.member(this, Accessor.OWNER, fieldName(getName()), null);

		assert path != null :
			"Can not find reproduced local";

		final Local local = (Local) path.lastStep();

		return reproducedPath(local.toPath());
	}

	@Override
	protected PathOp op(PathOp start) {
		// TODO Auto-generated method stub
		return null;
	}

	private final ObjectStepUses uses() {
		if (this.uses != null) {
			return this.uses;
		}
		return this.uses = new ObjectStepUses(this);
	}

	private PrefixPath refPrefix(Ref ref) {
		return ref.getPath().cut(1).toPrefix(ref.getScope());
	}

}

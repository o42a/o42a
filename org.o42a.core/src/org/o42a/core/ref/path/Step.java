/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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

import static org.o42a.core.ref.path.PathBindings.NO_PATH_BINDINGS;
import static org.o42a.core.ref.path.impl.AncestorFragment.ANCESTOR_FRAGMENT;

import org.o42a.analysis.Analyzer;
import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.ir.op.PathOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.path.impl.PathFieldDefinition;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.ValueAdapter;
import org.o42a.core.value.ValueStruct;


public abstract class Step {

	public static final Container resolveStep(
			Step step,
			PathResolver resolver,
			BoundPath path,
			int index,
			Scope start,
			PathWalker walker) {
		return step.resolve(resolver, path, index, start, walker);
	}

	public abstract PathKind getPathKind();

	public String getName() {
		return null;
	}

	public abstract RefUsage getObjectUsage();

	public ValueAdapter valueAdapter(
			Ref ref,
			ValueStruct<?, ?> expectedStruct,
			boolean adapt) {
		return ref.valueStruct(ref.getScope()).defaultAdapter(
				ref,
				expectedStruct,
				adapt);
	}

	public Path toPath() {
		return new Path(getPathKind(), NO_PATH_BINDINGS, false, this);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	protected PathFragment getPathFragment() {
		return null;
	}

	protected void rebuild(PathRebuilder rebuilder) {
	}

	protected void combineWith(PathRebuilder rebuilder, Step next) {
	}

	protected void combineWithLocalOwner(
			PathRebuilder rebuilder,
			Obj owner) {
	}

	protected TypeRef ancestor(
			BoundPath path,
			LocationInfo location,
			Distributor distributor) {
		return path.append(ANCESTOR_FRAGMENT).typeRef(distributor);
	}

	protected abstract FieldDefinition fieldDefinition(
			BoundPath path,
			Distributor distributor);

	protected abstract Container resolve(
			PathResolver resolver,
			BoundPath path,
			int index,
			Scope start,
			PathWalker walker);

	protected abstract Scope revert(Scope target);

	protected abstract void normalize(PathNormalizer normalizer);

	protected abstract void normalizeStatic(PathNormalizer normalizer);

	protected Path nonNormalizedRemainder(PathNormalizer normalizer) {
		return Path.SELF_PATH;
	}

	protected void normalizeStep(Analyzer analyzer) {
	}

	protected abstract PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer);

	protected final FieldDefinition defaultFieldDefinition(
			BoundPath path,
			Distributor distributor) {
		return new PathFieldDefinition(path, distributor);
	}

	protected abstract PathOp op(PathOp start);

}

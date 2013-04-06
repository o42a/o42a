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
package org.o42a.core.ref.path.impl;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.ir.op.PathOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;


public class Wrapper extends Step {

	public static PrefixPath wrapperPrefix(
			Scope wrapperScope,
			Scope wrappedScope) {

		final Wrapper wrapper = new Wrapper(wrapperScope, wrappedScope);

		return wrapper.toPath().bindStatically(wrapperScope, wrapperScope)
				.toPrefix(wrapperScope);
	}

	private final Scope wrapperScope;
	private final Scope wrappedScope;

	private Wrapper(Scope wrapperScope, Scope wrappedScope) {
		this.wrapperScope = wrapperScope;
		this.wrappedScope = wrappedScope;
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
	public String toString() {
		if (this.wrappedScope == null) {
			return super.toString();
		}
		return "<" + this.wrapperScope + ">//<" + this.wrappedScope + '>';
	}

	@Override
	protected FieldDefinition fieldDefinition(Ref ref) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected TypeRef ancestor(LocationInfo location, Ref ref) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected TypeRef iface(Ref ref) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Container resolve(	StepResolver resolver) {
		resolver.getStart().assertSameScope(this.wrapperScope);
		resolver.getWalker().staticScope(this, this.wrappedScope);
		return this.wrappedScope.getContainer();
	}

	@Override
	protected PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void normalize(PathNormalizer normalizer) {
		normalizer.cancel();
	}

	@Override
	protected void normalizeStatic(PathNormalizer normalizer) {
		normalizer.cancel();
	}

	@Override
	protected PathOp op(PathOp start) {
		throw new UnsupportedOperationException();
	}

}

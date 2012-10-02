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
package org.o42a.core.ref;

import org.o42a.core.*;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.PathResolution;
import org.o42a.core.ref.path.PathResolver;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.directive.Directive;
import org.o42a.util.log.Loggable;


public final class Resolution implements ScopeInfo {

	private final Ref ref;
	private final Resolver resolver;
	private Container resolved;
	private boolean error;

	Resolution(Ref ref, Resolver resolver) {
		this.ref = ref;
		this.resolver = resolver;
	}

	public final Ref getRef() {
		return this.ref;
	}

	public final Resolver getResolver() {
		return this.resolver;
	}

	@Override
	public final CompilerContext getContext() {
		return getResolver().getContext();
	}

	@Override
	public final Loggable getLoggable() {
		return getResolver().getLoggable();
	}

	@Override
	public final Scope getScope() {
		return getResolved().getScope();
	}

	public final boolean isError() {
		return this.error;
	}

	public final boolean isResolved() {

		final Obj object = toObject();

		return object == null || !object.isNone();
	}

	public final boolean isNone() {

		final Obj object = toObject();

		return object != null && object.isNone();
	}

	public final Clause toClause() {
		return getResolved().toClause();
	}

	public final Obj toObject() {
		return getResolved().toObject();
	}

	public Directive toDirective() {

		final Obj object = toObject();

		if (object == null) {
			return null;
		}
		if (!object.value().getValueType().is(ValueType.DIRECTIVE)) {
			return null;
		}

		final Value<Directive> value =
				ValueStruct.DIRECTIVE.cast(object.value().getValue());

		if (!value.getKnowledge().isKnown()) {
			getResolver().getLogger().error(
					"runtime_directive",
					this,
					"Unable to execute directive at compile time");
			return null;
		}

		return value.getCompilerValue();
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
		if (this.resolved != null) {
			return this.resolved.toString();
		}
		return "Resolution[" + this.ref + ']';
	}

	final Resolution resolveAll(FullResolver resolver) {
		getRef().refFullyResolved();

		final Container resolved = resolve(resolver.toPathResolver());

		resolver.getRefUsage().fullyResolve(resolver, resolved);

		return this;
	}

	private final Container getResolved() {
		if (this.resolved != null) {
			return this.resolved;
		}
		return this.resolved = resolve(getResolver().toPathResolver());
	}

	private final Container resolve(PathResolver pathResolver) {

		final BoundPath path = getRef().getPath();
		final PathResolution pathResolution =
				path.walk(pathResolver, getResolver().getWalker());

		if (!pathResolution.isResolved()) {
			if (pathResolution.isError()) {
				this.error = true;
			}
			return getContext().getNone();
		}

		return pathResolution.getResult();
	}

}

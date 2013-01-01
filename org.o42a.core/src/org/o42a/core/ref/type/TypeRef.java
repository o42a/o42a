/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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
package org.o42a.core.ref.type;

import static org.o42a.core.object.ConstructionMode.PROHIBITED_CONSTRUCTION;
import static org.o42a.core.ref.path.PrefixPath.upgradePrefix;
import static org.o42a.core.ref.type.TypeRefParameters.defaultTypeRefParameters;

import org.o42a.core.Scope;
import org.o42a.core.ScopeInfo;
import org.o42a.core.Scoped;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.object.ConstructionMode;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.impl.DefaultTypeRef;
import org.o42a.core.ref.type.impl.DefaultTypeRelation;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.Location;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public abstract class TypeRef implements ScopeInfo {

	public static TypeRef typeRef(Ref ref, TypeRefParameters parameters) {
		if (ref.isKnownStatic()) {
			return ref.toStaticTypeRef(parameters);
		}
		return new DefaultTypeRef(ref, parameters(ref, parameters));
	}

	public static StaticTypeRef staticTypeRef(
			Ref ref,
			TypeRefParameters parameters) {
		return new StaticTypeRef(
				ref,
				ref.toStatic(),
				parameters(ref, parameters));
	}

	private static TypeRefParameters parameters(
			Ref ref,
			TypeRefParameters parameters) {
		if (parameters != null) {
			return parameters;
		}
		return defaultTypeRefParameters(ref, ref.getScope());
	}

	private final Ref ref;
	private final TypeRefParameters typeRefParameters;
	private TypeParameters<?> parameters;
	private TypeRef ancestor;
	private TypeHolder type;
	private boolean allResolved;

	public TypeRef(Ref ref, TypeRefParameters parameters) {
		this.ref = ref;
		this.typeRefParameters = parameters;
	}

	@Override
	public final Location getLocation() {
		return getRef().getLocation();
	}

	public final CompilerContext getContext() {
		return getLocation().getContext();
	}

	@Override
	public final Scope getScope() {
		return getRef().getScope();
	}

	public abstract boolean isStatic();

	public final Ref getRef() {
		return this.ref;
	}

	public abstract Ref getIntactRef();

	public final ValueType<?> getValueType() {
		return getParameters().getValueType();
	}

	public final Obj getType() {
		return get().getObject();
	}

	public final TypeParameters<?> getParameters() {
		if (this.parameters != null) {
			return this.parameters;
		}

		final TypeParameters<?> defaultParameters = defaultParameters();
		final TypeParameters<?> typeParameters =
				this.typeRefParameters.refine(defaultParameters);

		if (typeParameters == null || typeParameters.isEmpty()) {
			this.parameters = defaultParameters;
		} else {
			this.parameters = typeParameters;
		}

		this.parameters.assertSameScope(this);

		return this.parameters;
	}

	public final TypeRefParameters copyParameters() {
		return this.typeRefParameters;
	}

	public final TypeParameters<?> defaultParameters() {
		return getRef().typeParameters(getScope());
	}

	public TypeRef setParameters(TypeRefParameters parameters) {
		parameters.assertSameScope(this);
		return create(getIntactRef(), getRef(), parameters);
	}

	public final BoundPath getPath() {

		final BoundPath path = getRef().getPath();

		assert path != null :
			"Not a path: " + this;

		return path;
	}

	public final boolean isValid() {
		return get().isValid();
	}

	public boolean validateAll() {
		if (!isValid()) {
			return false;
		}
		return getParameters().validateAll();
	}

	public final TypeRef getAncestor() {
		if (this.ancestor != null) {
			return this.ancestor;
		}
		return this.ancestor = getIntactRef().ancestor(this);
	}

	public final ConstructionMode getConstructionMode() {

		final TypeHolder holder = get();

		if (holder == null) {
			return PROHIBITED_CONSTRUCTION;
		}

		return holder.getObject().getConstructionMode();
	}

	public final CompilerLogger getLogger() {
		return getContext().getLogger();
	}

	public final Resolution resolve(Resolver resolver) {
		return getRef().resolve(resolver);
	}

	public final Value<?> value(Resolver resolver) {
		return getRef().value(resolver);
	}

	public final TypeRelation relationTo(TypeRef other) {
		return new DefaultTypeRelation(this, other);
	}

	public final boolean derivedFrom(TypeRef other) {
		return relationTo(other).isDerivative();
	}

	public StaticTypeRef toStatic() {
		return new StaticTypeRef(
				getIntactRef(),
				getRef(),
				this.typeRefParameters);
	}

	public TypeRef prefixWith(PrefixPath prefix) {
		if (prefix.emptyFor(this)) {
			return this;
		}

		final Ref oldRef = getRef();
		final Ref newRef = oldRef.prefixWith(prefix);

		final TypeRefParameters parameters =
				this.typeRefParameters.prefixWith(prefix);

		if (oldRef == newRef && this.typeRefParameters == parameters) {
			return this;
		}

		final Ref oldIntactRef = getIntactRef();
		final Ref newIntactRef;

		if (oldIntactRef == oldRef) {
			newIntactRef = newRef;
		} else {
			newIntactRef = oldIntactRef.prefixWith(prefix);
		}

		return create(newIntactRef, newRef, parameters);
	}

	public TypeRef upgradeScope(Scope toScope) {
		if (getScope().is(toScope)) {
			return this;
		}
		return prefixWith(upgradePrefix(this, toScope));
	}

	public TypeRef rescope(Scope scope) {
		if (getScope().is(scope)) {
			return this;
		}
		return prefixWith(scope.pathTo(getScope()));
	}

	public final TypeRef rebuildIn(Scope scope) {

		final TypeRef typeRef = upgradeScope(scope);
		final Ref ref = typeRef.getRef().rebuildIn(scope);

		return ref.toTypeRef(getParameters().rebuildIn(scope));
	}

	public void resolveAll(FullResolver resolver) {
		this.allResolved = true;
		getContext().fullResolution().start();
		try {
			getRef().resolveAll(resolver);
		} finally {
			getContext().fullResolution().end();
		}
	}

	public TypeRef reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final Ref oldRef = getRef();
		final Ref newRef = oldRef.reproduce(reproducer);

		if (newRef == null) {
			return null;
		}

		final Ref oldIntactRef = getIntactRef();
		final Ref newIntactRef;

		if (oldIntactRef == oldRef) {
			newIntactRef = newRef;
		} else {
			newIntactRef = oldIntactRef.reproduce(reproducer);
			if (newIntactRef == null) {
				return null;
			}
		}

		final TypeRefParameters parameters =
				this.typeRefParameters.reproduce(reproducer);

		if (parameters == null) {
			return null;
		}

		return create(newIntactRef, newRef, parameters);
	}

	public final RefOp op(HostOp host) {
		return getRef().op(host);
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

	public final boolean assertFullyResolved() {
		assert this.allResolved :
			this + " is not fully resolved";
		return true;
	}

	@Override
	public String toString() {
		if (this.typeRefParameters == null) {
			return super.toString();
		}
		return this.ref.toString() + ' ' + this.typeRefParameters;
	}

	protected abstract TypeRef create(
			Ref intactRef,
			Ref ref,
			TypeRefParameters parameters);

	private TypeHolder get() {
		if (this.type != null) {
			return this.type;
		}

		final Resolution resolution = resolve(getScope().resolver());

		if (resolution.isError()) {
			return this.type = new TypeHolder(getContext().getNone(), false);
		}

		final Obj object = resolution.toObject();

		if (object == null) {
			getScope().getLogger().error(
					"not_type_ref",
					this,
					"Not a valid type reference");
			return this.type = new TypeHolder(getContext().getNone(), false);
		}

		return this.type = new TypeHolder(object, true);
	}

	private static final class TypeHolder {

		private final Obj object;
		private final boolean valid;

		TypeHolder(Obj object, boolean valid) {
			this.object = object;
			this.valid = valid;
		}

		public final Obj getObject() {
			return this.object;
		}

		public final boolean isValid() {
			return this.valid;
		}

		@Override
		public String toString() {
			if (this.object == null) {
				return super.toString();
			}
			if (this.valid) {
				return this.object.toString();
			}
			return "INVALID";
		}

	}

}

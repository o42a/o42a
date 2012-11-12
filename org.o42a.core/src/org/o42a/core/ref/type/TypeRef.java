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
package org.o42a.core.ref.type;

import static org.o42a.core.object.ConstructionMode.PROHIBITED_CONSTRUCTION;
import static org.o42a.core.ref.path.PrefixPath.upgradePrefix;
import static org.o42a.core.value.ValueStructFinder.DEFAULT_VALUE_STRUCT_FINDER;

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
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.*;
import org.o42a.util.log.Loggable;


public abstract class TypeRef implements ScopeInfo {

	public static TypeRef typeRef(
			Ref ref,
			ValueStructFinder valueStructFinder) {
		if (ref.isKnownStatic()) {
			return ref.toStaticTypeRef(valueStructFinder);
		}
		return new DefaultTypeRef(ref, vsFinder(valueStructFinder));
	}

	public static StaticTypeRef staticTypeRef(
			Ref ref,
			ValueStructFinder valueStructFinder) {
		return new StaticTypeRef(
				ref,
				ref.toStatic(),
				vsFinder(valueStructFinder));
	}

	private static ValueStructFinder vsFinder(ValueStructFinder vsFinder) {
		if (vsFinder != null) {
			return vsFinder;
		}
		return DEFAULT_VALUE_STRUCT_FINDER;
	}

	private final Ref ref;
	private final ValueStructFinder valueStructFinder;
	private ValueStruct<?, ?> valueStruct;
	private TypeRef ancestor;
	private TypeHolder type;
	private boolean allResolved;

	public TypeRef(Ref ref, ValueStructFinder valueStructFinder) {
		this.ref = ref;
		this.valueStructFinder = valueStructFinder;
	}

	@Override
	public final CompilerContext getContext() {
		return getRef().getContext();
	}

	@Override
	public final Loggable getLoggable() {
		return getRef().getLoggable();
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

	public ValueStruct<?, ?> getValueStruct() {
		if (this.valueStruct != null) {
			return this.valueStruct;
		}

		final ValueStruct<?, ?> defaultValueStruct =
				getRef().valueStruct(getScope());

		final ScopeInfo scoped;
		final ValueStruct<?, ?> valueStruct =
				this.valueStructFinder.valueStructBy(defaultValueStruct);

		if (valueStruct == null || valueStruct.isNone()) {
			scoped = defaultValueStruct.toScoped();
			this.valueStruct = defaultValueStruct;
		} else if (!valueStruct.isValid()) {
			scoped = valueStruct.toScoped();
			this.valueStruct = valueStruct;
		} else {
			assert defaultValueStruct.assertAssignableFrom(valueStruct);
			scoped = valueStruct.toScoped();
			this.valueStruct = valueStruct;
		}

		if (scoped != null) {
			assertSameScope(scoped);
		}

		return this.valueStruct;
	}

	public final TypeParameters getTypeParameters() {
		return getValueStruct().getParameters();
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

	public final Obj getType() {
		return get().getObject();
	}

	public final ValueType<?> getValueType() {
		return getValueStruct().getValueType();
	}

	public TypeRef setValueStruct(ValueStructFinder valueStructFinder) {

		final ValueStructFinder vsFinder;

		if (valueStructFinder != null) {
			vsFinder = valueStructFinder;
		} else {
			vsFinder = DEFAULT_VALUE_STRUCT_FINDER;
		}

		return create(getIntactRef(), getRef(), vsFinder);
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
				this.valueStructFinder);
	}

	public TypeRef prefixWith(PrefixPath prefix) {
		if (prefix.emptyFor(this)) {
			return this;
		}


		final Ref oldRef = getRef();
		final Ref newRef = oldRef.prefixWith(prefix);

		final ValueStructFinder vsFinder =
				this.valueStructFinder.prefixWith(prefix);

		if (oldRef == newRef && this.valueStructFinder == vsFinder) {
			return this;
		}

		final Ref oldIntactRef = getIntactRef();
		final Ref newIntactRef;

		if (oldIntactRef == oldRef) {
			newIntactRef = newRef;
		} else {
			newIntactRef = oldIntactRef.prefixWith(prefix);
		}

		return create(newIntactRef, newRef, vsFinder);
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

		return ref.toTypeRef(getValueStruct().rebuildIn(scope));
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

		final ValueStructFinder vsFinder =
				this.valueStructFinder.reproduce(reproducer);

		if (vsFinder == null) {
			return null;
		}

		return create(newIntactRef, newRef, vsFinder);
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
		if (this.ref == null) {
			return super.toString();
		}
		return this.ref.toString();
	}

	protected abstract TypeRef create(
			Ref intactRef,
			Ref ref,
			ValueStructFinder valueStructFinder);

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

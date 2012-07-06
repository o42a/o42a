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
import static org.o42a.core.ref.path.PrefixPath.emptyPrefix;
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

		final ValueStructFinder vsFinder;
		final ValueStruct<?, ?> valueStruct;

		if (valueStructFinder != null) {
			vsFinder = valueStructFinder;
			valueStruct = valueStructFinder.toValueStruct();
		} else {
			vsFinder = DEFAULT_VALUE_STRUCT_FINDER;
			valueStruct = null;
		}

		return new DefaultTypeRef(
				ref,
				emptyPrefix(ref.getScope()),
				vsFinder,
				valueStruct);
	}

	public static StaticTypeRef staticTypeRef(
			Ref ref,
			ValueStructFinder valueStructFinder) {

		final ValueStructFinder vsFinder;
		final ValueStruct<?, ?> valueStruct;

		if (valueStructFinder != null) {
			vsFinder = valueStructFinder;
			valueStruct = valueStructFinder.toValueStruct();
		} else {
			vsFinder = DEFAULT_VALUE_STRUCT_FINDER;
			valueStruct = null;
		}

		return new StaticTypeRef(
				ref.toStatic(),
				ref,
				emptyPrefix(ref.getScope()),
				vsFinder,
				valueStruct);
	}

	private final Ref unprefixedRef;
	private final PrefixPath prefix;
	private final ValueStructFinder valueStructFinder;
	private Ref ref;
	private ValueStruct<?, ?> valueStruct;
	private TypeRef ancestor;
	private TypeHolder type;
	private boolean allResolved;

	public TypeRef(
			Ref unprefixedRef,
			PrefixPath prefix,
			ValueStructFinder valueStructFinder,
			ValueStruct<?, ?> valueStruct) {
		this.unprefixedRef = unprefixedRef;
		this.prefix = prefix;
		this.valueStructFinder = valueStructFinder;
		this.valueStruct = valueStruct;
	}

	@Override
	public final CompilerContext getContext() {
		return getUnprefixedRef().getContext();
	}

	@Override
	public final Loggable getLoggable() {
		return getUnprefixedRef().getLoggable();
	}

	@Override
	public final Scope getScope() {
		return getPrefix().getStart();
	}

	public final PrefixPath getPrefix() {
		return this.prefix;
	}

	public abstract boolean isStatic();

	public final Ref getRef() {
		if (this.ref != null) {
			return this.ref;
		}
		return this.ref = getUnprefixedRef().prefixWith(getPrefix());
	}

	public final Ref getUnprefixedRef() {
		return this.unprefixedRef;
	}

	public abstract Ref getIntactRef();

	public ValueStruct<?, ?> getValueStruct() {
		if (this.valueStruct != null) {
			return this.valueStruct;
		}

		final ValueStruct<?, ?> defaultValueStruct =
				getUnprefixedRef().valueStruct(getUnprefixedRef().getScope());
		final ValueStruct<?, ?> valueStruct =
				this.valueStructFinder.valueStructBy(
						getUnprefixedRef(),
						defaultValueStruct);

		assert defaultValueStruct.assertAssignableFrom(valueStruct);

		return this.valueStruct = valueStruct.prefixWith(getPrefix());
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

		final TypeRef ancestor = getIntactRef().ancestor(this);

		return this.ancestor = ancestor.prefixWith(getPrefix());
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
		final ValueStruct<?, ?> valueStruct;

		if (valueStructFinder != null) {
			vsFinder = valueStructFinder;
			valueStruct = valueStructFinder.toValueStruct();
		} else {
			vsFinder = DEFAULT_VALUE_STRUCT_FINDER;
			valueStruct = null;
		}

		return create(
				getUnprefixedRef(),
				getIntactRef(),
				getPrefix(),
				vsFinder,
				valueStruct);
	}

	public final TypeRelation relationTo(TypeRef other) {
		return new DefaultTypeRelation(this, other);
	}

	public final boolean derivedFrom(TypeRef other) {
		return relationTo(other).isDerivative();
	}

	public StaticTypeRef toStatic() {
		return new StaticTypeRef(
				getUnprefixedRef(),
				getIntactRef(),
				getPrefix(),
				this.valueStructFinder,
				this.valueStruct);
	}

	public TypeRef prefixWith(PrefixPath prefix) {
		if (prefix.emptyFor(this)) {
			return this;
		}

		final PrefixPath oldPrefix = getPrefix();
		final PrefixPath newPrefix = oldPrefix.and(prefix);

		if (newPrefix == oldPrefix) {
			return this;
		}

		final ValueStruct<?, ?> valueStruct;

		if (this.valueStruct == null) {
			valueStruct = null;
		} else {
			valueStruct = this.valueStruct.prefixWith(prefix);
		}

		return create(
				getUnprefixedRef(),
				getIntactRef(),
				newPrefix,
				this.valueStructFinder,
				valueStruct);
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

		final Scope rescoped =
				getPrefix().rescope(reproducer.getReproducingScope());
		final Reproducer rescopedReproducer = reproducer.reproducerOf(rescoped);

		if (rescopedReproducer == null) {
			reproducer.getLogger().notReproducible(this);
			return null;
		}

		final PrefixPath prefix = getPrefix().reproduce(reproducer);

		if (prefix == null) {
			return null;
		}

		final Ref unprefixedRef =
				getUnprefixedRef().reproduce(rescopedReproducer);

		if (unprefixedRef == null) {
			return null;
		}

		final Ref intactRef;

		if (getUnprefixedRef() == getIntactRef()) {
			intactRef = unprefixedRef;
		} else {
			intactRef = getIntactRef().reproduce(rescopedReproducer);
			if (intactRef == null) {
				return null;
			}
		}

		final ValueStruct<?, ?> valueStruct;

		if (this.valueStruct == null) {
			valueStruct = null;
		} else {
			valueStruct = this.valueStruct.reproduce(reproducer);
			if (valueStruct == null) {
				return null;
			}
		}

		return create(
				unprefixedRef,
				intactRef,
				prefix,
				this.valueStructFinder,
				valueStruct);
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
		if (this.unprefixedRef == null) {
			return super.toString();
		}
		return this.unprefixedRef.toString();
	}

	protected abstract TypeRef create(
			Ref unprefixedRef,
			Ref intactRef,
			PrefixPath prefix,
			ValueStructFinder valueStructFinder,
			ValueStruct<?, ?> valueStruct);

	private TypeHolder get() {
		if (this.type != null) {
			return this.type;
		}

		final Resolution resolution = resolve(getScope().resolver());

		if (resolution.isError()) {
			return this.type = new TypeHolder(getContext().getFalse(), false);
		}

		final Obj object = resolution.toObject();

		if (object == null) {
			getScope().getLogger().error(
					"not_type_ref",
					this,
					"Not a valid type reference");
			return this.type = new TypeHolder(getContext().getFalse(), false);
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

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

import static org.o42a.core.ir.ObjectsCode.objectAncestor;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;
import static org.o42a.core.object.type.DerivationUsage.RUNTIME_DERIVATION_USAGE;

import java.util.IdentityHashMap;

import org.o42a.analysis.Analyzer;
import org.o42a.core.Contained;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.*;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.object.state.Dep;
import org.o42a.core.object.state.SyntheticDep;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.impl.ObjectConstructorStep;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.ValueAdapter;
import org.o42a.core.value.ValueRequest;


public abstract class ObjectConstructor
		extends Contained
		implements SyntheticDep {

	private final Construction construction = new Construction(this);
	private Obj constructed;
	private IdentityHashMap<Scope, Obj> propagated;

	public ObjectConstructor(LocationInfo location, Distributor distributor) {
		super(location, distributor);
	}

	/**
	 * Whether a constructed object may contain dependencies.
	 *
	 * <p>If the object may contain dependencies, then another object depending
	 * on this one will hold it as a dependency too.</p>
	 *
	 * @return <code>true</code> by default.
	 */
	public boolean mayContainDeps() {
		return true;
	}

	public final Nesting getNesting() {
		return this.construction;
	}

	public final Obj getConstructed() {
		if (this.constructed != null) {
			return this.constructed;
		}
		return this.constructed = createObject();
	}

	/**
	 * Whether the object construction is allowed inside prototype.
	 *
	 * <p>This is so only for special object constructors, like type parameter
	 * access.</p>
	 *
	 * @return <code>true</code> if object construction allowed inside prototype,
	 * or <code>false</code> otherwise. Returns <code>false</code> by default.
	 */
	public boolean isAllowedInsidePrototype() {
		return false;
	}

	public abstract TypeRef ancestor(LocationInfo location, Ref ref);

	public final Obj resolve(Scope scope) {
		if (scope.is(getScope())) {
			return getConstructed();
		}
		return propagate(scope);
	}

	public ValueAdapter valueAdapter(Ref ref, ValueRequest request) {
		return ref.typeParameters(ref.getScope()).valueAdapter(ref, request);
	}

	public abstract FieldDefinition fieldDefinition(Ref ref);

	public final Step toStep() {
		return new ObjectConstructorStep(this);
	}

	public final Path toPath() {
		return toStep().toPath();
	}

	public final Ref toRef() {
		return toPath().bind(this, getScope()).target(distribute());
	}

	public abstract ObjectConstructor reproduce(PathReproducer reproducer);

	@Override
	public boolean isSynthetic(Analyzer analyzer, Dep dep) {
		return !getConstructed().hasDeps(analyzer);
	}

	public PathOp op(PathOp host) {
		return new Op(host);
	}

	protected abstract Obj createObject();

	protected Obj propagateObject(Scope scope) {
		return new PropagatedConstructedObject(this, scope);
	}

	final Obj propagate(Scope scope) {
		assertCompatible(scope);
		if (this.propagated != null) {

			final Obj cached = this.propagated.get(scope);

			if (cached != null) {
				return cached;
			}
		} else {
			this.propagated = new IdentityHashMap<>();
		}

		final Obj propagated = propagateObject(scope);

		this.propagated.put(scope, propagated);

		return propagated;
	}

	private static final class Construction implements Nesting {

		private final ObjectConstructor constructor;

		Construction(ObjectConstructor constructor) {
			this.constructor = constructor;
		}

		@Override
		public Obj findObjectIn(Scope enclosing) {
			return this.constructor.propagate(enclosing);
		}

		@Override
		public String toString() {
			if (this.constructor == null) {
				return super.toString();
			}
			return this.constructor.toString();
		}

	}

	private final class Op extends PathOp {

		Op(PathOp start) {
			super(start);
		}

		@Override
		public HostValueOp value() {
			return targetValueOp();
		}

		@Override
		public HostOp target(CodeDirs dirs) {
			if (!getConstructed().type().derivation().isUsed(
					dirs.getGenerator().getAnalyzer(),
					RUNTIME_DERIVATION_USAGE)) {
				return exactObject(dirs);
			}
			return newObject(dirs);
		}

		@Override
		public String toString() {
			return String.valueOf(ObjectConstructor.this);
		}

		private HostOp exactObject(CodeDirs dirs) {

			final Obj sample = getConstructed();
			final ObjOp target = sample.ir(dirs.getGenerator()).op(
					getBuilder(),
					dirs.code());

			dirs.code().dumpName("Static object: ", target);
			target.fillDeps(dirs, host(), sample);

			return target;
		}

		private HostOp newObject(CodeDirs dirs) {

			final ObjectOp owner;
			final ObjectOp host = host().materialize(
					dirs,
					tempObjHolder(dirs.getAllocator()));

			if (host == null || host.getPrecision().isExact()) {
				owner = null;
			} else {
				owner = host;
			}

			return getBuilder().objects().newObject(
					dirs,
					host,
					tempObjHolder(dirs.getAllocator()),
					owner,
					objectAncestor(dirs, host, getConstructed()),
					getConstructed());
		}

	}

}

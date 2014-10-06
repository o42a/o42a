/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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

import static org.o42a.core.ir.object.ObjectOp.NEW_OBJECT_ID;
import static org.o42a.core.ir.object.op.CtrOp.allocateCtr;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;
import static org.o42a.core.object.type.DerivationUsage.DERIVATION_USAGE;

import java.util.IdentityHashMap;
import java.util.function.Function;

import org.o42a.analysis.Analyzer;
import org.o42a.codegen.code.Allocator;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.core.Contained;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.ir.field.local.LocalIROp;
import org.o42a.core.ir.object.AbstractObjectStoreOp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.op.*;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.object.state.Dep;
import org.o42a.core.object.state.SyntheticDep;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.impl.ObjectConstructorStep;
import org.o42a.core.ref.path.impl.SyntheticObjectConstructor;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.LocalRegistry;
import org.o42a.core.value.ValueAdapter;
import org.o42a.core.value.ValueRequest;
import org.o42a.util.string.ID;


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

	public boolean isEager() {
		return false;
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
	 * @return <code>true</code> if object construction allowed inside
	 * prototype, or <code>false</code> otherwise.
	 * Returns <code>false</code> by default.
	 */
	public boolean isAllowedInsidePrototype() {
		return false;
	}

	public abstract TypeRef ancestor(LocationInfo location, Ref ref);

	public abstract TypeRef iface(Ref ref);

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

	public void localMember(LocalRegistry registry) {
		registry.declareMemberLocal();
	}

	public final Step toStep() {
		return new ObjectConstructorStep(this);
	}

	public final Path toPath() {
		return toStep().toPath();
	}

	public final Ref toRef() {
		return toPath().bind(this, getScope()).target(distribute());
	}

	public final ObjectConstructor toSynthetic() {
		if (!mayContainDeps()) {
			return this;
		}
		return new SyntheticObjectConstructor(this);
	}

	public abstract ObjectConstructor reproduce(PathReproducer reproducer);

	@Override
	public boolean isSynthetic(Analyzer analyzer, Dep dep) {
		return !getConstructed().hasDeps(analyzer);
	}

	public HostOp op(HostOp host) {
		return new ConstructorOp(host);
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

	private final class ConstructorOp extends PathOp {

		ConstructorOp(HostOp host) {
			super(host);
		}

		@Override
		public HostValueOp value() {
			return pathValueOp();
		}

		@Override
		public ObjectOp pathTarget(CodeDirs dirs) {
			return createObject(dirs, tempObjHolder(dirs.getAllocator()));
		}

		@Override
		public TargetStoreOp allocateStore(ID id, Code code) {
			return new NewObjectStoreOp(id, code, this);
		}

		@Override
		public TargetStoreOp localStore(
				ID id,
				Function<CodeDirs, LocalIROp> getLocal) {
			return new NewObjectStoreOp(id, getLocal, this);
		}

		@Override
		public String toString() {
			return String.valueOf(ObjectConstructor.this);
		}

		private boolean isExact() {
			return !getConstructed().type().derivation().isUsed(
					getGenerator().getAnalyzer(),
					DERIVATION_USAGE);
		}

		private ObjectOp createObject(CodeDirs dirs, ObjHolder holder) {
			if (isExact()) {
				return exactObject(dirs);
			}
			return newObject(dirs, holder);
		}

		private ObjectOp exactObject(CodeDirs dirs) {

			final Obj sample = getConstructed();
			final ObjOp target = sample.ir(dirs.getGenerator()).exactOp(dirs);

			dirs.code().dumpName("Static object: ", target);
			target.fillDeps(dirs, host(), sample);

			return target;
		}

		private ObjectOp newObject(CodeDirs dirs, ObjHolder holder) {

			final ObjectOp host = host().materialize(
					dirs,
					tempObjHolder(dirs.getAllocator()));
			final CodeDirs subDirs = dirs.begin(
					NEW_OBJECT_ID,
					"New object: sample=`" + getConstructed() + "`");
			final Block code = subDirs.code();
			final ObjectOp result =
					allocateCtr(subDirs)
					.sample(getConstructed())
					.fillOwner(code, host)
					.evalAncestor(subDirs)
					.allocateObject(subDirs)
					.fillAncestor(code)
					.fillVmtc(code)
					.fillObject(subDirs)
					.newObject(subDirs, holder);

			subDirs.done();

			return result;
		}

	}

	private final class NewObjectStoreOp extends AbstractObjectStoreOp {

		private final ConstructorOp op;

		NewObjectStoreOp(ID id, Code code, ConstructorOp op) {
			super(id, code);
			this.op = op;
		}

		NewObjectStoreOp(
				ID id,
				Function<CodeDirs, LocalIROp> getLocal,
				ConstructorOp op) {
			super(id, getLocal);
			this.op = op;
		}

		@Override
		public Obj getWellKnownType() {
			return getConstructed();
		}

		@Override
		protected ObjectOp object(CodeDirs dirs, Allocator allocator) {

			final ObjHolder holder = tempObjHolder(
					allocator != null ? allocator : dirs.getAllocator());

			return this.op.createObject(dirs, holder);
		}

	}

}

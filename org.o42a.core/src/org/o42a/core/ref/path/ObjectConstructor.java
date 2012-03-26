/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import static org.o42a.core.ir.CodeBuilder.hostlessBuilder;
import static org.o42a.core.ir.CodeBuilder.objectAncestor;
import static org.o42a.core.object.def.Definitions.emptyDefinitions;
import static org.o42a.core.object.type.DerivationUsage.RUNTIME_DERIVATION_USAGE;

import java.util.IdentityHashMap;

import org.o42a.core.Distributor;
import org.o42a.core.Placed;
import org.o42a.core.Scope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.local.LocalOp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.PathOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectMembers;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.impl.ObjectConstructorStep;
import org.o42a.core.ref.path.impl.ObjectFieldDefinition;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.ValueAdapter;
import org.o42a.core.value.ValueStruct;


public abstract class ObjectConstructor extends Placed {

	private Obj constructed;
	private IdentityHashMap<Scope, Obj> propagated;

	public ObjectConstructor(LocationInfo location, Distributor distributor) {
		super(location, distributor);
	}

	public final Obj getConstructed() {
		if (this.constructed != null) {
			return this.constructed;
		}
		return this.constructed = createObject();
	}

	public abstract TypeRef ancestor(LocationInfo location);

	public final Obj resolve(Scope scope) {
		if (scope == getScope()) {
			return getConstructed();
		}
		return propagate(scope);
	}

	public ValueAdapter valueAdapter(
			Ref ref,
			ValueStruct<?, ?> expectedStruct) {
		return ref.valueStruct(ref.getScope()).defaultAdapter(
				ref,
				expectedStruct);
	}

	public FieldDefinition fieldDefinition(
			BoundPath path,
			Distributor distributor) {
		return new ObjectFieldDefinition(path, distributor);
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

	public abstract ObjectConstructor reproduce(PathReproducer reproducer);

	public PathOp op(PathOp host) {
		return new Op(host);
	}

	protected abstract Obj createObject();

	final Obj propagate(Scope scope) {
		assertCompatible(scope);
		if (this.propagated != null) {

			final Obj cached = this.propagated.get(scope);

			if (cached != null) {
				return cached;
			}
		} else {
			this.propagated = new IdentityHashMap<Scope, Obj>();
		}

		final Propagated propagated =
				new Propagated(scope, this, getConstructed());

		this.propagated.put(scope, propagated);

		return propagated;
	}

	private static final class Propagated extends Obj {

		private final ObjectConstructor constructor;
		private final StaticTypeRef propagatedFrom;

		Propagated(Scope scope, ObjectConstructor constructor, Obj sample) {
			super(
					constructor.distributeIn(scope.getContainer()),
					sample);
			this.constructor = constructor;
			this.propagatedFrom =
					constructor.toRef()
					.toStaticTypeRef()
					.upgradeScope(scope);
		}

		@Override
		public String toString() {
			return ("Propagated[" + this.propagatedFrom
					+ " / " + getScope().getEnclosingScope() + "]");
		}

		@Override
		protected Ascendants buildAscendants() {
			return new Ascendants(this).addImplicitSample(this.propagatedFrom);
		}

		@Override
		protected void declareMembers(ObjectMembers members) {
		}

		@Override
		protected Definitions explicitDefinitions() {
			return emptyDefinitions(this, getScope());
		}

		@Override
		protected Obj findObjectIn(Scope enclosing) {
			return this.constructor.propagate(enclosing);
		}

	}

	private final class Op extends PathOp {

		public Op(PathOp start) {
			super(start);
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
			final LocalOp local = host().toLocal();
			final ObjOp target = sample.ir(dirs.getGenerator()).op(
					getBuilder(),
					dirs.code());

			if (dirs.isDebug()) {
				dirs.code().dumpName(
						"Static object: ",
						target.toData(dirs.code()));
			}
			if (local != null) {
				target.fillDeps(dirs, sample);
			}

			return target;
		}

		private HostOp newObject(CodeDirs dirs) {

			final ObjectOp owner;
			final CodeBuilder ancestorBuilder;
			final HostOp ancestorHost;
			final LocalOp local = host().toLocal();

			if (local != null) {
				owner = null;
				ancestorBuilder = getBuilder();
				ancestorHost = local;
			} else {

				final ObjectOp ownerObject = host().materialize(dirs);

				if (ownerObject == null
						|| ownerObject.getPrecision().isExact()) {
					owner = null;
					ancestorBuilder = hostlessBuilder(
							getContext(),
							getBuilder().getFunction());
					ancestorHost = ancestorBuilder.host();
				} else {
					owner = ownerObject;
					ancestorBuilder = getBuilder();
					ancestorHost = ownerObject;
				}
			}

			return ancestorBuilder.newObject(
					dirs,
					owner,
					objectAncestor(
							dirs,
							ancestorHost,
							getConstructed()),
					getConstructed());
		}

	}

}

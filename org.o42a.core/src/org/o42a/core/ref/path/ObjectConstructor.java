/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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

import static org.o42a.core.def.Definitions.emptyDefinitions;
import static org.o42a.core.ir.CodeBuilder.codeBuilder;
import static org.o42a.core.ir.object.ObjectPrecision.DERIVED;
import static org.o42a.core.ir.op.ObjectRefFunc.OBJECT_REF;

import java.util.IdentityHashMap;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Function;
import org.o42a.core.Distributor;
import org.o42a.core.Placed;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.ObjectMembers;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.local.LocalOp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ObjectRefFunc;
import org.o42a.core.ir.op.PathOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.impl.path.ObjectConstructorStep;
import org.o42a.core.ref.impl.path.ObjectFieldDefinition;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
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
		return toPath().target(this, distribute());
	}

	public abstract ObjectConstructor reproduce(Reproducer reproducer);

	public PathOp op(PathOp host) {
		return new Op(host);
	}

	protected abstract Obj createObject();

	protected Obj propagateObject(Scope scope, Obj sample) {
		return new Propagated(scope, this, sample);
	}

	private Obj propagate(Scope scope) {
		if (this.propagated == null) {
			this.propagated = new IdentityHashMap<Scope, Obj>();
		} else {

			final Obj cached = this.propagated.get(scope);

			if (cached != null) {
				return cached;
			}
		}

		final Obj propagated = propagateObject(scope, getConstructed());

		this.propagated.put(scope, propagated);

		return propagated;
	}

	protected ObjectOp buildAncestor(CodeDirs dirs) {
		return dirs.getBuilder().objectAncestor(dirs, getConstructed());
	}

	private Function<ObjectRefFunc> ancestorFunc(CodeBuilder enclosing) {

		final Function<ObjectRefFunc> ancestorFunc =
				enclosing.getGenerator().newFunction().create(
						enclosing.nextId(),
						OBJECT_REF);

		final Code ancestorNotFound =
				ancestorFunc.addBlock("ancestor_not_found");
		final CodeBuilder builder = codeBuilder(
				ancestorFunc,
				ancestorNotFound.head(),
				getScope(),
				DERIVED);

		buildAncestorFunc(builder, ancestorFunc);
		if (ancestorNotFound.exists()) {
			ancestorNotFound.nullPtr().returnValue(ancestorNotFound);
		}

		return ancestorFunc;
	}

	private void buildAncestorFunc(CodeBuilder builder, Code code) {

		final Code ancestorFailed = code.addBlock("ancestor_failed");
		final ObjectOp ancestor = buildAncestor(
				builder.falseWhenUnknown(code, ancestorFailed.head()));

		if (ancestor == null) {
			code.nullPtr().returnValue(code);
		} else {
			ancestor.toAny(code).returnValue(code);
		}

		if (ancestorFailed.exists()) {
			getContext().getFalse()
			.ir(builder.getGenerator()).op(builder, ancestorFailed)
			.ptr().toAny(null, ancestorFailed).returnValue(ancestorFailed);
		}
	}

	private static final class Propagated extends Obj {

		private final StaticTypeRef propagatedFrom;

		Propagated(Scope scope, ObjectConstructor propagatedFrom, Obj sample) {
			super(
					propagatedFrom.distributeIn(scope.getContainer()),
					sample);
			this.propagatedFrom =
					propagatedFrom.toRef().toStaticTypeRef().upgradeScope(
							scope);
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

	}

	private final class Op extends PathOp {

		public Op(PathOp start) {
			super(start);
		}

		@Override
		public HostOp target(CodeDirs dirs) {
			final CodeBuilder builder = dirs.getBuilder();
			final LocalOp local = host().toLocal();

			if (local != null) {
				assert local.getBuilder() == builder :
					"Wrong builder used when instantiating local object: "
					+ this + ", while " + local.getBuilder() + " expected";
			}

			final Obj sample = getConstructed();

			if (!sample.type().runtimeConstruction().isUsedBy(
					dirs.getGenerator())) {

				final ObjOp target = sample.ir(dirs.getGenerator()).op(
						builder,
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

			final ObjectOp object = host().toObject(dirs);

			if (object != null) {
				return builder.newObject(
						dirs,
						object,
						ancestorFunc(builder).getPointer().op(
								null,
								dirs.code()),
						sample);
			}

			return builder.newObject(
					dirs,
					null,
					buildAncestor(dirs),
					sample);
		}

		@Override
		public String toString() {
			return ObjectConstructor.this.toString();
		}

	}

}

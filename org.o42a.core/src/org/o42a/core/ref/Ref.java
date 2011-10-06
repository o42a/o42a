/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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

import static org.o42a.core.artifact.link.TargetRef.targetRef;
import static org.o42a.core.def.Rescoper.transparentRescoper;
import static org.o42a.core.ref.path.Path.ROOT_PATH;

import org.o42a.codegen.code.Code;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.link.TargetRef;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.Rescoper;
import org.o42a.core.def.impl.rescoper.RefRescoper;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.local.Control;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.local.StOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.impl.*;
import org.o42a.core.ref.impl.cond.RefCondition;
import org.o42a.core.ref.impl.type.DefaultStaticTypeRef;
import org.o42a.core.ref.impl.type.DefaultTypeRef;
import org.o42a.core.ref.path.AbsolutePath;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.Statement;
import org.o42a.core.st.StatementEnv;
import org.o42a.core.value.*;
import org.o42a.util.Lambda;


public abstract class Ref extends Statement {

	public static Ref voidRef(LocationInfo location, Distributor distributor) {

		final Field<Obj> wrapperField =
				location.getContext().getIntrinsics().getVoidField();

		return ROOT_PATH.append(
				wrapperField.getKey()).target(location, distributor);
	}

	public static Ref falseRef(LocationInfo location, Distributor distributor) {

		final Obj falseObject = location.getContext().getFalse();
		final AbsolutePath falsePath = ROOT_PATH.append(
				falseObject.getScope().toMember().getKey());

		return falsePath.target(location, distributor);
	}

	public static Ref errorRef(LocationInfo location, Distributor distributor) {
		return new ErrorRef(location, distributor);
	}

	private Logical logical;
	private RefOp op;

	public Ref(LocationInfo location, Distributor distributor) {
		this(location, distributor, null);
	}

	protected Ref(
			LocationInfo location,
			Distributor distributor,
			Logical logical) {
		super(location, distributor);
		this.logical = logical;
	}

	public abstract boolean isConstant();

	public boolean isKnownStatic() {
		return false;
	}

	public boolean isStatic() {
		if (isKnownStatic()) {
			return true;
		}

		final Path path = getPath();

		if (path != null) {
			return path.isAbsolute();
		}

		return false;
	}

	public Path getPath() {
		return null;
	}

	public final ValueType<?> getValueType() {

		final ValueStruct<?, ?> valueStruct = valueStruct(getScope());

		return valueStruct != null ? valueStruct.getValueType() : null;
	}

	public final ValueStruct<?, ?> valueStruct(Scope scope) {

		final Resolution resolution = resolve(scope.dummyResolver());
		final ValueStruct<?, ?> valueStruct =
				resolution.materialize().value().getValueStruct();

		return valueStruct.rescope(toRescoper());
	}

	public final Logical getLogical() {
		if (this.logical == null) {
			this.logical = new RefLogical(this);
		}
		return this.logical;
	}

	public final Resolution getResolution() {
		return resolve(getScope().dummyResolver());
	}

	@Override
	public RefDefiner define(StatementEnv env) {
		return new RefDefiner(this, env);
	}

	public abstract Resolution resolve(Resolver resolver);

	public final Value<?> getValue() {
		return value(getScope().dummyResolver());
	}

	public Value<?> value(Resolver resolver) {
		return resolve(resolver).materialize()
				.value().explicitUseBy(resolver).getValue();
	}

	public ValueAdapter valueAdapter(ValueStruct<?, ?> expectedStruct) {
		return valueStruct(getScope()).defaultAdapter(this, expectedStruct);
	}

	/**
	 * Builds ancestor reference.
	 *
	 * <p>This returns ancestor of object or interface of the link. This
	 * shouldn't be called for e.g. arrays.</p>
	 *
	 * <p>If this reference is an object constructor, the ancestor should be
	 * built before object construction.</p>
	 *
	 * @param location the location of caller.
	 *
	 * @return ancestor reference or <code>null</code> if can not be determined.
	 */
	public TypeRef ancestor(LocationInfo location) {
		return new AncestorRef(location, this).toTypeRef();
	}

	public Ref materialize() {

		final Resolution resolution = getResolution();
		final Path materializationPath = resolution.materializationPath();

		if (materializationPath.isSelf()) {
			return this;
		}

		return materializationPath.target(this, distribute(), this);
	}

	public Path appendToPath(Path path) {
		return path.rebuildWithRef(this);
	}

	@Override
	public abstract Ref reproduce(Reproducer reproducer);

	public Ref toStatic() {
		if (isKnownStatic()) {
			return this;
		}
		return new StaticRef(this);
	}

	public final Ref adapt(LocationInfo location, StaticTypeRef adapterType) {
		return new Adapter(location, this, adapterType);
	}

	public final Ref rescope(Scope toScope) {
		if (getScope() == toScope) {
			return this;
		}
		return rescope(getScope().rescoperTo(toScope));
	}

	public Ref rescope(Rescoper rescoper) {
		if (rescoper.isTransparent()) {
			return this;
		}
		return new RescopedRef(this, rescoper);
	}

	public final TypeRef toTypeRef() {
		if (isKnownStatic()) {
			return toStaticTypeRef(null, null);
		}
		return toTypeRef(null, null);
	}

	public final TypeRef toTypeRef(ValueStruct<?, ?> valueStruct) {
		if (isKnownStatic()) {
			return toStaticTypeRef(null, valueStruct);
		}
		return toTypeRef(null, valueStruct);
	}

	public final TypeRef toTypeRef(
			Lambda<ValueStruct<?, ?>, Ref> valueStructFinder) {
		if (isKnownStatic()) {
			toStaticTypeRef(valueStructFinder, null);
		}
		return toTypeRef(valueStructFinder, null);
	}

	public final StaticTypeRef toStaticTypeRef() {
		return toStaticTypeRef(null, null);
	}

	public final StaticTypeRef toStaticTypeRef(
			ValueStruct<?, ?> valueStruct) {
		return toStaticTypeRef(null, valueStruct);
	}

	public final StaticTypeRef toStaticTypeRef(
			Lambda<ValueStruct<?, ?>, Ref> valueStructFinder) {
		return toStaticTypeRef(valueStructFinder, null);
	}

	public TargetRef toTargetRef(TypeRef typeRef) {
		return targetRef(this, typeRef);
	}

	public Rescoper toRescoper() {

		final Path path = getPath();

		if (path != null) {
			return path.rescoper(getScope());
		}

		return new RefRescoper(this);
	}

	public final Statement toCondition() {
		return new RefCondition(this);
	}

	public final FieldDefinition toFieldDefinition() {
		return createFieldDefinition();
	}

	public final RefOp op(HostOp host) {

		final RefOp op = this.op;

		if (op != null && op.host() == host) {
			return op;
		}

		assert assertFullyResolved();

		return this.op = createOp(host);
	}

	protected TypeRef toTypeRef(
			Lambda<ValueStruct<?, ?>, Ref> valueStructFinder,
			ValueStruct<?, ?> valueStruct) {
		return new DefaultTypeRef(
				this,
				transparentRescoper(getScope()),
				valueStructFinder,
				null);
	}

	protected StaticTypeRef toStaticTypeRef(
			Lambda<ValueStruct<?, ?>, Ref> valueStructFinder,
			ValueStruct<?, ?> valueStruct) {
		return new DefaultStaticTypeRef(
				this,
				this,
				transparentRescoper(getScope()),
				valueStructFinder,
				valueStruct);
	}

	protected abstract FieldDefinition createFieldDefinition();

	protected final FieldDefinition defaultFieldDefinition() {
		return new ValueFieldDefinition(this);
	}

	protected abstract RefOp createOp(HostOp host);

	@Override
	protected final StOp createOp(LocalBuilder builder) {
		return new RefStOp(builder, this, op(builder.host()));
	}

	private static final class RefStOp extends StOp {

		private final RefOp ref;

		RefStOp(
				LocalBuilder builder,
				Statement statement,
				RefOp ref) {
			super(builder, statement);
			this.ref = ref;
		}

		@Override
		public void writeAssignment(Control control, ValOp result) {

			final Code code = control.code();
			final ValDirs dirs =
					control.getBuilder().falseWhenUnknown(
							code,
							control.falseDir())
					.value(code.id("local_val"), result);

			result.store(code, this.ref.writeValue(dirs));

			dirs.done();

			control.returnValue();
		}

		@Override
		public void writeLogicalValue(Control control) {

			final CodeDirs dirs = control.getBuilder().falseWhenUnknown(
					control.code(),
					control.falseDir());

			this.ref.writeLogicalValue(dirs);
		}

	}

}

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
import static org.o42a.core.ref.path.PathResolver.fullPathResolver;
import static org.o42a.core.ref.path.PathResolver.pathResolver;
import static org.o42a.core.ref.path.PathResolver.valuePathResolver;
import static org.o42a.core.value.ValueStructFinder.DEFAULT_VALUE_STRUCT_FINDER;

import org.o42a.codegen.code.Code;
import org.o42a.core.Distributor;
import org.o42a.core.Rescopable;
import org.o42a.core.Scope;
import org.o42a.core.artifact.link.TargetRef;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.Rescoper;
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
import org.o42a.core.ref.impl.Adapter;
import org.o42a.core.ref.impl.RefLogical;
import org.o42a.core.ref.impl.cond.RefCondition;
import org.o42a.core.ref.impl.path.ErrorStep;
import org.o42a.core.ref.impl.type.DefaultStaticTypeRef;
import org.o42a.core.ref.impl.type.DefaultTypeRef;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.Statement;
import org.o42a.core.st.StatementEnv;
import org.o42a.core.value.*;


public class Ref extends Statement implements Rescopable<Ref> {

	public static Ref voidRef(LocationInfo location, Distributor distributor) {

		final Field<Obj> wrapperField =
				location.getContext().getIntrinsics().getVoidField();

		return ROOT_PATH.append(wrapperField.getKey())
				.bind(location, distributor.getScope())
				.target(distributor);
	}

	public static Ref falseRef(LocationInfo location, Distributor distributor) {

		final Obj falseObject = location.getContext().getFalse();
		final Path falsePath = ROOT_PATH.append(
				falseObject.getScope().toMember().getKey());

		return falsePath.bind(location, distributor.getScope())
				.target(distributor);
	}

	public static Ref errorRef(LocationInfo location, Distributor distributor) {
		return ErrorStep.ERROR_STEP.toPath().bindStatically(
				location,
				distributor.getScope())
				.target(distributor);
	}

	private final BoundPath path;
	private Logical logical;
	private RefOp op;

	public Ref(BoundPath path, Distributor distributor) {
		super(path, distributor);
		this.path = path;
	}

	public final boolean isConstant() {
		return isStatic() && getResolution().isConstant();
	}

	public final boolean isKnownStatic() {
		return getPath().getRawPath().isStatic();
	}

	public final boolean isStatic() {
		return getPath().isStatic();
	}

	public final BoundPath getPath() {
		return this.path;
	}

	public final BoundPath bindPath() {

		final BoundPath path = getPath();

		if (path.getOrigin() == getScope()) {
			return path;
		}

		return path.getRawPath().bind(this, getScope());
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

	public Resolution resolve(Resolver resolver) {
		assertCompatible(resolver.getScope());
		return resolve(resolver, pathResolver(resolver));
	}

	public final Value<?> getValue() {
		return value(getScope().dummyResolver());
	}

	public Value<?> value(Resolver resolver) {
		return resolve(resolver).materialize()
				.value().explicitUseBy(resolver).getValue();
	}

	public final ValueAdapter valueAdapter(ValueStruct<?, ?> expectedStruct) {

		final Step[] steps = getPath().getSteps();

		if (steps.length == 0) {
			return valueStruct(getScope()).defaultAdapter(this, expectedStruct);
		}

		final Step lastStep = steps[steps.length - 1];

		return lastStep.valueAdapter(this, expectedStruct);
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
	public final TypeRef ancestor(LocationInfo location) {
		return getPath().ancestor(location, distribute());
	}

	public final Ref materialize() {

		final BoundPath path = getPath();
		final BoundPath materialized = path.materialize();

		if (materialized == path) {
			return this;
		}

		return materialized.target(distribute());
	}

	@Override
	public Ref reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final BoundPath path = getPath();

		if (path.isAbsolute()) {
			return path.target(reproducer.distribute());
		}

		final PathReproduction pathReproduction = path.reproduce(reproducer);

		if (pathReproduction == null) {
			return null;
		}
		if (pathReproduction.isUnchanged()) {
			if (!reproducer.isTopLevel()) {
				return reproducePart(
						reproducer,
						pathReproduction.getExternalPath());
			}
			// Top-level reproducer`s scope is not compatible with path
			// and requires rescoping.
			return startWithPrefix(
					reproducer,
					pathReproduction,
					reproducer.getPhrasePrefix()
					.getPath()
					.getRawPath()
					.materialize());
		}

		if (!pathReproduction.isOutOfClause()) {
			return reproducePart(
					reproducer,
					pathReproduction.getReproducedPath());
		}

		return startWithPrefix(
				reproducer,
				pathReproduction,
				pathReproduction.getReproducedPath().append(
						reproducer.getPhrasePrefix()
						.getPath()
						.getRawPath()
						.materialize()));
	}

	private Ref reproducePart(Reproducer reproducer, Path path) {
		return path.bind(this, reproducer.getScope()).target(
				reproducer.distribute());
	}

	private Ref startWithPrefix(
			Reproducer reproducer,
			PathReproduction pathReproduction,
			Path phrasePrefix) {

		final Path externalPath = pathReproduction.getExternalPath();

		if (externalPath.isSelf()) {
			return phrasePrefix.bind(this, reproducer.getScope()).target(
					reproducer.distribute());
		}

		return phrasePrefix.append(externalPath)
				.bind(this, reproducer.getScope())
				.target(reproducer.distribute());
	}

	public final Ref toStatic() {
		if (isKnownStatic()) {
			return this;
		}
		return getPath().toStatic().target(distribute());
	}

	public final Ref adapt(LocationInfo location, StaticTypeRef adapterType) {

		final Adapter adapter = new Adapter(location, adapterType);

		return getPath().materialize()
				.append(adapter.toPath())
				.target(distribute());
	}

	public final Ref rescope(Scope toScope) {
		if (getScope() == toScope) {
			return this;
		}
		return rescope(getScope().rescoperTo(this, toScope));
	}

	@Override
	public final Ref rescope(Rescoper rescoper) {
		return rescoper.update(this);
	}

	@Override
	public final Ref rescope(BoundPath path) {
		return path.append(getPath()).target(
				distributeIn(path.getOrigin().getContainer()));
	}

	@Override
	public final Ref upgradeScope(Scope scope) {
		if (getScope() == scope) {
			return this;
		}
		return getPath().target(distributeIn(scope.getContainer()));
	}

	public final TypeRef toTypeRef() {
		if (isKnownStatic()) {
			return toStaticTypeRef(null);
		}
		return toTypeRef(null);
	}

	public TypeRef toTypeRef(ValueStructFinder valueStructFinder) {
		if (isKnownStatic()) {
			toStaticTypeRef(valueStructFinder);
		}

		final ValueStructFinder vsFinder = vsFinder(valueStructFinder);

		return new DefaultTypeRef(
				this,
				transparentRescoper(getScope()),
				vsFinder,
				vsFinder.toValueStruct());
	}

	public final StaticTypeRef toStaticTypeRef() {
		return toStaticTypeRef(null);
	}

	public StaticTypeRef toStaticTypeRef(ValueStructFinder valueStructFinder) {

		final ValueStructFinder vsFinder = vsFinder(valueStructFinder);

		return new DefaultStaticTypeRef(
				this,
				this,
				transparentRescoper(getScope()),
				vsFinder,
				vsFinder.toValueStruct());
	}

	public final TargetRef toTargetRef(TypeRef typeRef) {
		return targetRef(this, typeRef);
	}

	public final Rescoper toRescoper() {
		return bindPath().toRescoper();
	}

	public final Statement toCondition() {
		return new RefCondition(this);
	}

	public final FieldDefinition toFieldDefinition() {
		return getPath().fieldDefinition(distribute());
	}

	public final RefOp op(HostOp host) {

		final RefOp op = this.op;

		if (op != null && op.host() == host) {
			return op;
		}

		assert assertFullyResolved();

		return this.op = new RefOp(host, this);
	}

	@Override
	public String toString() {
		if (this.path == null) {
			return super.toString();
		}
		return this.path.toString();
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		resolve(resolver, fullPathResolver(resolver)).resolveAll();
	}

	@Override
	protected void fullyResolveValues(Resolver resolver) {

		final Resolution resolution =
				resolve(resolver, valuePathResolver(resolver));

		resolution.resolveValues(resolver);
	}

	private Resolution resolve(Resolver resolver, PathResolver pathResolver) {
		return resolver.path(pathResolver, getPath(), resolver.getScope());
	}

	@Override
	protected final StOp createOp(LocalBuilder builder) {
		return new RefStOp(builder, this, op(builder.host()));
	}

	private static ValueStructFinder vsFinder(ValueStructFinder finder) {
		if (finder != null) {
			return finder;
		}
		return DEFAULT_VALUE_STRUCT_FINDER;
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

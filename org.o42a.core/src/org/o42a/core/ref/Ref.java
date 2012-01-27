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

import static org.o42a.core.artifact.link.TargetRef.targetRef;
import static org.o42a.core.ref.path.Path.FALSE_PATH;
import static org.o42a.core.ref.path.Path.VOID_PATH;
import static org.o42a.core.ref.path.PrefixPath.emptyPrefix;
import static org.o42a.core.ref.path.PrefixPath.upgradePrefix;
import static org.o42a.core.value.ValueStructFinder.DEFAULT_VALUE_STRUCT_FINDER;

import org.o42a.codegen.code.Code;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.link.TargetRef;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.local.Control;
import org.o42a.core.ir.local.StOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.member.local.LocalResolver;
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
import org.o42a.core.st.*;
import org.o42a.core.value.*;


public class Ref extends Statement {

	public static Ref voidRef(LocationInfo location, Distributor distributor) {
		return VOID_PATH.bind(location, distributor.getScope()).target(
				distributor);
	}

	public static Ref falseRef(LocationInfo location, Distributor distributor) {
		return FALSE_PATH.bind(location, distributor.getScope()).target(
				distributor);
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
	private InlineValue inline;

	public Ref(BoundPath path, Distributor distributor) {
		super(path, distributor);
		this.path = path;
	}

	public final boolean isConstant() {
		if (!isStatic()) {
			return false;
		}

		final Definitions definitions =
				getResolution().materialize().value().getDefinitions();

		return definitions.isConstant();
	}

	public final boolean isKnownStatic() {
		return getPath().isKnownStatic();
	}

	public final boolean isStatic() {
		return getPath().isStatic();
	}

	public final BoundPath getPath() {
		return this.path;
	}

	public final ValueType<?> getValueType() {

		final ValueStruct<?, ?> valueStruct = valueStruct(getScope());

		return valueStruct != null ? valueStruct.getValueType() : null;
	}

	public final ValueStruct<?, ?> valueStruct(Scope scope) {

		final Resolution resolution = resolve(scope.dummyResolver());
		final ValueStruct<?, ?> valueStruct =
				resolution.materialize().value().getValueStruct();

		return valueStruct.prefixWith(getPath().toPrefix(scope));
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

	public final Resolution resolve(Resolver resolver) {
		assertCompatible(resolver.getScope());
		return new Resolution(this, resolver);
	}

	public final Value<?> getValue() {
		return value(getScope().dummyResolver());
	}

	public Value<?> value(Resolver resolver) {
		return resolve(resolver)
				.materialize()
				.value()
				.explicitUseBy(resolver)
				.getValue()
				.prefixWith(getPath().toPrefix(resolver.getScope()));
	}

	public final ValueAdapter valueAdapter(ValueStruct<?, ?> expectedStruct) {

		final Step lastStep = getPath().lastStep();

		if (lastStep == null) {
			return valueStruct(getScope()).defaultAdapter(this, expectedStruct);
		}

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

	@Override
	public Ref reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final BoundPath path = getPath();

		if (path.isSelf()) {
			return Path.SELF_PATH
					.bind(this, reproducer.getScope())
					.target(reproducer.distribute());
		}

		final PathReproducer pathReproducer = path.reproducer(reproducer);
		final PathReproduction pathReproduction =
				pathReproducer.reproducePath();

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
					.getPath());
		}

		if (!pathReproduction.isOutOfClause()) {
			return reproducePart(
					reproducer,
					pathReproducer.reproduceBindings(
							pathReproduction.getReproducedPath()));
		}

		return startWithPrefix(
				reproducer,
				pathReproduction,
				pathReproducer.reproduceBindings(
						pathReproduction.getReproducedPath())
				.bind(this, reproducer.getScope())
				.append(reproducer.getPhrasePrefix().getPath()));
	}

	public InlineValue inline(Normalizer normalizer, Scope origin) {

		final NormalPath normalPath = getPath().normalize(normalizer, origin);

		if (!normalPath.isNormalized()) {
			return null;
		}

		return new Inline(valueStruct(origin), normalPath);
	}

	@Override
	public InlineCommand inlineImperative(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct) {

		final InlineValue inline = inline(normalizer, getScope());

		if (inline == null) {
			return null;
		}

		return new InlineCmd(inline);
	}

	@Override
	public void normalizeImperative(Normalizer normalizer) {
		this.inline = inline(normalizer, getScope());
	}

	public final Ref toStatic() {
		if (isKnownStatic()) {
			return this;
		}
		return getPath().toStatic().target(distribute());
	}

	public final Ref adapt(LocationInfo location, StaticTypeRef adapterType) {

		final Adapter adapter = new Adapter(location, adapterType);

		return getPath().append(adapter.toPath()).target(distribute());
	}

	public final Ref prefixWith(PrefixPath prefix) {
		if (prefix.emptyFor(this)) {
			return this;
		}

		final BoundPath path = getPath().prefixWith(prefix);

		return path.target(distributeIn(prefix.getStart().getContainer()));
	}

	public final Ref upgradeScope(Scope toScope) {
		if (getScope() == toScope) {
			return this;
		}
		return prefixWith(upgradePrefix(this, toScope));
	}

	public final Ref rescope(Scope toScope) {
		if (getScope() == toScope) {
			return this;
		}
		return prefixWith(toScope.pathTo(getScope()));
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
				emptyPrefix(getScope()),
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
				emptyPrefix(getScope()),
				vsFinder,
				vsFinder.toValueStruct());
	}

	public final TargetRef toTargetRef(TypeRef typeRef) {
		return targetRef(this, typeRef);
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
	protected void fullyResolveImperative(LocalResolver resolver) {
		resolve(resolver).resolveValue();
	}

	@Override
	protected final StOp createOp(CodeBuilder builder) {
		if (this.inline != null) {
			return new InlineOp(builder, this, this.inline);
		}
		return new Op(builder, this, op(builder.host()));
	}

	final void refFullyResolved() {
		fullyResolved();
	}

	private static ValueStructFinder vsFinder(ValueStructFinder finder) {
		if (finder != null) {
			return finder;
		}
		return DEFAULT_VALUE_STRUCT_FINDER;
	}

	private Ref reproducePart(Reproducer reproducer, Path path) {
		return path.bind(this, reproducer.getScope()).target(
				reproducer.distribute());
	}

	private Ref startWithPrefix(
			Reproducer reproducer,
			PathReproduction pathReproduction,
			BoundPath phrasePrefix) {

		final Path externalPath = pathReproduction.getExternalPath();

		if (externalPath.isSelf()) {
			return phrasePrefix.target(reproducer.distribute());
		}

		return phrasePrefix.append(externalPath)
				.target(reproducer.distribute());
	}

	private static final class Inline extends InlineValue {

		private final NormalPath normalPath;

		Inline(ValueStruct<?, ?> valueStruct, NormalPath normalPath) {
			super(valueStruct);
			this.normalPath = normalPath;
		}

		@Override
		public void writeCond(CodeDirs dirs, HostOp host) {
			this.normalPath.writeLogicalValue(dirs, host);
		}

		@Override
		public ValOp writeValue(ValDirs dirs, HostOp host) {
			return this.normalPath.writeValue(dirs, host);
		}

		@Override
		public void cancel() {
			this.normalPath.cancel();
		}

		@Override
		public String toString() {
			if (this.normalPath == null) {
				return super.toString();
			}
			return this.normalPath.toString();
		}

	}

	private static final class Op extends StOp {

		private final RefOp ref;

		Op(
				CodeBuilder builder,
				Statement statement,
				RefOp ref) {
			super(builder, statement);
			this.ref = ref;
		}

		@Override
		public void writeLogicalValue(Control control) {

			final CodeDirs dirs = control.getBuilder().falseWhenUnknown(
					control.code(),
					control.falseDir());

			this.ref.writeLogicalValue(dirs);
		}

		@Override
		public void writeValue(Control control, ValOp result) {

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

	}

	private static final class InlineOp extends StOp {

		private final InlineValue inline;

		InlineOp(
				CodeBuilder builder,
				Statement statement,
				InlineValue inline) {
			super(builder, statement);
			this.inline = inline;
		}

		@Override
		public void writeLogicalValue(Control control) {

			final CodeDirs dirs = control.getBuilder().falseWhenUnknown(
					control.code(),
					control.falseDir());

			this.inline.writeCond(dirs, getBuilder().host());
		}

		@Override
		public void writeValue(Control control, ValOp result) {

			final Code code = control.code();
			final ValDirs dirs =
					control.getBuilder().falseWhenUnknown(
							code,
							control.falseDir())
					.value(code.id("local_val"), result);

			result.store(
					code,
					this.inline.writeValue(dirs, getBuilder().host()));

			dirs.done();

			control.returnValue();
		}

		@Override
		public String toString() {
			if (this.inline == null) {
				return super.toString();
			}
			return this.inline.toString();
		}

	}

	private static final class InlineCmd implements InlineCommand {

		private final InlineValue inline;

		InlineCmd(InlineValue inline) {
			this.inline = inline;
		}

		@Override
		public void writeCond(Control control) {

			final CodeDirs dirs = control.getBuilder().falseWhenUnknown(
					control.code(),
					control.falseDir());

			this.inline.writeCond(dirs, control.host());
		}

		@Override
		public void writeValue(Control control, ValOp result) {

			final Code code = control.code();
			final ValDirs dirs =
					control.getBuilder().falseWhenUnknown(
							code,
							control.falseDir())
					.value(code.id("local_val"), result);

			result.store(
					code,
					this.inline.writeValue(dirs, control.host()));

			dirs.done();

			control.returnValue();
		}

		@Override
		public void cancel() {
			this.inline.cancel();
		}

		@Override
		public String toString() {
			if (this.inline == null) {
				return super.toString();
			}
			return this.inline.toString();
		}

	}

}

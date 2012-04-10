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

import static org.o42a.core.object.link.TargetRef.targetRef;
import static org.o42a.core.ref.path.Path.FALSE_PATH;
import static org.o42a.core.ref.path.Path.VOID_PATH;
import static org.o42a.core.ref.path.PrefixPath.upgradePrefix;
import static org.o42a.core.ref.type.TypeRef.staticTypeRef;
import static org.o42a.core.ref.type.TypeRef.typeRef;

import org.o42a.analysis.Analyzer;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.link.TargetRef;
import org.o42a.core.ref.impl.Adapter;
import org.o42a.core.ref.impl.RefLogical;
import org.o42a.core.ref.impl.cond.RefCondition;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.path.impl.ErrorStep;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.*;
import org.o42a.core.value.*;
import org.o42a.util.fn.Cancelable;


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

	public Ref(LocationInfo location, Distributor distributor, BoundPath path) {
		super(location, distributor);
		this.path = path;
	}

	public final boolean isConstant() {
		if (!isStatic()) {
			return false;
		}

		final Definitions definitions =
				getResolution().toObject().value().getDefinitions();

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
				resolution.toObject().value().getValueStruct();

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
	public RefDefiner define(DefinerEnv env) {
		return new RefDefiner(this, env);
	}

	@Override
	public RefCommand command(CommandEnv env) {
		return new RefCommand(this, env);
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
				.toObject()
				.value()
				.explicitUseBy(resolver)
				.getValue()
				.prefixWith(getPath().toPrefix(resolver.getScope()));
	}

	public final ValueAdapter valueAdapter(
			ValueStruct<?, ?> expectedStruct,
			boolean adapt) {

		final Step lastStep = getPath().lastStep();

		if (lastStep == null) {
			return valueStruct(getScope()).defaultAdapter(
					this,
					expectedStruct,
					adapt);
		}

		return lastStep.valueAdapter(this, expectedStruct, adapt);
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
			return reproducer.getPhrasePrefix();
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

	public final InlineValue inline(Normalizer normalizer, Scope origin) {

		final NormalPath normalPath = getPath().normalize(normalizer, origin);

		if (!normalPath.isNormalized()) {
			return null;
		}

		return new InlineRef(valueStruct(origin), normalPath);
	}

	public final void normalize(Analyzer analyzer) {
		getPath().normalizePath(analyzer);
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
		return typeRef(this, valueStructFinder);
	}

	public final StaticTypeRef toStaticTypeRef() {
		return toStaticTypeRef(null);
	}

	public final StaticTypeRef toStaticTypeRef(
			ValueStructFinder valueStructFinder) {
		return staticTypeRef(this, valueStructFinder);
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

	final void refFullyResolved() {
		fullyResolved();
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

	private static final class InlineRef extends InlineValue {

		private final NormalPath normalPath;

		InlineRef(ValueStruct<?, ?> valueStruct, NormalPath normalPath) {
			super(null, valueStruct);
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
		public String toString() {
			if (this.normalPath == null) {
				return super.toString();
			}
			return this.normalPath.toString();
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

}

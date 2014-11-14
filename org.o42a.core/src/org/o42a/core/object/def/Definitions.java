/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.core.object.def;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.object.def.DefTarget.NO_DEF_TARGET;
import static org.o42a.core.object.def.DefTarget.UNKNOWN_DEF_TARGET;
import static org.o42a.core.object.def.impl.DefTargetFinder.defTarget;
import static org.o42a.core.ref.RefUsage.TARGET_REF_USAGE;
import static org.o42a.core.ref.ScopeUpgrade.wrapScope;
import static org.o42a.core.value.TypeParameters.typeParameters;
import static org.o42a.util.fn.Init.init;

import org.o42a.core.Scope;
import org.o42a.core.Scoped;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.impl.InlineDefinitions;
import org.o42a.core.object.meta.EscapeAnalyzer;
import org.o42a.core.object.meta.EscapeFlag;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.DefValue;
import org.o42a.core.value.*;
import org.o42a.core.value.Void;
import org.o42a.core.value.link.TargetResolver;
import org.o42a.util.fn.Init;


public class Definitions extends Scoped {

	public static final Defs NO_DEFS = new Defs();

	public static Definitions emptyDefinitions(
			LocationInfo location,
			Scope scope) {
		return new Empty(location, scope);
	}

	public static Definitions noValueDefinitions(
			LocationInfo location,
			Scope scope,
			TypeParameters<?> typeParameters) {
		return new Definitions(
				location,
				scope,
				typeParameters,
				NO_DEFS);
	}

	private final TypeParameters<?> typeParameters;
	private final Defs defs;

	private final Init<Value<?>> constant = init(this::detectConstant);
	private final Init<DefTarget> target = init(this::buildTarget);

	Definitions(
			LocationInfo location,
			Scope scope,
			TypeParameters<?> typeParameters,
			Defs defs) {
		super(location, scope);
		assert defs.assertValid(scope);
		this.typeParameters = typeParameters;
		this.defs = defs;
		assert assertEmptyWithoutValues();
	}

	private Definitions(LocationInfo location, Scope scope) {
		this(location, scope, null, NO_DEFS);
	}

	Definitions(
			Definitions prototype,
			TypeParameters<?> typeParameters,
			Defs defs) {
		this(
				prototype,
				prototype.getScope(),
				typeParameters,
				defs);
	}

	public final ValueType<?> getValueType() {

		final TypeParameters<?> typeParameters = getTypeParameters();

		return typeParameters != null ? typeParameters.getValueType() : null;
	}

	public final boolean hasValues() {
		return getTypeParameters() != null;
	}

	public final TypeParameters<?> getTypeParameters() {
		return this.typeParameters;
	}

	public boolean isEmpty() {
		return false;
	}

	public final boolean isConstant() {
		return getConstant().getKnowledge().isKnown();
	}

	public final Value<?> getConstant() {
		return this.constant.get();
	}

	public final EscapeFlag escapeFlag(EscapeAnalyzer analyzer) {
		for (Def def : defs().get()) {

			final EscapeFlag escapeFlag = def.escapeFlag(analyzer);

			if (!escapeFlag.isEscapeImpossible()) {
				return escapeFlag;
			}
		}

		return analyzer.escapeImpossible();
	}

	public final Defs defs() {
		return this.defs;
	}

	public final boolean areDerived() {
		return defs().areDerived();
	}

	public final boolean areInherited() {
		return defs().areInherited();
	}

	public final boolean areYielding() {
		return defs().areYielding();
	}

	public Value<?> value(Resolver resolver) {
		assertCompatible(resolver.getScope());

		final DefValue value = defs().value(resolver);

		if (value.hasValue()) {
			return value.getValue();
		}

		final TypeParameters<?> typeParameters = getTypeParameters();
		final Condition condition = value.getCondition();

		if (!condition.isConstant()) {
			return typeParameters.runtimeValue();
		}

		return typeParameters.falseValue();
	}

	public final Definitions override(Definitions overriders) {
		assertSameScope(overriders);
		if (overriders.isEmpty()) {
			return this;
		}
		if (isEmpty()) {
			return overriders;
		}

		final TypeParameters<?> typeParameters =
				getTypeParameters() != null
				? getTypeParameters() : overriders.getTypeParameters();
		final Defs newDefs = defs().override(overriders.defs());

		return new Definitions(this, typeParameters, newDefs);
	}

	public final Definitions addVoid() {
		if (isEmpty()) {
			return new Definitions(
					this,
					typeParameters(this, ValueType.VOID),
					new Defs(new VoidDef(this)));
		}

		final Def[] defs = defs().get();

		if (defs.length != 0) {

			final Def last = defs[defs.length - 1];

			if (last.getConstantValue().hasValue()) {
				return this;
			}
		}

		return new Definitions(
				this,
				getTypeParameters(),
				defs().add(new VoidDef(this)));
	}

	public final Definitions wrapBy(Scope wrapperScope) {
		return upgradeScope(wrapScope(wrapperScope, getScope()));
	}

	public final Definitions upgradeScope(Scope scope) {
		if (scope.is(getScope())) {
			return this;
		}
		return upgradeScope(ScopeUpgrade.upgradeScope(this, scope));
	}

	public final Definitions toVoid() {
		if (isEmpty() || getValueType().isVoid()) {
			return this;
		}
		return new Definitions(
				this,
				typeParameters(this, ValueType.VOID),
				defs().toVoid());
	}

	public final Definitions upgradeTypeParameters(
			TypeParameters<?> typeParameters) {

		final TypeParameters<?> objectTypeParameters = getTypeParameters();

		if (objectTypeParameters != null
				&& typeParameters.relationTo(objectTypeParameters).isSame()) {
			return this;
		}

		return new Definitions(this, typeParameters, defs());
	}

	public void resolveTargets(TargetResolver resolver) {
		defs().resolveTargets(resolver);
	}

	public final boolean updatedSince(Obj ascendant) {
		return defs().updatedSince(ascendant);
	}

	public final InlineValue inline(Normalizer normalizer) {

		final InlineEval defs = defs().inline(normalizer);

		if (normalizer.isCancelled()) {
			return null;
		}

		return new InlineDefinitions(getValueType(), defs);
	}

	public final void resolveAll() {
		getContext().fullResolution().start();
		try {
			defs().resolveAll(this);

			final Ref targetRef = target().getRef();

			if (targetRef != null) {
				targetRef.resolveAll(
						getScope()
						.getEnclosingScope()
						.resolver()
						.fullResolver(dummyUser(), TARGET_REF_USAGE));
			}
		} finally {
			getContext().fullResolution().end();
		}
	}

	public final void normalize(RootNormalizer normalizer) {
		defs().normalize(normalizer);

		final Ref targetRef = target().getRef();

		if (targetRef != null) {
			// It is necessary to attempt to normalize the target,
			// especially when normalization fails.
			// This properly updates the use graph.
			targetRef.normalize(normalizer.getAnalyzer());
		}
	}

	public final DefTarget target() {
		return this.target.get();
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();
		final TypeParameters<?> typeParameters = getTypeParameters();

		if (typeParameters != null) {
			out.append("Definitions(");
			out.append(typeParameters);
			out.append(")[");
		} else {
			out.append("Definitions[");
		}

		boolean comma = false;

		comma = this.defs.defsToString(out, comma);

		out.append(']');

		return out.toString();
	}

	private boolean assertEmptyWithoutValues() {
		assert hasValues() || defs().areEmpty() :
				"Non-empty definitions should have a value type";
		return true;
	}

	private Definitions upgradeScope(ScopeUpgrade scopeUpgrade) {
		if (!scopeUpgrade.upgradeOf(this)) {
			return this;
		}

		final Scope resultScope = scopeUpgrade.getFinalScope();

		if (isEmpty()) {
			return emptyDefinitions(this, resultScope);
		}

		final Defs defs = defs();
		final Defs newDefs = defs.upgradeScope(scopeUpgrade);
		final TypeParameters<?> oldTypeParameters = getTypeParameters();
		final TypeParameters<?> newTypeParameters =
				oldTypeParameters != null
				? oldTypeParameters.prefixWith(scopeUpgrade.toPrefix())
				: null;

		if (resultScope.is(getScope())
				// This may fail when there is no definitions.
				&& oldTypeParameters == newTypeParameters
				&& defs == newDefs) {
			return this;
		}

		return new Definitions(
				this,
				resultScope,
				newTypeParameters,
				newDefs);
	}

	private Value<?> detectConstant() {

		final DefValue constant = defs().getConstant();

		if (constant.hasValue()) {
			return constant.getValue();
		}

		final Condition condition = constant.getCondition();

		if (condition.isConstant()) {
			if (condition.isTrue()
					&& getScope().toObject().type().getValueType().isVoid()) {
				return typeParameters(
						this,
						ValueType.VOID).compilerValue(Void.VOID);
			}
			return getTypeParameters().falseValue();
		}

		return getTypeParameters().runtimeValue();
	}

	private DefTarget buildTarget() {

		final Obj cloneOf = getScope().toObject().getCloneOf();

		if (cloneOf != null) {
			return cloneOf.value().getDefinitions().target();
		}
		if (!getValueType().isLink()) {
			return NO_DEF_TARGET;
		}

		final DefTarget target = defs().target();

		if (target == null) {
			return UNKNOWN_DEF_TARGET;
		}

		final Ref ref = target.getRef();

		if (ref == null) {
			return target;
		}

		final BoundPath targetPath =
				defTarget(ref.getPath(), getScope().getEnclosingScope());

		if (targetPath == null) {
			return NO_DEF_TARGET;
		}

		assert targetPath.getOrigin().is(getScope().getEnclosingScope()) :
			"Wrong target scope: " + targetPath.getOrigin()
			+ ", but " + getScope().getEnclosingScope() + " expected";

		return new DefTarget(targetPath.target(
				ref.distributeIn(targetPath.getOrigin().getContainer())));
	}

	private static final class Empty extends Definitions {

		Empty(LocationInfo location, Scope scope) {
			super(location, scope);
		}

		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public String toString() {
			return "Empty Definitions";
		}

	}

}

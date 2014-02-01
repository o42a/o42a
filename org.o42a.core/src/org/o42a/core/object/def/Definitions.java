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

import static org.o42a.core.object.def.DefTarget.NO_DEF_TARGET;
import static org.o42a.core.object.def.impl.DefTargetFinder.defTarget;
import static org.o42a.core.ref.RefUsage.TARGET_REF_USAGE;
import static org.o42a.core.ref.RefUser.dummyRefUser;
import static org.o42a.core.ref.ScopeUpgrade.wrapScope;
import static org.o42a.core.value.TypeParameters.typeParameters;

import org.o42a.core.Scope;
import org.o42a.core.Scoped;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.impl.InlineDefinitions;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.DefValue;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.link.TargetResolver;


public class Definitions extends Scoped {

	static final Defs NO_CLAIMS = new Defs(true);
	static final Defs NO_PROPOSITIONS = new Defs(false);

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
				NO_CLAIMS,
				NO_PROPOSITIONS);
	}

	private final TypeParameters<?> typeParameters;
	private final Defs claims;
	private final Defs propositions;

	private Value<?> constant;
	private DefTarget target;

	Definitions(
			LocationInfo location,
			Scope scope,
			TypeParameters<?> typeParameters,
			Defs claims,
			Defs propositions) {
		super(location, scope);
		assert claims.assertValid(scope, true);
		assert propositions.assertValid(scope, false);
		this.typeParameters = typeParameters;
		this.claims = claims;
		this.propositions = propositions;
		assert assertEmptyWithoutValues();
	}

	private Definitions(LocationInfo location, Scope scope) {
		this(
				location,
				scope,
				null,
				NO_CLAIMS,
				NO_PROPOSITIONS);
	}

	Definitions(
			Definitions prototype,
			TypeParameters<?> typeParameters,
			Defs claims,
			Defs propositions) {
		this(
				prototype,
				prototype.getScope(),
				typeParameters,
				claims,
				propositions);
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
		if (this.constant != null) {
			return this.constant;
		}

		final DefValue claim = claims().getConstant();

		if (claim.hasValue()) {
			return this.constant = claim.getValue();
		}

		switch (claim.getCondition()) {
		case FALSE:
			return this.constant = getTypeParameters().falseValue();
		case RUNTIME:
			return this.constant = getTypeParameters().runtimeValue();
		case TRUE:
			break;
		}

		final DefValue proposition = propositions().getConstant();

		if (proposition.hasValue()) {
			return this.constant = proposition.getValue();
		}
		if (proposition.getCondition().isConstant()) {
			return this.constant = getTypeParameters().falseValue();
		}

		return this.constant = getTypeParameters().runtimeValue();
	}

	public final Defs claims() {
		return this.claims;
	}

	public final Defs propositions() {
		return this.propositions;
	}

	public final Defs defs(boolean claim) {
		if (claim) {
			return claims();
		}
		return propositions();
	}

	public final boolean onlyClaims() {
		return propositions().isEmpty();
	}

	public final boolean noClaims() {
		return claims().isEmpty();
	}

	public final boolean hasInherited() {
		return claims().hasInherited() || propositions().hasInherited();
	}

	public Value<?> value(Resolver resolver) {
		assertCompatible(resolver.getScope());

		final DefValue claim = claims().value(resolver);

		if (claim.hasValue()) {
			return claim.getValue();
		}

		switch (claim.getCondition()) {
		case FALSE:
			return getTypeParameters().falseValue();
		case RUNTIME:
			return getTypeParameters().runtimeValue();
		case TRUE:
			break;
		}

		final DefValue proposition = propositions().value(resolver);

		if (proposition.hasValue()) {
			return proposition.getValue();
		}
		if (proposition.getCondition().isConstant()) {
			return getTypeParameters().falseValue();
		}

		return getTypeParameters().runtimeValue();
	}

	public Definitions refine(Definitions refinements) {
		assertSameScope(refinements);
		if (refinements.isEmpty()) {
			return this;
		}
		if (isEmpty()) {
			return refinements;
		}

		final TypeParameters<?> typeParameters =
				getTypeParameters() != null
				? getTypeParameters() : refinements.getTypeParameters();
		final Defs newClaims = claims().add(refinements.claims());
		final Defs newPropositions;

		if (newClaims.unconditional()) {
			newPropositions = NO_PROPOSITIONS;
		} else {
			newPropositions = refinements.propositions().add(propositions());
		}

		return new Definitions(
				this,
				typeParameters,
				newClaims,
				newPropositions);
	}

	public final Definitions override(Definitions overriders) {
		return refine(overriders);
	}

	public Definitions claim() {
		if (onlyClaims()) {
			return this;
		}
		return new Definitions(
				this,
				getTypeParameters(),
				propositions().claim(claims()),
				NO_PROPOSITIONS);
	}

	public Definitions unclaim() {
		if (noClaims()) {
			return this;
		}
		return new Definitions(
				this,
				getTypeParameters(),
				NO_CLAIMS,
				claims().unclaim(propositions()));
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
				claims().toVoid(),
				propositions().toVoid());
	}

	public final Definitions upgradeTypeParameters(
			TypeParameters<?> typeParameters) {

		final TypeParameters<?> objectTypeParameters = getTypeParameters();

		if (objectTypeParameters != null
				&& typeParameters.relationTo(objectTypeParameters).isSame()) {
			return this;
		}

		final boolean claimsOk =
				claims().upgradeTypeParameters(this, typeParameters);
		final boolean propositionsOk =
				propositions().upgradeTypeParameters(this, typeParameters);

		if (!claimsOk || !propositionsOk) {
			return this;
		}

		return new Definitions(
				this,
				typeParameters,
				claims(),
				propositions());
	}

	public void resolveTargets(TargetResolver resolver) {
		claims().resolveTargets(resolver);
		propositions().resolveTargets(resolver);
	}

	public final boolean updatedSince(Obj ascendant) {
		if (claims().updatedSince(ascendant)) {
			return true;
		}
		return propositions().updatedSince(ascendant);
	}

	public final InlineValue inline(Normalizer normalizer) {

		final InlineEval claim = claims().inline(normalizer);
		final InlineEval proposition = propositions().inline(normalizer);

		if (normalizer.isCancelled()) {
			return null;
		}

		return new InlineDefinitions(getValueType(), claim, proposition);
	}

	public final void resolveAll() {
		getContext().fullResolution().start();
		try {
			claims().resolveAll(this);
			propositions().resolveAll(this);

			final Ref targetRef = target().getRef();

			if (targetRef != null) {
				targetRef.resolveAll(
						getScope()
						.getEnclosingScope()
						.resolver()
						.fullResolver(dummyRefUser(), TARGET_REF_USAGE));
			}
		} finally {
			getContext().fullResolution().end();
		}
	}

	public final void normalize(RootNormalizer normalizer) {
		claims().normalize(normalizer);
		propositions().normalize(normalizer);

		final Ref targetRef = target().getRef();

		if (targetRef != null) {
			// It is necessary to attempt to normalize the target,
			// especially when normalization fails.
			// This properly updates the use graph.
			targetRef.normalize(normalizer.getAnalyzer());
		}
	}

	public DefTarget target() {
		if (this.target != null) {
			return this.target;
		}

		final Obj cloneOf = getScope().toObject().getCloneOf();

		if (cloneOf != null) {
			return this.target = cloneOf.value().getDefinitions().target();
		}
		if (!getValueType().isLink()) {
			return this.target = NO_DEF_TARGET;
		}

		final DefTarget claimedTarget = claims().target();

		if (claimedTarget != null) {
			return setTarget(claimedTarget);
		}

		final DefTarget proposedTarget = propositions().target();

		if (proposedTarget != null) {
			return setTarget(proposedTarget);
		}

		return this.target = DefTarget.UNKNOWN_DEF_TARGET;
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

		comma = this.claims.defsToString(out, comma);
		comma = this.propositions.defsToString(out, comma);

		out.append(']');

		return out.toString();
	}

	private boolean assertEmptyWithoutValues() {
		assert (hasValues()
				|| (propositions().isEmpty() && claims().isEmpty())) :
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

		final Defs claims = claims();
		final Defs newClaims = claims.upgradeScope(scopeUpgrade);
		final Defs propositions = propositions();
		final Defs newPropositions = propositions.upgradeScope(scopeUpgrade);
		final TypeParameters<?> oldTypeParameters = getTypeParameters();
		final TypeParameters<?> newTypeParameters =
				oldTypeParameters != null
				? oldTypeParameters.prefixWith(scopeUpgrade.toPrefix())
				: null;

		if (resultScope.is(getScope())
				// This may fail when there is no definitions.
				&& oldTypeParameters == newTypeParameters
				&& claims == newClaims
				&& propositions == newPropositions) {
			return this;
		}

		return new Definitions(
				this,
				resultScope,
				newTypeParameters,
				newClaims,
				newPropositions);
	}

	private DefTarget setTarget(DefTarget target) {

		final Ref ref = target.getRef();

		if (ref == null) {
			return this.target = target;
		}

		final BoundPath targetPath =
				defTarget(ref.getPath(), getScope().getEnclosingScope());

		if (targetPath == null) {
			return this.target = NO_DEF_TARGET;
		}

		assert targetPath.getOrigin().is(getScope().getEnclosingScope()) :
			"Wrong target scope: " + targetPath.getOrigin()
			+ ", but " + getScope().getEnclosingScope() + " expected";

		return this.target = new DefTarget(targetPath.target(
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

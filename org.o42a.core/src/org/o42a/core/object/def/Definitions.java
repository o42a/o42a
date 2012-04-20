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
package org.o42a.core.object.def;

import static org.o42a.core.object.def.DefKind.*;
import static org.o42a.core.object.def.DefTarget.NO_DEF_TARGET;
import static org.o42a.core.object.def.DefTarget.UNKNOWN_DEF_TARGET;
import static org.o42a.core.object.def.impl.DefTargetFinder.defTarget;
import static org.o42a.core.ref.Logical.logicalTrue;
import static org.o42a.core.ref.ScopeUpgrade.wrapScope;

import org.o42a.core.Scope;
import org.o42a.core.Scoped;
import org.o42a.core.ir.op.InlineCond;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.impl.InlineDefinitions;
import org.o42a.core.object.link.LinkValueStruct;
import org.o42a.core.object.link.TargetResolver;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.*;


public class Definitions extends Scoped {

	static final CondDefs NO_REQUIREMENTS = new CondDefs(REQUIREMENT);
	static final CondDefs NO_CONDITIONS = new CondDefs(CONDITION);
	static final ValueDefs NO_CLAIMS = new ValueDefs(CLAIM);
	static final ValueDefs NO_PROPOSITIONS = new ValueDefs(PROPOSITION);

	public static Definitions emptyDefinitions(
			LocationInfo location,
			Scope scope) {
		return new Empty(location, scope);
	}

	public static Definitions noValueDefinitions(
			LocationInfo location,
			Scope scope,
			ValueStruct<?, ?> valueStruct) {

		final CondDefs conditions = new CondDefs(
				DefKind.CONDITION,
				logicalTrue(location, scope).toCondDef());

		return new Definitions(
				location,
				scope,
				valueStruct,
				NO_REQUIREMENTS,
				conditions,
				NO_CLAIMS,
				NO_PROPOSITIONS);
	}

	private final ValueStruct<?, ?> valueStruct;
	private final CondDefs requirements;
	private final CondDefs conditions;
	private final ValueDefs claims;
	private final ValueDefs propositions;

	private Value<?> constant;
	private DefTarget target;

	Definitions(
			LocationInfo location,
			Scope scope,
			ValueStruct<?, ?> valueStruct,
			CondDefs requirements,
			CondDefs conditions,
			ValueDefs claims,
			ValueDefs propositions) {
		super(location, scope);
		assert requirements.assertValid(scope, REQUIREMENT);
		assert conditions.assertValid(scope, CONDITION);
		assert claims.assertValid(scope, CLAIM);
		assert propositions.assertValid(scope, PROPOSITION);
		this.valueStruct = valueStruct;
		this.requirements = requirements;
		this.conditions = conditions;
		this.claims = claims;
		this.propositions = propositions;
		assert assertEmptyWithoutValues();
	}

	private Definitions(LocationInfo location, Scope scope) {
		this(
				location,
				scope,
				null,
				NO_REQUIREMENTS,
				NO_CONDITIONS,
				NO_CLAIMS,
				NO_PROPOSITIONS);
	}

	Definitions(
			Definitions prototype,
			ValueStruct<?, ?> valueStruct,
			CondDefs requirements,
			CondDefs conditions,
			ValueDefs claims,
			ValueDefs propositions) {
		this(
				prototype,
				prototype.getScope(),
				valueStruct,
				requirements,
				conditions,
				claims,
				propositions);
	}

	public final ValueType<?> getValueType() {

		final ValueStruct<?, ?> valueStruct = getValueStruct();

		return valueStruct != null ? valueStruct.getValueType() : null;
	}

	public final boolean hasValues() {
		return getValueStruct() != null;
	}

	public final ValueStruct<?, ?> getValueStruct() {
		return this.valueStruct;
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

		final ValueStruct<?, ?> valueStruct =
				isEmpty() ? ValueStruct.VOID : getValueStruct();

		switch (requirements().getConstant()) {
		case RUNTIME:
			return valueStruct.runtimeValue();
		case FALSE:
			return valueStruct.falseValue();
		case TRUE:
		case UNKNOWN:
			switch (conditions().getConstant()) {
			case TRUE:
				break;
			case RUNTIME:
				return valueStruct.runtimeValue();
			case UNKNOWN:
			case FALSE:
				return valueStruct.falseValue();
			}
			break;
		}

		final Value<?> claim = claims().constant(this);

		if (!claim.getKnowledge().hasUnknownCondition()) {
			return claim;
		}

		return propositions().constant(this);
	}

	public final CondDefs requirements() {
		return this.requirements;
	}

	public final CondDefs conditions() {
		return this.conditions;
	}

	public final ValueDefs claims() {
		return this.claims;
	}

	public final ValueDefs propositions() {
		return this.propositions;
	}

	public final Defs<?, ?> defs(DefKind defKind) {
		switch (defKind) {
		case REQUIREMENT:
			return requirements();
		case CONDITION:
			return conditions();
		case CLAIM:
			return claims();
		case PROPOSITION:
			return propositions();
		}
		throw new IllegalArgumentException(
				"Unknown definition kind: " + defKind);
	}

	public final boolean onlyClaims() {
		return propositions().isEmpty() && conditions().isEmpty();
	}

	public final boolean noClaims() {
		return claims().isEmpty() && requirements().isEmpty();
	}

	public Value<?> value(Resolver resolver) {

		final Condition requirement = requirements().condition(resolver);

		if (!requirement.isConstant()) {
			return requirement.toValue(valueStruct());
		}
		if (requirement.isFalse() && !requirement.isUnknown()) {
			return requirement.toValue(getValueStruct());
		}

		final Condition condition = conditions().condition(resolver);

		if (!condition.isConstant()) {
			return condition.toValue(valueStruct());
		}
		if (condition.isFalse()) {
			return condition.toValue(valueStruct());
		}

		final Value<?> claim = claims().value(this, resolver);

		if (!claim.getKnowledge().hasUnknownCondition()) {
			return claim;
		}

		return propositions().value(this, resolver);
	}

	public Definitions refine(Definitions refinements) {
		assertSameScope(refinements);
		if (refinements.isEmpty()) {
			return this;
		}
		if (isEmpty()) {
			return refinements;
		}

		final ValueStruct<?, ?> valueStruct =
				getValueStruct() != null
				? getValueStruct() : refinements.getValueStruct();
		final ValueDefs newClaims = claims().add(refinements.claims());
		final ValueDefs newPropositions;

		if (newClaims.unconditional()) {
			newPropositions = NO_PROPOSITIONS;
		} else {
			newPropositions = refinements.propositions().add(propositions());
		}

		return new Definitions(
				this,
				valueStruct,
				requirements(),
				conditions(),
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
				getValueStruct(),
				conditions().claim(requirements()),
				NO_CONDITIONS,
				propositions().claim(claims()),
				NO_PROPOSITIONS);
	}

	public Definitions unclaim() {
		if (noClaims()) {
			return this;
		}
		return new Definitions(
				this,
				getValueStruct(),
				NO_REQUIREMENTS,
				requirements().unclaim(conditions()),
				NO_CLAIMS,
				claims().unclaim(propositions()));
	}

	public final Definitions wrapBy(Scope wrapperScope) {
		return upgradeScope(wrapScope(wrapperScope, getScope()));
	}

	public final Definitions upgradeScope(Scope scope) {
		if (scope == getScope()) {
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
				ValueStruct.VOID,
				requirements(),
				conditions(),
				claims().toVoid(),
				propositions().toVoid());
	}

	public final Definitions upgradeValueStruct(ValueStruct<?, ?> valueStruct) {

		final ValueStruct<?, ?> objectValueStruct = getValueStruct();

		if (objectValueStruct != null
				&& valueStruct.relationTo(objectValueStruct).isSame()) {
			return this;
		}

		final boolean claimsOk =
				claims().upgradeValueStruct(this, valueStruct);
		final boolean propositionsOk =
				propositions().upgradeValueStruct(this, valueStruct);

		if (!claimsOk || !propositionsOk) {
			return this;
		}

		return new Definitions(
				this,
				valueStruct,
				requirements(),
				conditions(),
				claims(),
				propositions());
	}

	public void resolveTargets(TargetResolver resolver) {
		claims().resolveTargets(resolver);
		propositions().resolveTargets(resolver);
	}

	public Definitions runtime() {
		return new Definitions(
				this,
				getValueStruct(),
				requirements(),
				conditions(),
				claims().runtime(this),
				propositions().runtime(this));
	}

	public final boolean updatedSince(Obj ascendant) {
		if (requirements().updatedSince(ascendant)) {
			return true;
		}
		if (conditions().updatedSince(ascendant)) {
			return true;
		}
		if (claims().updatedSince(ascendant)) {
			return true;
		}
		return propositions().updatedSince(ascendant);
	}

	public final InlineValue inline(Normalizer normalizer) {

		final InlineCond requirement = requirements().inline(normalizer);
		final InlineCond condition = conditions().inline(normalizer);
		final InlineValue claim = claims().inline(normalizer, this);
		final InlineValue proposition = propositions().inline(normalizer, this);

		return normalizer.isCancelled() ? null : new InlineDefinitions(
				requirement,
				condition,
				claim,
				proposition);
	}

	public final void resolveAll() {
		getContext().fullResolution().start();
		try {
			requirements().resolveAll(this);
			conditions().resolveAll(this);
			claims().resolveAll(this);
			propositions().resolveAll(this);

			final Ref targetRef = target().getRef();

			if (targetRef != null) {
				targetRef.resolve(
						getScope().getEnclosingScope().dummyResolver())
						.resolveTarget();
			}
		} finally {
			getContext().fullResolution().end();
		}
	}

	public final void normalize(RootNormalizer normalizer) {
		requirements().normalize(normalizer);
		conditions().normalize(normalizer);
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

		final LinkValueStruct linkStruct = getValueStruct().toLinkStruct();

		if (linkStruct == null) {
			return this.target = NO_DEF_TARGET;
		}

		final CondDefs requirements = requirements();

		if (!requirements.isEmpty() && !requirements.getConstant().isTrue()) {
			return this.target = NO_DEF_TARGET;
		}

		final CondDefs conditions = conditions();

		if (!conditions.isEmpty() && !conditions.getConstant().isTrue()) {
			return this.target = NO_DEF_TARGET;
		}
		if (!claims().isEmpty()) {
			return this.target = NO_DEF_TARGET;
		}

		final ValueDef[] defs = propositions().get();

		if (defs.length == 0) {
			return this.target = UNKNOWN_DEF_TARGET;
		}
		if (defs.length != 1) {
			return this.target = NO_DEF_TARGET;
		}

		final Ref target = defs[0].target();

		if (target == null) {
			return this.target = NO_DEF_TARGET;
		}

		final BoundPath targetPath =
				defTarget(target.getPath(), getScope().getEnclosingScope());

		if (targetPath == null) {
			return this.target = NO_DEF_TARGET;
		}

		assert targetPath.getOrigin() == getScope().getEnclosingScope() :
			"Wrong target scope: " + targetPath.getOrigin()
			+ ", but " + getScope().getEnclosingScope() + " expected";

		return this.target = new DefTarget(targetPath.target(
				target.distributeIn(targetPath.getOrigin().getContainer())));
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();
		final ValueStruct<?, ?> valueStruct = getValueStruct();

		if (valueStruct != null) {
			out.append("Definitions(");
			out.append(valueStruct);
			out.append(")[");
		} else {
			out.append("Definitions[");
		}

		boolean comma = false;

		comma = this.requirements.defsToString(out, comma);
		comma = this.conditions.defsToString(out, comma);
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

	private final ValueStruct<?, ?> valueStruct() {
		return this.valueStruct != null ? this.valueStruct : ValueStruct.VOID;
	}

	private Definitions upgradeScope(ScopeUpgrade scopeUpgrade) {
		if (!scopeUpgrade.upgradeOf(this)) {
			return this;
		}

		final Scope resultScope = scopeUpgrade.getFinalScope();

		if (isEmpty()) {
			return emptyDefinitions(this, resultScope);
		}

		final CondDefs requirements = requirements();
		final CondDefs newRequirements =
				requirements.upgradeScope(scopeUpgrade);
		final CondDefs conditions = conditions();
		final CondDefs newConditions = conditions.upgradeScope(scopeUpgrade);
		final ValueDefs claims = claims();
		final ValueDefs newClaims = claims.upgradeScope(scopeUpgrade);
		final ValueDefs propositions = propositions();
		final ValueDefs newPropositions =
				propositions.upgradeScope(scopeUpgrade);
		final ValueStruct<?, ?> valueStruct = getValueStruct();
		final ValueStruct<?, ?> newValueStruct =
				valueStruct != null
				? valueStruct.prefixWith(scopeUpgrade.toPrefix())
				: null;

		if (resultScope == getScope()
				// This may fail when there is no definitions.
				&& valueStruct == newValueStruct
				&& requirements == newRequirements
				&& conditions == newConditions
				&& claims == newClaims
				&& propositions == newPropositions) {
			return this;
		}

		return new Definitions(
				this,
				resultScope,
				newValueStruct,
				newRequirements,
				newConditions,
				newClaims,
				newPropositions);
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

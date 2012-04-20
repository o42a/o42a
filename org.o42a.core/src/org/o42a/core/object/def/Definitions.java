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

import static org.o42a.core.object.def.DefTarget.NO_DEF_TARGET;
import static org.o42a.core.object.def.DefTarget.UNKNOWN_DEF_TARGET;
import static org.o42a.core.object.def.impl.DefTargetFinder.defTarget;
import static org.o42a.core.ref.ScopeUpgrade.wrapScope;

import org.o42a.core.Scope;
import org.o42a.core.Scoped;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.impl.InlineDefinitions;
import org.o42a.core.object.link.LinkValueStruct;
import org.o42a.core.object.link.TargetResolver;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;


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
			ValueStruct<?, ?> valueStruct) {
		return new Definitions(
				location,
				scope,
				valueStruct,
				NO_CLAIMS,
				NO_PROPOSITIONS);
	}

	private final ValueStruct<?, ?> valueStruct;
	private final Defs claims;
	private final Defs propositions;

	private Value<?> constant;
	private DefTarget target;

	Definitions(
			LocationInfo location,
			Scope scope,
			ValueStruct<?, ?> valueStruct,
			Defs claims,
			Defs propositions) {
		super(location, scope);
		assert claims.assertValid(scope, true);
		assert propositions.assertValid(scope, false);
		this.valueStruct = valueStruct;
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
			ValueStruct<?, ?> valueStruct,
			Defs claims,
			Defs propositions) {
		this(
				prototype,
				prototype.getScope(),
				valueStruct,
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

		final Value<?> claim = claims().constant(this);

		if (!claim.getKnowledge().hasUnknownCondition()) {
			return claim;
		}

		return propositions().constant(this);
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

	public Value<?> value(Resolver resolver) {

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
		final Defs newClaims = claims().add(refinements.claims());
		final Defs newPropositions;

		if (newClaims.unconditional()) {
			newPropositions = NO_PROPOSITIONS;
		} else {
			newPropositions = refinements.propositions().add(propositions());
		}

		return new Definitions(
				this,
				valueStruct,
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
				claims().runtime(this),
				propositions().runtime(this));
	}

	public final boolean updatedSince(Obj ascendant) {
		if (claims().updatedSince(ascendant)) {
			return true;
		}
		return propositions().updatedSince(ascendant);
	}

	public final InlineValue inline(Normalizer normalizer) {

		final InlineValue claim = claims().inline(normalizer, this);
		final InlineValue proposition = propositions().inline(normalizer, this);

		return normalizer.isCancelled() ? null : new InlineDefinitions(
				claim,
				proposition);
	}

	public final void resolveAll() {
		getContext().fullResolution().start();
		try {
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
		if (!claims().isEmpty()) {
			return this.target = NO_DEF_TARGET;
		}

		final Def[] defs = propositions().get();

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
		final ValueStruct<?, ?> valueStruct = getValueStruct();
		final ValueStruct<?, ?> newValueStruct =
				valueStruct != null
				? valueStruct.prefixWith(scopeUpgrade.toPrefix())
				: null;

		if (resultScope == getScope()
				// This may fail when there is no definitions.
				&& valueStruct == newValueStruct
				&& claims == newClaims
				&& propositions == newPropositions) {
			return this;
		}

		return new Definitions(
				this,
				resultScope,
				newValueStruct,
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

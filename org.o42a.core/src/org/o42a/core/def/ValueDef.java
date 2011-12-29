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
package org.o42a.core.def;

import static org.o42a.core.def.Definitions.NO_CLAIMS;
import static org.o42a.core.def.Definitions.NO_PROPOSITIONS;
import static org.o42a.core.def.Definitions.NO_REQUIREMENTS;
import static org.o42a.core.ref.Logical.logicalTrue;

import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.impl.InlineValueDef;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.*;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.*;


public abstract class ValueDef extends Def<ValueDef> {

	private Value<?> constantValue;
	private CondDef condition;

	public ValueDef(
			Obj source,
			LocationInfo location,
			ScopeUpgrade scopeUpgrade) {
		super(source, location, DefKind.PROPOSITION, scopeUpgrade);
	}

	protected ValueDef(ValueDef prototype, ScopeUpgrade scopeUpgrade) {
		super(prototype, scopeUpgrade);
	}

	public final boolean isClaim() {
		return getKind().isClaim();
	}

	public boolean isLocal() {
		return false;
	}

	public final ValueType<?> getValueType() {
		return getValueStruct().getValueType();
	}

	public abstract ValueStruct<?, ?> getValueStruct();

	public final Value<?> getConstantValue() {
		if (this.constantValue != null) {
			return this.constantValue;
		}
		if (hasPrerequisite()) {

			final Logical prerequisite = getPrerequisite();

			if (!prerequisite.isTrue()) {
				if (prerequisite.isFalse()) {
					return this.constantValue = getValueStruct().unknownValue();
				}
				return this.constantValue = getValueStruct().runtimeValue();
			}
		}
		if (!hasConstantValue()) {
			return this.constantValue = getValueStruct().runtimeValue();
		}

		final Resolver resolver =
				getScopeUpgrade().rescope(getScope().dummyResolver());

		return this.constantValue = calculateValue(resolver);
	}

	@Override
	public boolean impliesWhenAfter(ValueDef def) {
		if (isLocal() || def.isLocal()) {
			return false;
		}
		return getPrerequisite().implies(def.getPrerequisite());
	}

	@Override
	public boolean impliesWhenBefore(ValueDef def) {
		if (isLocal() || def.isLocal()) {
			return false;
		}
		return getPrerequisite().implies(def.getPrerequisite());
	}

	@Override
	public final ValueDef toValue() {
		return this;
	}

	@Override
	public final CondDef toCondition() {
		if (this.condition != null) {
			return this.condition;
		}
		return this.condition = createCondDef();
	}

	public final Value<?> value(Resolver resolver) {
		assertCompatible(resolver.getScope());

		final Resolver rescoped = getScopeUpgrade().rescope(resolver);

		if (hasPrerequisite()) {

			final LogicalValue prerequisite =
					getPrerequisite().logicalValue(rescoped);

			if (!prerequisite.isTrue()) {
				if (prerequisite.isFalse()) {
					return getValueStruct().unknownValue();
				}
				return getValueStruct().runtimeValue();
			}
		}

		final LogicalValue precondition =
				getPrecondition().logicalValue(rescoped);

		if (!precondition.isTrue()) {
			if (precondition.isFalse()) {
				return getValueStruct().falseValue();
			}
			return getValueStruct().runtimeValue();
		}

		final Value<?> value = calculateValue(rescoped);

		if (value == null) {
			return getValueStruct().unknownValue();
		}

		return value.prefixWith(getScopeUpgrade().toPrefix());
	}

	@Override
	public final Definitions toDefinitions() {

		final CondDefs conditions = new CondDefs(
				DefKind.CONDITION,
				logicalTrue(this, getScope()).toCondDef());

		if (isClaim()) {
			return new Definitions(
					this,
					getScope(),
					getValueStruct(),
					NO_REQUIREMENTS,
					conditions,
					new ValueDefs(DefKind.CLAIM, this),
					NO_PROPOSITIONS);
		}

		return new Definitions(
				this,
				getScope(),
				getValueStruct(),
				NO_REQUIREMENTS,
				conditions,
				NO_CLAIMS,
				new ValueDefs(DefKind.PROPOSITION, this));
	}

	public ValOp write(ValDirs dirs, HostOp host) {
		if (hasPrerequisite()) {
			getPrerequisite().write(
					dirs.dirs().unknownWhenFalse(),
					host);
		}

		if (!getPrecondition().isTrue()) {
			getPrecondition().write(
					dirs.dirs().falseWhenUnknown(),
					host);
		}

		return writeDef(dirs, host);
	}

	@Override
	protected String name() {
		return "ValueDef";
	}

	protected abstract boolean hasConstantValue();

	protected abstract Value<?> calculateValue(Resolver resolver);

	protected CondDef createCondDef() {
		return new ValueCondDef(this);
	}

	@Override
	protected final void fullyResolve(Resolver resolver) {
		getPrerequisite().resolveAll(resolver);
		getPrecondition().resolveAll(resolver);
		fullyResolveDef(resolver);
	}

	protected abstract void fullyResolveDef(Resolver resolver);

	protected InlineValue inlineDef(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct) {
		// TODO In-line ValueDef.
		return null;
	}

	protected ValOp writeDef(ValDirs dirs, HostOp host) {
		return writeValue(dirs.falseWhenUnknown(), host);
	}

	protected abstract ValOp writeValue(ValDirs dirs, HostOp host);

	final InlineValue inline(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct) {

		final InlineCond prerequisite;

		if (!hasPrerequisite()) {
			prerequisite = null;
		} else {
			prerequisite = getPrerequisite().inline(normalizer, getScope());
			if (prerequisite == null) {
				return null;
			}
		}

		final InlineCond precondition =
				getPrecondition().inline(normalizer, getScope());

		if (precondition == null) {
			return null;
		}

		final InlineValue def = inlineDef(normalizer, valueStruct);

		if (def == null) {
			return null;
		}

		return new InlineValueDef(prerequisite, precondition, def);
	}

}

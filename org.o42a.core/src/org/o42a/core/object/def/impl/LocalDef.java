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
package org.o42a.core.object.def.impl;

import static org.o42a.core.ref.ScopeUpgrade.noScopeUpgrade;

import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.local.InlineCmd;
import org.o42a.core.ir.local.LocalIR;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.Def;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.st.Command;
import org.o42a.core.st.CommandTargets;
import org.o42a.core.st.sentence.ImperativeBlock;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;


public class LocalDef extends Def {

	public static Def localDef(
			ImperativeBlock block,
			Scope scope,
			Command command) {

		final Obj actualOwner = scope.toObject();
		final Obj explicitOwner = block.getScope().getOwner();

		assert actualOwner == explicitOwner :
			"LocalDef can only be constructed for explicit local scope";

		return new LocalDef(scope, block, command);
	}

	private final Scope ownerScope;
	private final ImperativeBlock block;
	private final Command command;
	private final PrefixPath localPrefix;

	private LocalDef(
			Scope ownerScope,
			ImperativeBlock block,
			Command command) {
		super(sourceOf(block), block, noScopeUpgrade(ownerScope));
		this.ownerScope = ownerScope;
		this.block = block;
		this.command = command;
		this.localPrefix = getOwnerScope().pathTo(block.getScope());
	}

	private LocalDef(LocalDef prototype, ScopeUpgrade scopeUpgrade) {
		super(prototype, scopeUpgrade);
		this.ownerScope = prototype.ownerScope;
		this.block = prototype.block;
		this.command = prototype.command;
		this.localPrefix = prototype.localPrefix;
	}

	public final ImperativeBlock getBlock() {
		return this.block;
	}

	@Override
	public ValueStruct<?, ?> getValueStruct() {

		final Scope scope =
				getLocalPrefix().rescope(getScopeUpgrade().rescope(getScope()));

		assert scope.toLocal() != null :
			"Not a local scope: " + scope;

		final ValueStruct<?, ?> valueStruct =
				getCommand().env().getExpectedValueStruct();

		if (valueStruct == null) {
			return null;
		}

		return valueStruct
				.prefixWith(getLocalPrefix())
				.prefixWith(getScopeUpgrade().toPrefix());
	}

	@Override
	public boolean unconditional() {

		final CommandTargets targets = getCommand().getCommandTargets();

		return targets.haveValue() && !targets.havePrerequisite();
	}

	@Override
	public void normalize(RootNormalizer normalizer) {
		if (!isExplicit()) {
			return;
		}

		final Scope localScope =
				getLocalPrefix().rescope(getScopeUpgrade().rescope(getScope()));
		final RootNormalizer imperativeNormalizer =
				new RootNormalizer(normalizer.getAnalyzer(), localScope);

		getCommand().normalize(imperativeNormalizer);
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append("LocalDef{");
		out.append(getLocation());
		if (isClaim()) {
			out.append("!}");
		} else {
			out.append(".}");
		}

		return out.toString();
	}

	@Override
	protected boolean hasConstantValue() {
		return getCommand().getCommandTargets().isConstant();
	}

	@Override
	protected Value<?> calculateValue(Resolver resolver) {

		final LocalScope local =
				getLocalPrefix().rescope(resolver.getScope()).toLocal();

		assert local != null :
			"Not a local scope: " + resolver;

		return getCommand()
				.initialValue(local.walkingResolver(resolver))
				.getValue()
				.prefixWith(getLocalPrefix());
	}

	@Override
	protected LocalDef create(
			ScopeUpgrade upgrade,
			ScopeUpgrade additionalUpgrade) {
		return new LocalDef(this, upgrade);
	}

	@Override
	protected void fullyResolve(Resolver resolver) {

		final LocalScope local =
				getLocalPrefix().rescope(resolver.getScope()).toLocal();

		getCommand().resolveAll(local.walkingResolver(resolver));
	}

	@Override
	protected InlineValue inline(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct) {

		final InlineCmd inline = getCommand().inline(
				normalizer,
				valueStruct,
				getLocalPrefix().rescope(getScope()));

		if (inline == null) {
			return null;
		}

		return new InlineLocalDef(valueStruct, inline);
	}

	@Override
	protected ValOp writeDefValue(ValDirs dirs, HostOp host) {
		// Imperative block`s value CAN be UNKNOWN.
		return writeValue(dirs, host);
	}

	@Override
	protected ValOp writeValue(ValDirs dirs, HostOp host) {
		assert assertFullyResolved();

		final ObjectOp ownerObject = host.materialize(dirs.dirs());
		final LocalScope scope = getBlock().getScope().toLocal();
		final Obj ownerType = scope.getOwner();
		final ObjOp ownerBody =
				ownerObject.cast(dirs.id("owner"), dirs.dirs(), ownerType);
		final LocalIR ir = scope.ir(host.getGenerator());

		return ir.writeValue(dirs, ownerBody, null, getCommand());
	}

	final Command getCommand() {
		return this.command;
	}

	final PrefixPath getLocalPrefix() {
		return this.localPrefix;
	}

	Scope getOwnerScope() {
		return this.ownerScope;
	}

}

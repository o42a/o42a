/*
    Compiler
    Copyright (C) 2013 Ruslan Lopatin

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
package org.o42a.compiler.ip.ref.keeper;

import org.o42a.core.Scope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.object.state.Keeper;
import org.o42a.core.ref.*;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.action.ReturnValue;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.link.TargetResolver;


final class KeptValueDefiner extends Definer {

	private final Keeper keeper;

	KeptValueDefiner(KeepValueStatement statement, CommandEnv env) {
		super(statement, env);
		this.keeper = getScope().toObject().keepers().keep(
				this,
				getValue());
	}

	public final Ref getValue() {
		return ((KeepValueStatement) getStatement()).getValue();
	}

	@Override
	public CommandTargets getTargets() {
		if (!getValue().isConstant()) {
			return returnCommand();
		}
		return returnCommand().setConstant();
	}

	@Override
	public Action action(Resolver resolver) {
		return new ReturnValue(this, getValue().value(resolver));
	}

	@Override
	public InlineEval inline(Normalizer normalizer, Scope origin) {
		return null;
	}

	@Override
	public InlineEval normalize(RootNormalizer normalizer, Scope origin) {
		getValue().upgradeScope(normalizer.getNormalizedScope())
		.normalize(normalizer.getAnalyzer());
		return null;
	}

	@Override
	public Eval eval(CodeBuilder builder, Scope origin) {
		return new KeptValueEval(this.keeper);
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		getValue().resolveAll(resolver);

		final Obj object = resolver.getScope().toObject();

		object.type().useBy(resolver.refUser());
	}

	@Override
	public TypeParameters<?> typeParameters(Scope scope) {
		return getValue().typeParameters(scope);
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {
		return null;
	}

	@Override
	public DefTarget toTarget(Scope origin) {

		final Ref value = getValue();

		if (!value.getValueType().isLink()) {
			return DefTarget.NO_DEF_TARGET;
		}

		return new DefTarget(value.dereference());
	}

	@Override
	public void resolveTargets(TargetResolver resolver, Scope origin) {
		if (!getValue().getValueType().isLink()) {
			return;
		}

		final Obj object = getValue().getResolution().toObject();

		object.value().getDefinitions().resolveTargets(resolver);
	}

}

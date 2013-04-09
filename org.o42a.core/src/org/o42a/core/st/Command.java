/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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
package org.o42a.core.st;

import static org.o42a.core.st.CommandTargets.*;

import org.o42a.core.*;
import org.o42a.core.ir.cmd.Cmd;
import org.o42a.core.ir.cmd.InlineCmd;
import org.o42a.core.object.def.Def;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.ref.*;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.Location;
import org.o42a.core.st.action.Action;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.link.TargetResolver;
import org.o42a.util.log.LogInfo;


public abstract class Command implements ContainerInfo {


	public static CommandTargets noCommands() {
		return NO_COMMANDS;
	}

	public static CommandTargets exitCommand(LogInfo loggable) {
		return new CommandTargets(loggable, EXIT_MASK);
	}

	private final Statement statement;
	private final CommandEnv env;

	public Command(Statement statement, CommandEnv env) {
		this.statement = statement;
		this.env = env;
	}

	public final Statement getStatement() {
		return this.statement;
	}

	public final CommandEnv env() {
		return this.env;
	}

	@Override
	public final Location getLocation() {
		return getStatement().getLocation();
	}

	public final CompilerContext getContext() {
		return getLocation().getContext();
	}

	public final CompilerLogger getLogger() {
		return getContext().getLogger();
	}

	@Override
	public final Scope getScope() {
		return getStatement().getScope();
	}

	@Override
	public final Container getContainer() {
		return getStatement().getContainer();
	}

	public abstract CommandTargets getTargets();

	/**
	 * Called to replace the statement with another one.
	 *
	 * <p>Supported only for inclusion statement.<p>
	 *
	 * @param statement replacement statement.
	 *
	 * @return replacement definer.
	 */
	public Command replaceWith(Statement statement) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Determines a type parameters of command's value in the given scope.
	 *
	 * <p>This value is used by {@link Def#getTypeParameters() value definition}
	 * and for compatibility checks.</p>
	 *
	 * @param scope the scope the type parameters to determine in.
	 *
	 * @return the type parameters or <code>null</code> if this command
	 * does not produce any values.
	 */
	public abstract TypeParameters<?> typeParameters(Scope scope);

	public abstract Action action(Resolver resolver);

	public abstract Instruction toInstruction(Resolver resolver);

	public abstract DefTarget toTarget(Scope origin);

	public final void resolveAll(FullResolver resolver) {
		getStatement().fullyResolved();
		getContext().fullResolution().start();
		try {
			fullyResolve(resolver);
		} finally {
			getContext().fullResolution().end();
		}
	}

	public abstract void resolveTargets(TargetResolver resolver, Scope origin);

	@Override
	public final Distributor distribute() {
		return Contained.distribute(this);
	}

	@Override
	public final Distributor distributeIn(Container container) {
		return Contained.distributeIn(this, container);
	}

	public abstract InlineCmd inline(Normalizer normalizer, Scope origin);

	public abstract InlineCmd normalize(
			RootNormalizer normalizer,
			Scope origin);

	public abstract Cmd cmd(Scope origin);

	@Override
	public final void assertScopeIs(Scope scope) {
		Scoped.assertScopeIs(this, scope);
	}

	@Override
	public final void assertCompatible(Scope scope) {
		Scoped.assertCompatible(this, scope);
	}

	@Override
	public final void assertSameScope(ScopeInfo other) {
		Scoped.assertSameScope(this, other);
	}

	@Override
	public final void assertCompatibleScope(ScopeInfo other) {
		Scoped.assertCompatibleScope(this, other);
	}

	@Override
	public String toString() {
		if (this.statement == null) {
			return super.toString();
		}
		return this.statement.toString();
	}

	protected abstract void fullyResolve(FullResolver resolver);

	protected final CommandTargets actionCommand() {
		return new CommandTargets(this, PRECONDITION_MASK | NON_CONSTANT_MASK);
	}

	protected final CommandTargets exitCommand() {
		return new CommandTargets(this, EXIT_MASK);
	}

	protected final CommandTargets repeatCommand() {
		return new CommandTargets(this, REPEAT_MASK);
	}

	protected final CommandTargets returnCommand() {
		return new CommandTargets(
				this,
				PRECONDITION_MASK | VALUE_MASK | NON_CONSTANT_MASK);
	}

	protected final CommandTargets fieldDef() {
		return new CommandTargets(this, FIELD_MASK);
	}

	protected final CommandTargets clauseDef() {
		return new CommandTargets(this, CLAUSE_MASK);
	}

}

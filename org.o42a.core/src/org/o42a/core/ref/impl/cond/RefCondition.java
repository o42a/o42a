/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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
package org.o42a.core.ref.impl.cond;

import org.o42a.core.ref.Ref;
import org.o42a.core.st.*;
import org.o42a.core.st.sentence.Local;


public final class RefCondition extends Statement {

	private final Ref ref;
	private final Local local;

	public RefCondition(Ref ref) {
		super(ref, ref.distribute());
		this.ref = ref;
		this.local = null;
	}

	public RefCondition(Local local) {
		super(local, local.distribute());
		this.ref = local.ref();
		this.local = local;
	}

	public final Ref getRef() {
		return this.ref;
	}

	public final boolean isLocal() {
		return this.local != null;
	}

	public final Local getLocal() {
		return this.local;
	}

	@Override
	public boolean isValid() {
		return getRef().isValid();
	}

	@Override
	public Command command(CommandEnv env) {
		return new RefConditionCommand(this, env);
	}

	@Override
	public Statement reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final Ref ref = this.ref.reproduce(reproducer);

		if (ref == null) {
			return null;
		}

		final Local local = getLocal();

		if (local == null) {
			return new RefCondition(ref);
		}

		reproducer.getStatements().local(this, local.getName(), ref);

		return null;
	}

	@Override
	public String toString() {
		if (this.ref == null) {
			return super.toString();
		}
		if (this.local != null) {
			return this.local.toString() + " = " + this.local.ref();
		}
		return this.ref.toString();
	}

}

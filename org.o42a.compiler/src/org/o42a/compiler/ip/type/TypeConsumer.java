/*
    Compiler
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.compiler.ip.type;

import static org.o42a.core.object.meta.Nesting.NO_NESTING;

import org.o42a.common.macro.type.TypeParameterKey;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRefParameters;


public abstract class TypeConsumer {

	public static final TypeConsumer NO_TYPE_CONSUMER =
			new NoTypeConsumer(NO_NESTING);
	/**
	 * This consumer is applied to standalone expressions.
	 *
	 * <p>A phrase can recognize and replace it with appropriate type consumer.
	 * </p>
	 */
	public static final TypeConsumer EXPRESSION_TYPE_CONSUMER =
			new NoTypeConsumer(NO_NESTING);

	public static TypeConsumer typeConsumer(Nesting nesting) {
		return new DefaultTypeConsumer(nesting);
	}

	private final Nesting nesting;

	public TypeConsumer(Nesting nesting) {
		this.nesting = nesting;
	}

	public final Nesting getNesting() {
		return this.nesting;
	}

	public abstract TypeConsumer paramConsumer(TypeParameterKey parameterKey);

	public abstract ParamTypeRef consumeType(
			Ref ref,
			TypeRefParameters typeParameters);

	public final TypeConsumer noConsumption() {
		return new NoTypeConsumer(getNesting());
	}

}

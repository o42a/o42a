/*
    Compiler Core
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
package org.o42a.core.ref;

import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Located;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Statement;
import org.o42a.core.st.sentence.Statements;
import org.o42a.util.log.LogInfo;


public abstract class RefPath extends Located {

	public RefPath(LocationInfo location) {
		super(location);
	}

	public RefPath(CompilerContext context, LogInfo logInfo) {
		super(context, logInfo);
	}

	protected abstract TypeRef ancestor(LocationInfo location, Ref ref);

	protected abstract TypeRef iface(Ref ref, boolean rebuilt);

	protected abstract Statement toCondition(
			Ref condition,
			Statements statements);

	protected abstract Ref toValue(
			LocationInfo location,
			Ref value,
			Statements statements);

	protected abstract FieldDefinition toFieldDefinition(
			Ref ref,
			boolean rebuilt);

	protected abstract Ref consume(Ref ref, Consumer consumer);

	protected abstract Ref toStateful(Ref ref);

}

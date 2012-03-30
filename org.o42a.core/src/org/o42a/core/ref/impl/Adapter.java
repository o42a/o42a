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
package org.o42a.core.ref.impl;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.member.AdapterId.adapterId;
import static org.o42a.core.ref.impl.CastToVoid.CAST_TO_VOID;

import org.o42a.core.Scope;
import org.o42a.core.member.Member;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectType;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathExpander;
import org.o42a.core.ref.path.PathFragment;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.ValueType;
import org.o42a.util.log.Loggable;


public final class Adapter extends PathFragment implements LocationInfo {

	private final CompilerContext context;
	private final Loggable loggable;
	private final StaticTypeRef adapterType;

	public Adapter(LocationInfo location, StaticTypeRef adapterType) {
		this.context = location.getContext();
		this.loggable = location.getLoggable();
		this.adapterType = adapterType;
	}

	@Override
	public Loggable getLoggable() {
		return this.loggable;
	}

	@Override
	public CompilerContext getContext() {
		return this.context;
	}

	public final StaticTypeRef getAdapterType() {
		return this.adapterType;
	}

	@Override
	public Path expand(PathExpander expander, int index, Scope start) {

		final Path path = path(start);

		if (path == null) {
			return null;
		}
		if (!castToVoid(start)) {
			return path;
		}

		return path.append(CAST_TO_VOID);
	}

	@Override
	public String toString() {
		return "@@" + this.adapterType;
	}

	private Path path(Scope start) {

		final ObjectType objectType = start.toObject().type();

		if (objectType.derivedFrom(this.adapterType.type(dummyUser()))) {
			return Path.SELF_PATH;
		}

		final Member adapterMember =
				objectType.getObject().member(adapterId(this.adapterType));

		if (adapterMember == null) {
			this.context.getLogger().incompatible(
					this.loggable,
					this.adapterType);
			return null;
		}

		return adapterMember.getKey().toPath();
	}

	private boolean castToVoid(Scope start) {

		final ValueType<?> adapterValueType =
				getAdapterType().typeObject(dummyUser())
				.value()
				.getValueType();

		if (!adapterValueType.isVoid()) {
			return false;
		}

		final Obj object = start.toObject();
		final ValueType<?> valueType = object.value().getValueType();

		return valueType.isVoid();
	}

}

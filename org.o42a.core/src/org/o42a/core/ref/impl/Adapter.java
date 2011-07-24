/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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

import static org.o42a.core.member.AdapterId.adapterId;
import static org.o42a.core.value.Value.voidValue;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.artifact.object.ObjectType;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.Member;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.common.Wrap;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.ValueType;


public final class Adapter extends Wrap {

	private final Ref ref;
	private final StaticTypeRef adapterType;

	public Adapter(LocationInfo location, Ref ref, StaticTypeRef adapterType) {
		super(location, ref.distribute());
		this.ref = ref;
		this.adapterType = adapterType;
	}

	@Override
	protected Ref resolveWrapped() {

		final Resolution resolution = this.ref.getResolution();

		if (resolution.isError()) {
			return errorRef(resolution);
		}

		final ObjectType objectType = resolution.materialize().type();

		if (objectType.derivedFrom(this.adapterType.type(dummyUser()))) {
			return this.ref;
		}

		final Member adapterMember =
			objectType.getObject().member(adapterId(this.adapterType));

		if (adapterMember == null) {
			getLogger().incompatible(this.ref, this.adapterType);
			return errorRef(this);
		}

		final Path adapterPath = adapterMember.getKey().toPath();

		return adapterPath.target(this, distribute(), this.ref);
	}

	@Override
	protected RefOp createOp(HostOp host) {

		final RefOp op = super.createOp(host);
		final ValueType<?> adapterValueType =
			this.adapterType.typeObject(dummyUser()).value().getValueType();

		if (adapterValueType != ValueType.VOID) {
			return op;
		}

		final ValueType<?> valueType =
			this.ref.getResolution().materialize().value().getValueType();

		if (valueType == ValueType.VOID) {
			return op;
		}

		return new CastToVoidOp(host, this, op);
	}

	private static final class CastToVoidOp extends RefOp {

		private final RefOp op;

		CastToVoidOp(HostOp host, Ref ref, RefOp op) {
			super(host, ref);
			this.op = op;
		}

		@Override
		public void writeLogicalValue(CodeDirs dirs) {
			this.op.writeLogicalValue(dirs);
		}

		@Override
		public ValOp writeValue(ValDirs dirs) {
			assert dirs.getValueType().assertIs(ValueType.VOID);

			writeLogicalValue(dirs.dirs());

			return voidValue().op(dirs.getBuilder(), dirs.code());
		}

		@Override
		public HostOp target(CodeDirs dirs) {
			return this.op.target(dirs);
		}

	}

}

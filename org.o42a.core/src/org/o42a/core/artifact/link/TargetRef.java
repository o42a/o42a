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
package org.o42a.core.artifact.link;

import static org.o42a.core.ref.path.PrefixPath.emptyPrefix;

import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.common.RescopableRef;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.st.Reproducer;
import org.o42a.util.log.Loggable;


public final class TargetRef extends RescopableRef<TargetRef> {

	public static TargetRef targetRef(Ref ref, TypeRef typeRef) {
		if (typeRef != null) {
			return new TargetRef(
					ref,
					typeRef,
					emptyPrefix(ref.getScope()));
		}
		return new TargetRef(
				ref,
				ref.ancestor(ref),
				emptyPrefix(ref.getScope()));
	}

	public static TargetRef targetRef(
			Ref ref,
			TypeRef typeRef,
			PrefixPath prefix) {
		if (typeRef != null) {
			return new TargetRef(ref, typeRef, prefix);
		}
		return new TargetRef(
				ref,
				ref.ancestor(ref).prefixWith(prefix),
				prefix);
	}

	private final Ref ref;
	private final TypeRef typeRef;

	private TargetRef(Ref ref, TypeRef typeRef, PrefixPath prefix) {
		super(prefix);
		this.ref = ref;
		this.typeRef = typeRef;
		typeRef.assertSameScope(this);
	}

	@Override
	public final CompilerContext getContext() {
		return this.ref.getContext();
	}

	@Override
	public final Loggable getLoggable() {
		return this.ref.getLoggable();
	}

	@Override
	public final Ref getRef() {
		return this.ref;
	}

	public TypeRef getTypeRef() {
		return this.typeRef;
	}

	public final TypeRef toTypeRef() {
		return getRef().toTypeRef().prefixWith(getPrefix());
	}

	public final TargetRef toStatic() {
		return new TargetRef(
				this.ref.toStatic(),
				this.typeRef.toStatic(),
				getPrefix());
	}

	public RefOp ref(CodeDirs dirs, ObjOp host) {

		final HostOp rescopedHost = getPrefix().write(dirs, host);

		return getRef().op(rescopedHost);
	}

	public HostOp target(CodeDirs dirs, ObjOp host) {
		return ref(dirs, host).target(dirs);
	}

	@Override
	public String toString() {
		if (this.typeRef == null) {
			return super.toString();
		}
		return "(" + this.typeRef + ") " + this.ref;
	}

	@Override
	protected TargetRef create(PrefixPath prefix, PrefixPath additionalPrefix) {
		return new TargetRef(
				getRef(),
				getTypeRef().prefixWith(additionalPrefix),
				prefix);
	}

	@Override
	protected TargetRef createReproduction(
			Reproducer reproducer,
			Reproducer rescopedReproducer,
			Ref ref,
			PrefixPath prefix) {

		final TypeRef typeRef = getTypeRef().reproduce(reproducer);

		if (typeRef == null) {
			return null;
		}

		return new TargetRef(ref, typeRef, prefix);
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		this.typeRef.resolveAll(resolver);
		this.ref.resolve(resolver).resolveTarget();
	}

}

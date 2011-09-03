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
package org.o42a.core.ref.path;

import static org.o42a.core.value.Value.unknownValue;

import org.o42a.codegen.code.Code;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.ObjectTypeOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


final class PathTargetAncestor extends Ref {

	private final Ref ref;
	private boolean error;

	PathTargetAncestor(LocationInfo location, Ref ref) {
		super(location, ref.distribute());
		this.ref = ref;
	}

	@Override
	public boolean isConstant() {
		return this.ref.isConstant();
	}

	@Override
	public boolean isStatic() {
		if (!this.ref.isStatic()) {
			return false;
		}

		final TypeRef ancestor =
				this.ref.getResolution().materialize().type().getAncestor();

		return ancestor == null || ancestor.isStatic();
	}

	@Override
	public Resolution resolve(Resolver resolver) {

		final Artifact<?> artifact = resolveArtifact(resolver);
		final TypeRef ancestor = resolveAncestor(artifact);

		if (ancestor == null) {
			return resolver.noResolution(this);
		}

		return resolver.artifactPart(
				this,
				artifact,
				ancestor.artifact(resolver));
	}

	@Override
	public Value<?> value(Resolver resolver) {

		final TypeRef ancestor = resolveAncestor(resolver);

		if (ancestor == null) {
			return unknownValue();
		}

		return ancestor.value(ancestor.getScope().walkingResolver(resolver));
	}

	@Override
	public TypeRef ancestor(LocationInfo location) {
		return resolveAncestor(getScope().dummyResolver()).getAncestor();
	}

	@Override
	public Ref reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final Ref ref = this.ref.reproduce(reproducer);

		if (ref == null) {
			return null;
		}

		return new PathTargetAncestor(reproducer.getScope(), ref);
	}

	@Override
	public String toString() {
		if (this.ref == null) {
			return super.toString();
		}
		return this.ref + "^^";
	}

	@Override
	protected FieldDefinition createFieldDefinition() {
		return defaultFieldDefinition();
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		resolve(resolver).resolveAll();
	}

	@Override
	protected void fullyResolveValues(Resolver resolver) {
		resolve(resolver).resolveValues(resolver);
	}

	@Override
	protected RefOp createOp(HostOp host) {
		return new AncestorOp(host, this);
	}

	private final TypeRef resolveAncestor(Resolver resolver) {
		return resolveAncestor(resolveArtifact(resolver));
	}

	private Artifact<?> resolveArtifact(Resolver resolver) {
		if (this.error) {
			return null;
		}

		final Resolution resolution = this.ref.resolve(resolver);

		if (resolution.isError()) {
			this.error = true;
			return null;
		}

		final Artifact<?> artifact = resolution.toArtifact();

		if (artifact == null) {
			this.error = true;
			getLogger().notArtifact(resolution);
			return null;
		}

		return artifact;
	}

	private TypeRef resolveAncestor(Artifact<?> artifact) {
		if (artifact == null) {
			return null;
		}

		final Obj object = artifact.toObject();

		if (object != null) {

			final ValueType<?> valueType = object.value().getValueType();

			if (valueType.wrapper(getContext().getIntrinsics()) == object) {
				return valueType.typeRef(
						this,
						object.getScope().getEnclosingScope());
			}

			return object.type().getAncestor();
		}

		final TypeRef typeRef = artifact.getTypeRef();

		if (typeRef != null) {
			return typeRef;
		}

		this.error = true;
		getLogger().error(
				"no_ancestor",
				this,
				"Artifact %s has no ancestor",
				artifact.getKind());

		return null;
	}

	private static final class AncestorOp extends RefOp {

		AncestorOp(HostOp scope, PathTargetAncestor ref) {
			super(scope, ref);
		}

		@Override
		public HostOp target(CodeDirs dirs) {

			final Code code = dirs.code();
			final PathTargetAncestor ref = (PathTargetAncestor) getRef();
			final ObjectOp object =
					ref.ref.op(host()).target(dirs).materialize(dirs);
			final ObjectTypeOp ancestorData =
					object.objectType(code).ptr()
					.data(code)
					.ancestorType(code)
					.load(null, code)
					.op(getBuilder(), object.getPrecision());

			return ancestorData.object(code, ref.getResolution().materialize());
		}

	}

}

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
package org.o42a.core.artifact.array;

import static org.o42a.core.artifact.array.ArrayInitializer.invalidArrayInitializer;
import static org.o42a.core.artifact.array.ArrayTypeRef.arrayTypeObject;
import static org.o42a.core.artifact.array.ArrayTypeRef.arrayTypeRef;
import static org.o42a.core.ref.Ref.voidRef;

import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.*;
import org.o42a.core.artifact.link.Link;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.IRGenerator;
import org.o42a.core.ir.field.FieldIR;
import org.o42a.core.member.field.DeclaredField;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldDecl;


public abstract class Array extends Artifact<Array> {

	public static FieldDecl<Array> fieldDecl(
			DeclaredField<Array> field) {
		return new ArrayFieldDecl(field);
	}

	public static FieldIR<Array> fieldIR(
			IRGenerator generator,
			Field<Array> field) {
		return new ArrayFieldIR(generator, field);
	}

	private MaterializedArray materialized;
	private ArrayTypeRef typeRef;
	private ArrayInitializer initializer;

	public Array(Scope scope) {
		super(scope);
	}

	protected Array(Scope scope, Array sample) {
		super(scope, sample);
	}

	@Override
	public boolean isValid() {
		return getArrayTypeRef().isValid() && getInitializer().isValid();
	}

	@Override
	public ArtifactKind<Array> getKind() {
		return null;
	}

	public final Obj getItemType() {
		return getArrayTypeRef().getItemTypeRef().getType();
	}

	@Override
	public Directive toDirective() {
		return null;
	}

	@Override
	public final Obj toObject() {
		return null;
	}

	@Override
	public final Array toArray() {
		return this;
	}

	@Override
	public final Link toLink() {
		return null;
	}

	@Override
	public final Obj materialize() {
		if (this.materialized == null) {
			this.materialized = new MaterializedArray(this);
		}
		return this.materialized;
	}

	public final ArrayTypeRef getArrayTypeRef() {
		if (this.typeRef == null) {
			define();
		}
		return this.typeRef;
	}

	public final ArrayInitializer getInitializer() {
		if (this.initializer == null) {
			define();
		}
		return this.initializer;
	}

	@Override
	public void resolveAll() {
		getArrayTypeRef();
		getInitializer();
	}

	protected abstract ArrayTypeRef buildTypeRef();

	protected abstract TypeRef buildItemTypeRef();

	protected abstract ArrayInitializer buildInitializer();

	private void define() {

		final ArrayTypeRef typeRef = buildTypeRef();
		final ArrayInitializer initializer = buildInitializer();
		final ArrayTypeRef initializerTypeRef;
		final TypeRef itemTypeRef;

		if (typeRef != null) {
			itemTypeRef = typeRef.getItemTypeRef();
			initializerTypeRef = initializer.arrayTypeRef(typeRef);
		} else {
			itemTypeRef = buildItemTypeRef();
			initializerTypeRef = initializer.arrayTypeRef(itemTypeRef);
		}

		if (initializerTypeRef != null) {
			this.initializer = initializer;
			if (typeRef != null) {
				this.typeRef = typeRef;
			} else if (itemTypeRef != null) {
				this.typeRef = arrayTypeRef(
						itemTypeRef,
						initializerTypeRef.getDimension());
			} else {
				this.typeRef = initializerTypeRef;
			}
			return;
		}

		// handle error
		final Distributor distributor =
			distributeIn(getScope().getEnclosingContainer());

		this.initializer = invalidArrayInitializer(initializer, distributor);
		if (typeRef != null) {
			this.typeRef = typeRef;
		} else if (itemTypeRef != null) {
			this.typeRef = arrayTypeRef(itemTypeRef, 0);
		} else {
			this.typeRef = arrayTypeObject(
					voidRef(this, distributor),
					0);
		}
	}

}

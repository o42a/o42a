/*
    Compiler
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.compiler.ip.ref.array;

import static org.o42a.core.ref.Ref.voidRef;

import org.o42a.ast.expression.BracketsNode;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.core.Distributor;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.ValueFieldDefinition;
import org.o42a.core.ref.path.ObjectConstructor;
import org.o42a.core.ref.path.PathReproducer;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeParameters;
import org.o42a.core.ref.type.TypeParametersBuilder;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.ValueAdapter;
import org.o42a.core.value.ValueRequest;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.array.ArrayValueStruct;
import org.o42a.core.value.array.ArrayValueType;


public class ArrayConstructor extends ObjectConstructor {

	private final Interpreter ip;
	private final BracketsNode node;
	private final ArrayConstructor reproducedFrom;
	private final Reproducer reproducer;
	private ArrayValueStruct arrayStruct;
	private TypeParametersBuilder typeParameters;

	public ArrayConstructor(
			Interpreter ip,
			CompilerContext context,
			BracketsNode node,
			Distributor distributor) {
		super(new Location(context, node), distributor);
		this.ip = ip;
		this.node = node;
		this.reproducer = null;
		this.reproducedFrom = null;
	}

	private ArrayConstructor(
			ArrayConstructor reproducedFrom,
			Reproducer reproducer) {
		super(reproducedFrom, reproducer.distribute());
		this.ip = reproducedFrom.ip;
		this.node = reproducedFrom.node;
		this.reproducer = reproducer;
		this.reproducedFrom = reproducedFrom;
	}

	public final BracketsNode getNode() {
		return this.node;
	}

	public final Interpreter ip() {
		return this.ip;
	}

	@Override
	public TypeRef ancestor(LocationInfo location) {
		return ArrayValueType.ROW.typeRef(
				location,
				getScope(),
				typeParameters());
	}

	@Override
	public ValueAdapter valueAdapter(Ref ref, ValueRequest request) {
		if (request.isTransformAllowed()) {

			final ArrayValueStruct arrayStruct =
					request.getExpectedStruct().toArrayStruct();

			if (arrayStruct != null) {
				return new ArrayInitValueAdapter(ref, this, arrayStruct);
			}
		}

		return super.valueAdapter(ref, request);
	}

	@Override
	public FieldDefinition fieldDefinition(Ref ref) {
		return new ValueFieldDefinition(ref);
	}

	@Override
	public ArrayConstructor reproduce(PathReproducer reproducer) {
		return new ArrayConstructor(this, reproducer.getReproducer());
	}

	@Override
	public String toString() {
		if (this.node == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder();

		this.node.printContent(out);

		return out.toString();
	}

	@Override
	protected Obj createObject() {
		if (this.reproducedFrom == null) {
			return new ArrayObject(this, typeByItems());
		}

		final ArrayObject object = (ArrayObject) getConstructed();

		return new ArrayObject(this, this.reproducer, object);
	}

	boolean typeByItems() {
		typeParameters();
		return this.arrayStruct == null;
	}

	private TypeParametersBuilder typeParameters() {
		if (this.typeParameters != null) {
			return this.typeParameters;
		}
		if (this.reproducedFrom != null) {
			if (this.reproducedFrom.arrayStruct == null) {
				return this.typeParameters =
						this.reproducedFrom.typeParameters;
			}
			this.arrayStruct =
					this.reproducedFrom.arrayStruct.reproduce(
							this.reproducer);
			if (this.arrayStruct != null) {
				return this.typeParameters = this.arrayStruct;
			}
			return this.typeParameters =
					this.reproducedFrom.typeParameters;
		}

		if (this.node.getArguments().length == 0) {
			return this.typeParameters =
					this.arrayStruct =
					ArrayValueType.ROW.arrayStruct(
							voidRef(this, distribute()).toTypeRef());
		}

		return this.typeParameters = new ArrayTypeParamsByItems(toRef());
	}

	private static final class ArrayTypeParamsByItems
			implements TypeParametersBuilder {

		private final Ref arrayRef;

		public ArrayTypeParamsByItems(Ref arrayRef) {
			this.arrayRef = arrayRef;
		}

		@Override
		public TypeParametersBuilder prefixWith(PrefixPath prefix) {

			final Ref arrayRef = this.arrayRef.prefixWith(prefix);

			if (this.arrayRef == arrayRef) {
				return this;
			}

			return new ArrayTypeParamsByItems(arrayRef);
		}

		@Override
		public ValueStruct<?, ?> valueStructBy(
				ValueStruct<?, ?> defaultStruct) {
			return this.arrayRef.valueStruct(this.arrayRef.getScope());
		}

		@Override
		public TypeParameters typeParametersBy(
				TypeParameters defaultParameters) {
			return this.arrayRef.typeParameters(this.arrayRef.getScope());
		}

		@Override
		public TypeParametersBuilder reproduce(Reproducer reproducer) {

			final Ref arrayRef = this.arrayRef.reproduce(reproducer);

			if (arrayRef == null) {
				return null;
			}

			return new ArrayTypeParamsByItems(arrayRef);
		}

	}

}

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
import static org.o42a.core.value.ValueStructFinder.DEFAULT_VALUE_STRUCT_FINDER;

import org.o42a.ast.expression.BracketsNode;
import org.o42a.ast.type.DefinitionKind;
import org.o42a.ast.type.InterfaceNode;
import org.o42a.ast.type.TypeNode;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.core.Distributor;
import org.o42a.core.object.Obj;
import org.o42a.core.object.array.ArrayValueStruct;
import org.o42a.core.object.array.ArrayValueType;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.ObjectConstructor;
import org.o42a.core.ref.path.PathReproducer;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.ValueAdapter;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueStructFinder;


public class ArrayConstructor extends ObjectConstructor {

	private final Interpreter ip;
	private final BracketsNode node;
	private final ArrayConstructor reproducedFrom;
	private final Reproducer reproducer;
	private ArrayValueType arrayType;
	private ArrayValueStruct arrayStruct;
	private ValueStructFinder valueStructFinder;

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

	public final InterfaceNode getInterfaceNode() {
		return this.node.getInterface();
	}

	public final ArrayValueType getValueType() {
		if (this.arrayType != null) {
			return this.arrayType;
		}

		final InterfaceNode interfaceNode = getInterfaceNode();

		if (interfaceNode == null) {
			return this.arrayType = ArrayValueType.ROW;
		}
		if (interfaceNode.getKind().getType() == DefinitionKind.LINK) {
			return this.arrayType = ArrayValueType.ROW;
		}

		return this.arrayType = ArrayValueType.ARRAY;
	}

	public final Interpreter ip() {
		return this.ip;
	}

	@Override
	public TypeRef ancestor(LocationInfo location) {
		return getValueType().typeRef(
				location,
				getScope(),
				valueStructFinder());
	}

	@Override
	public ValueAdapter valueAdapter(
			Ref ref,
			ValueStruct<?, ?> expectedStruct,
			boolean adapt) {
		if (adapt
				&& expectedStruct != null
				&& getNode().getArguments().length == 0) {

			final ArrayValueStruct arrayStruct = expectedStruct.toArrayStruct();

			if (arrayStruct != null) {
				assert this.arrayStruct != null :
					"Array structure already determined";
				this.valueStructFinder = this.arrayStruct =
						arrayStruct.setConstant(true);
			}
		}

		return super.valueAdapter(ref, expectedStruct, adapt);
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

	private boolean typeByItems() {
		valueStructFinder();
		return this.arrayStruct == null;
	}

	private ValueStructFinder valueStructFinder() {
		if (this.valueStructFinder != null) {
			return this.valueStructFinder;
		}
		if (this.reproducedFrom != null) {
			if (this.reproducedFrom.arrayStruct == null) {
				return this.valueStructFinder =
						this.reproducedFrom.valueStructFinder;
			}
			this.arrayStruct =
					this.reproducedFrom.arrayStruct.reproduce(
							this.reproducer);
			if (this.arrayStruct != null) {
				return this.valueStructFinder = this.arrayStruct;
			}
			return this.valueStructFinder =
					this.reproducedFrom.valueStructFinder;
		}

		final InterfaceNode iface = getInterfaceNode();

		if (iface == null) {
			if (this.node.getArguments().length == 0) {
				return this.valueStructFinder =
						this.arrayStruct =
						getValueType().arrayStruct(
								voidRef(this, distribute()).toTypeRef());
			}
			return this.valueStructFinder = new ArrayStructByItems(toRef());
		}

		final TypeNode type = iface.getType();

		if (type == null) {
			if (this.node.getArguments().length == 0) {
				return this.arrayStruct = getValueType().arrayStruct(
						voidRef(this, distribute()).toTypeRef());
			}
			return this.valueStructFinder = new ArrayStructByItems(toRef());
		}

		final TypeRef itemTypeRef =
				type.accept(this.ip.typeVisitor(), distribute());

		if (itemTypeRef == null) {
			return this.valueStructFinder = DEFAULT_VALUE_STRUCT_FINDER;
		}

		return this.valueStructFinder = this.arrayStruct =
				getValueType().arrayStruct(itemTypeRef);
	}

	private static final class ArrayStructByItems implements ValueStructFinder {

		private final Ref arrayRef;

		public ArrayStructByItems(Ref arrayRef) {
			this.arrayRef = arrayRef;
		}

		@Override
		public ValueStruct<?, ?> valueStructBy(
				Ref ref,
				ValueStruct<?, ?> defaultStruct) {
			return this.arrayRef.valueStruct(this.arrayRef.getScope());
		}

		@Override
		public ValueStruct<?, ?> toValueStruct() {
			return null;
		}

	}

}

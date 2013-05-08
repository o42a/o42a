/*
    Compiler
    Copyright (C) 2011-2013 Ruslan Lopatin

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
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.compiler.ip.access.AccessRules;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.ValueFieldDefinition;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRefParameters;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.ValueAdapter;
import org.o42a.core.value.ValueRequest;
import org.o42a.core.value.array.Array;
import org.o42a.core.value.array.ArrayValueType;


public class ArrayConstructor extends ObjectConstructor {

	private final Interpreter ip;
	private final BracketsNode node;
	private final AccessRules accessRules;
	private final ArrayConstructor reproducedFrom;
	private final Reproducer reproducer;
	private TypeParameters<Array> arrayParameters;
	private TypeRefParameters typeParameters;

	public ArrayConstructor(
			Interpreter ip,
			CompilerContext context,
			BracketsNode node,
			AccessDistributor distributor) {
		super(new Location(context, node), distributor);
		this.ip = ip;
		this.node = node;
		this.accessRules = distributor.getAccessRules();
		this.reproducer = null;
		this.reproducedFrom = null;
	}

	private ArrayConstructor(
			ArrayConstructor reproducedFrom,
			Reproducer reproducer) {
		super(reproducedFrom, reproducer.distribute());
		this.ip = reproducedFrom.ip;
		this.node = reproducedFrom.node;
		this.accessRules = reproducedFrom.accessRules;
		this.reproducer = reproducer;
		this.reproducedFrom = reproducedFrom;
	}

	public final Interpreter ip() {
		return this.ip;
	}

	public final BracketsNode getNode() {
		return this.node;
	}

	public final AccessRules getAccessRules() {
		return this.accessRules;
	}

	public TypeRef ancestor(LocationInfo location) {
		return ArrayValueType.ROW.typeRef(
				location,
				getScope(),
				typeParameters());
	}

	@Override
	public boolean mayContainDeps() {
		return this.node.getArguments().length != 0;
	}

	@Override
	public TypeRef ancestor(LocationInfo location, Ref ref) {
		return ancestor(location);
	}

	@Override
	public TypeRef iface(Ref ref) {
		return ancestor(ref);
	}

	@Override
	public ValueAdapter valueAdapter(Ref ref, ValueRequest request) {

		final TypeParameters<Array> arrayParameters =
				request.getExpectedParameters().toArrayParameters();

		if (arrayParameters != null) {
			return new ArrayInitValueAdapter(ref, this, arrayParameters);
		}

		return super.valueAdapter(ref, request);
	}

	@Override
	public FieldDefinition fieldDefinition(Ref ref) {
		return new ValueFieldDefinition(ref, rescopedTypeParameters(ref));
	}

	@Override
	public ArrayConstructor reproduce(PathReproducer reproducer) {
		return new ArrayConstructor(this, reproducer.getReproducer());
	}

	public final AccessDistributor distributeAccess() {
		return this.accessRules.distribute(distribute());
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
		return this.arrayParameters == null;
	}

	private TypeRefParameters typeParameters() {
		if (this.typeParameters != null) {
			return this.typeParameters;
		}
		if (this.reproducedFrom != null) {
			if (this.reproducedFrom.arrayParameters == null) {
				return this.typeParameters =
						this.reproducedFrom.typeParameters;
			}
			this.arrayParameters =
					this.reproducedFrom.arrayParameters.reproduce(
							this.reproducer);
			if (this.arrayParameters != null) {
				return this.typeParameters = this.arrayParameters;
			}
			return this.typeParameters =
					this.reproducedFrom.typeParameters;
		}

		if (this.node.getArguments().length == 0) {
			return this.typeParameters = this.arrayParameters =
					ArrayValueType.ROW.typeParameters(
							voidRef(this, distribute()).toTypeRef());
		}

		return this.typeParameters = new ArrayTypeRefParamsByItems(toRef());
	}

	private TypeRefParameters rescopedTypeParameters(Ref ref) {

		final TypeRefParameters typeParameters = typeParameters();
		final BoundPath path = ref.getPath();

		if (path.length() == 1) {
			return typeParameters;
		}

		final PrefixPath prefix = path.cut(1).toPrefix(path.cut(1).getOrigin());

		return typeParameters.prefixWith(prefix);
	}

}

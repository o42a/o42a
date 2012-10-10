/*
    Intrinsics
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.intrinsic.array;

import static org.o42a.core.member.MemberName.fieldName;
import static org.o42a.core.ref.RefUsage.VALUE_REF_USAGE;
import static org.o42a.util.string.Capitalization.CASE_INSENSITIVE;

import org.o42a.common.builtin.AnnotatedBuiltin;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.core.Scope;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.member.*;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectValue;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.array.Array;
import org.o42a.core.value.array.ArrayItem;
import org.o42a.core.value.array.ArrayValueStruct;
import org.o42a.core.value.link.LinkValueStruct;
import org.o42a.core.value.link.LinkValueType;
import org.o42a.core.value.link.TargetRef;


abstract class IndexedItem extends AnnotatedBuiltin {

	private static final MemberName INDEX_NAME =
			fieldName(CASE_INSENSITIVE.canonicalName("index"));

	private Ref array;
	private Ref index;

	IndexedItem(MemberOwner owner, AnnotatedSources sources) {
		super(owner, sources);
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {

		final Ref arrayRef = array();
		final Obj arrayObject = arrayRef.resolve(resolver).toObject();
		final ObjectValue arrayObjectValue = arrayObject.value();
		final ArrayValueStruct arrayStruct =
				arrayObjectValue.getValueStruct().toArrayStruct();
		final LinkValueStruct resultStruct =
				LinkValueType.LINK.linkStruct(arrayStruct.getItemTypeRef());
		final Value<Array> arrayValue =
				arrayStruct.cast(arrayObjectValue.getValue());

		if (!arrayValue.getKnowledge().isKnown()) {
			return resultStruct.runtimeValue();
		}
		if (arrayValue.getKnowledge().isFalse()) {
			return resultStruct.falseValue();
		}

		final ObjectValue indexObjectValue =
				index().resolve(resolver).toObject().value();
		final Value<Long> indexValue =
				ValueType.INTEGER.cast(indexObjectValue.getValue());

		if (!indexValue.getKnowledge().isKnown()) {
			return resultStruct.runtimeValue();
		}
		if (indexValue.getKnowledge().isFalse()) {
			return resultStruct.falseValue();
		}

		final Array array = arrayValue.getCompilerValue();
		final long index = indexValue.getCompilerValue();

		if (index < 0) {
			return resultStruct.falseValue();
		}
		if (index >= array.length()) {
			return resultStruct.falseValue();
		}

		final ArrayItem item =
				array.items(arrayObject.getScope())[(int) index];
		final PrefixPath arrayPrefix =
				arrayRef.getPath().toPrefix(resolver.getScope());
		final TargetRef itemTarget =
				item.getValueRef()
				.toTargetRef(item.getTypeRef())
				.prefixWith(arrayPrefix);

		return resultStruct.compilerValue(new IndexedItemLink(
				resolver,
				distributeIn(resolver.getContainer()),
				itemTarget));
	}

	@Override
	public void resolveBuiltin(FullResolver resolver) {

		final FullResolver valueResolver =
				resolver.setRefUsage(VALUE_REF_USAGE);

		array().resolveAll(valueResolver);
		index().resolveAll(valueResolver);
	}

	@Override
	public InlineEval inlineBuiltin(Normalizer normalizer, Scope origin) {

		final InlineValue inlineArray = array().inline(normalizer, origin);
		final InlineValue inlineIndex = index().inline(normalizer, origin);

		if (inlineArray == null && inlineIndex == null) {
			return null;
		}

		return new IndexedItemEval(this, inlineArray, inlineIndex);
	}

	@Override
	public Eval evalBuiltin() {
		return new IndexedItemEval(this, null, null);
	}

	final Ref array() {
		if (this.array != null) {
			return this.array;
		}
		return this.array =
				getScope()
				.getEnclosingScopePath()
				.bind(this, getScope())
				.target(distribute());
	}

	final Ref index() {
		if (this.index != null) {
			return this.index;
		}
		return this.index = indexPath(getScope()).target(distribute());
	}

	static final BoundPath indexPath(Scope scope) {

		final MemberKey indexKey =
				INDEX_NAME.key(scope.toField().getFirstDeclaration());
		final Member indexField = scope.getContainer().member(indexKey);

		return indexKey.toPath().dereference().bind(indexField, scope);
	}

}

/*
    Root Object Definition
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
package org.o42a.root.array;

import static org.o42a.core.member.MemberIdKind.FIELD_NAME;
import static org.o42a.core.object.meta.EscapeMode.ESCAPE_POSSIBLE;
import static org.o42a.core.ref.RefUsage.VALUE_REF_USAGE;
import static org.o42a.util.string.Capitalization.CASE_INSENSITIVE;

import org.o42a.common.builtin.AnnotatedBuiltin;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.core.Scope;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.MemberName;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectValue;
import org.o42a.core.object.meta.EscapeMode;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.array.Array;
import org.o42a.core.value.array.ArrayItem;
import org.o42a.core.value.link.KnownLink;
import org.o42a.core.value.link.LinkValueType;
import org.o42a.core.value.link.TargetRef;


abstract class IndexedItem extends AnnotatedBuiltin {

	private static final MemberName INDEX_NAME =
			FIELD_NAME.memberName(CASE_INSENSITIVE.canonicalName("index"));

	private Ref array;
	private Ref index;

	IndexedItem(Obj owner, AnnotatedSources sources) {
		super(owner, sources);
	}

	@Override
	public EscapeMode escapeMode(Scope scope) {
		return ESCAPE_POSSIBLE;
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {

		final Ref arrayRef = array();
		final Obj arrayObject = arrayRef.resolve(resolver).toObject();
		final ObjectValue arrayObjectValue = arrayObject.value();
		final TypeParameters<Array> arrayParams =
				arrayObject.type().getParameters().toArrayParameters();
		final TypeParameters<KnownLink> resultParams =
				LinkValueType.LINK.typeParameters(
						arrayParams.getValueType()
						.toArrayType()
						.itemTypeRef(arrayParams));
		final Value<Array> arrayValue =
				arrayParams.cast(arrayObjectValue.getValue());

		if (!arrayValue.getKnowledge().isKnown()) {
			return resultParams.runtimeValue();
		}
		if (arrayValue.getKnowledge().isFalse()) {
			return resultParams.falseValue();
		}

		final ObjectValue indexObjectValue =
				index().resolve(resolver).toObject().value();
		final Value<Long> indexValue =
				ValueType.INTEGER.cast(indexObjectValue.getValue());

		if (!indexValue.getKnowledge().isKnown()) {
			return resultParams.runtimeValue();
		}
		if (indexValue.getKnowledge().isFalse()) {
			return resultParams.falseValue();
		}

		final Array array = arrayValue.getCompilerValue();
		final long index = indexValue.getCompilerValue();

		if (index < 0) {
			return resultParams.falseValue();
		}
		if (index >= array.length()) {
			return resultParams.falseValue();
		}

		final ArrayItem item =
				array.items(arrayObject.getScope())[(int) index];
		final PrefixPath arrayPrefix =
				arrayRef.getPath().toPrefix(resolver.getScope());
		final TargetRef itemTarget =
				item.getValueRef()
				.toTargetRef(item.getInterfaceRef())
				.prefixWith(arrayPrefix);

		return resultParams.compilerValue(new IndexedItemLink(
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

		if (inlineArray == null || inlineIndex == null) {
			return null;
		}

		return new IndexedItemEval(
				this,
				inlineArray,
				inlineIndex,
				!type().getValueType().isVariable());
	}

	@Override
	public Eval evalBuiltin() {
		return new IndexedItemEval(
				this,
				null,
				null,
				!type().getValueType().isVariable());
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

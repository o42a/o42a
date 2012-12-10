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
package org.o42a.compiler.ip.ref.operator;

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;
import static org.o42a.core.member.MemberName.fieldName;
import static org.o42a.util.string.Capitalization.CASE_SENSITIVE;

import org.o42a.ast.expression.BinaryNode;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.phrase.part.BinaryPhrasePart;
import org.o42a.compiler.ip.type.TypeConsumer;
import org.o42a.core.Distributor;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.ValueFieldDefinition;
import org.o42a.core.ref.path.ObjectConstructor;
import org.o42a.core.ref.path.PathReproducer;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.ValueType;


public final class ComparisonExpression extends ObjectConstructor {

	static final MemberId COMPARISON_MEMBER =
			fieldName(CASE_SENSITIVE.canonicalName("_cmp"));

	private final Interpreter ip;
	private final BinaryNode node;
	private final TypeConsumer typeConsumer;
	private final ComparisonExpression prototype;
	private final Reproducer reproducer;
	private ComparisonOperator operator;
	private Ref phrase;
	private byte error;


	public ComparisonExpression(
			Interpreter ip,
			BinaryNode node,
			Distributor distributor,
			TypeConsumer typeConsumer) {
		super(location(distributor, node), distributor);
		this.ip = ip;
		this.node = node;
		this.typeConsumer = typeConsumer;
		this.prototype = null;
		this.reproducer = null;
	}

	private ComparisonExpression(
			ComparisonExpression prototype,
			Reproducer reproducer) {
		super(prototype, reproducer.distribute());
		this.ip = prototype.ip;
		this.node = prototype.node;
		this.typeConsumer = null;
		this.prototype = prototype;
		this.reproducer = reproducer;
		this.operator = prototype.getOperator();
		this.error = prototype.error;
	}

	public final Interpreter ip() {
		return this.ip;
	}

	@Override
	public TypeRef ancestor(LocationInfo location) {
		return ValueType.VOID.typeRef(location, getScope());
	}

	public final boolean hasError() {
		if (this.error != 0) {
			return this.error > 0;
		}
		if (this.operator.checkError(this.phrase)) {
			this.error = 1;
			return true;
		}
		this.error = -1;
		return false;
	}

	@Override
	public FieldDefinition fieldDefinition(Ref ref) {
		return new ValueFieldDefinition(ref, null);
	}

	@Override
	public ComparisonExpression reproduce(PathReproducer reproducer) {
		return new ComparisonExpression(this, reproducer.getReproducer());
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		this.node.printContent(out);

		return out.toString();
	}

	@Override
	protected Obj createObject() {
		return new ComparisonResult(this);
	}

	protected final ComparisonOperator getOperator() {
		assert this.operator != null :
			"Phrase didn't built yet";
		return this.operator;
	}

	protected final Ref getPhrase() {
		assert this.phrase != null :
			"Phrase didn't built yet";
		return this.phrase;
	}

	final Ref phrase(Distributor distributor) {
		if (this.phrase != null) {
			return this.phrase;
		}
		if (this.prototype == null) {

			final BinaryPhrasePart binary =
					ip().phraseIp().binaryPhrase(
							this.node,
							distributor,
							this.typeConsumer);

			this.operator = binary.getComparisonOperator();

			if (this.operator == null) {
				this.error = 1;
			}

			return this.phrase = binary.getPhrase().toRef();
		}
		// Build prototype`s phrase.
		this.prototype.getConstructed().resolveMembers(false);
		// Reproduce prototype`s phrase.
		return this.phrase = this.prototype.getPhrase().reproduce(
				this.reproducer.distributeBy(distributor));
	}

	ValOp write(ValDirs dirs, HostOp host, RefOp cmp, InlineValue inlineCmp) {

		final ComparisonOperator operator = getOperator();
		final ValDirs cmpDirs = dirs.dirs().nested().value(
				"cmp",
				operator.getValueType(),
				TEMP_VAL_HOLDER);
		final ValOp cmpVal;

		if (inlineCmp != null) {
			cmpVal = operator.inlineComparison(cmpDirs, host, inlineCmp);
		} else {
			cmpVal = operator.writeComparison(cmpDirs, cmp);
		}

		final ValDirs resultDirs = cmpDirs.dirs().nested().value(dirs);
		final ValOp result = operator.write(resultDirs, cmpVal);

		resultDirs.done();
		cmpDirs.done();

		return result;
	}

}

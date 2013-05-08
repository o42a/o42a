/*
    Compiler Commons
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
package org.o42a.common.ref.cmp;

import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;
import static org.o42a.core.member.MemberName.fieldName;
import static org.o42a.util.string.Capitalization.CASE_SENSITIVE;

import org.o42a.common.phrase.Phrase;
import org.o42a.common.phrase.part.BinaryPhraseOperator;
import org.o42a.common.phrase.part.BinaryPhrasePart;
import org.o42a.core.Distributor;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.ValueFieldDefinition;
import org.o42a.core.ref.path.ObjectConstructor;
import org.o42a.core.ref.path.PathReproducer;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.ValueType;


public class ComparisonExpression extends ObjectConstructor {

	static final MemberId COMPARISON_MEMBER =
			fieldName(CASE_SENSITIVE.canonicalName("_cmp"));

	private final ComparisonExpression prototype;
	private final Reproducer reproducer;
	private final BinaryPhraseOperator operator;
	private final Ref leftOperand;
	private final Ref rightOperand;
	private CompilerLogger resolutionLogger;
	private ComparisonOperator comparisonOperator;
	private Ref phrase;
	private byte error;

	public ComparisonExpression(
			LocationInfo location,
			BinaryPhraseOperator operator,
			Ref leftOperand,
			Ref rightOperand) {
		super(location, leftOperand.distribute());
		this.operator = operator;
		this.leftOperand = leftOperand;
		this.rightOperand = rightOperand;
		this.prototype = null;
		this.reproducer = null;
	}

	private ComparisonExpression(
			ComparisonExpression prototype,
			Reproducer reproducer) {
		super(prototype, reproducer.distribute());
		this.operator = prototype.operator;
		this.leftOperand = prototype.leftOperand;
		this.rightOperand = prototype.rightOperand;
		this.prototype = prototype;
		this.reproducer = reproducer;
		this.comparisonOperator = prototype.getComparisonOperator();
		this.error = prototype.error;
	}

	public final BinaryPhraseOperator getOperator() {
		return this.operator;
	}

	public final Ref getLeftOperand() {
		return this.leftOperand;
	}

	public final Ref getRightOperand() {
		return this.rightOperand;
	}

	@Override
	public TypeRef ancestor(LocationInfo location, Ref ref) {
		return ValueType.VOID.typeRef(location, getScope());
	}

	@Override
	public TypeRef iface(Ref ref) {
		return ancestor(ref, ref);
	}

	public final CompilerLogger getResolutionLogger() {
		return this.resolutionLogger != null
				? this.resolutionLogger : getLogger();
	}

	public final boolean hasResolutionLogger() {
		return this.resolutionLogger != null;
	}

	public final ComparisonExpression setResolutionLogger(
			CompilerLogger resolutionLogger) {
		this.resolutionLogger = resolutionLogger;
		return this;
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
		if (this.rightOperand == null) {
			return super.toString();
		}
		return this.leftOperand
				+ this.operator.getSign()
				+ this.rightOperand.toString();
	}

	@Override
	protected Obj createObject() {
		return new ComparisonResult(this);
	}

	protected final ComparisonOperator getComparisonOperator() {
		assert this.comparisonOperator != null :
			"Phrase didn't built yet";
		return this.comparisonOperator;
	}

	protected final Ref getPhrase() {
		assert this.phrase != null :
			"Phrase didn't built yet";
		return this.phrase;
	}

	final boolean hasErrors() {
		if (this.error != 0) {
			return this.error > 0;
		}
		if (checkForErrors()) {
			this.error = 1;
			return true;
		}
		this.error = -1;
		return false;
	}

	final Ref phrase(Distributor distributor) {
		if (this.phrase != null) {
			return this.phrase;
		}
		if (this.prototype == null) {

			final Phrase phrase = new Phrase(this, distributor);

			if (hasResolutionLogger()) {
				phrase.setResolutionLogger(getResolutionLogger());
			}
			phrase.setAncestor(
					this.leftOperand.rescope(distributor.getScope())
					.toTypeRef());

			final BinaryPhrasePart binary =
					phrase.binary(this, this.operator, this.rightOperand);

			this.comparisonOperator = binary.getComparisonOperator();

			if (this.comparisonOperator == null) {
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

		final ComparisonOperator operator = getComparisonOperator();
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

	private boolean checkForErrors() {
		return this.comparisonOperator.checkForErrors(
				getPhrase(),
				getResolutionLogger());
	}

}

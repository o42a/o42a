/*
    Compiler
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
package org.o42a.compiler.ip.ref;

import static org.o42a.core.def.Def.voidDef;
import static org.o42a.core.def.Definitions.definitions;
import static org.o42a.core.ir.op.ValOp.VAL_TYPE;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.object.ObjectTypeOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.ValOp;


enum ValuePart {

	ALL("") {

		@Override
		Definitions valuePart(ValuePartRef ex, Definitions definitions) {
			return definitions;
		}

		@Override
		void writeLogicalValue(Code code, CodePos exit, ValuePartOp op) {

			final ObjectOp object = op.object(code, exit);

			if (!op.isOverridden()) {
				object.writeLogicalValue(code, exit);
				return;
			}

			final ValOp result = code.allocate(VAL_TYPE).storeUnknown(code);

			object.objectType(code).writeOverriddenValue(code, result);
			result.loadCondition(code).go(code, null, exit);
		}

		@Override
		void writeValue(Code code, CodePos exit, ValOp result, ValuePartOp op) {

			final ObjectOp object = op.object(code, exit);

			if (!op.isOverridden()) {
				object.writeValue(code, exit, result);
			} else {
				object.objectType(code).writeOverriddenValue(code, exit, result);
			}
		}

	},

	VALUE("Value", "value") {

		@Override
		Definitions valuePart(ValuePartRef ex, Definitions definitions) {
			return definitions.removeCondition(ex);
		}

		@Override
		void writeLogicalValue(Code code, CodePos exit, ValuePartOp op) {

			final ValOp result = code.allocate(VAL_TYPE).storeUnknown(code);

			writeValue(code, exit, result, op);
			result.loadCondition(code).go(code, null, exit);
		}

		@Override
		void writeValue(Code code, CodePos exit, ValOp result, ValuePartOp op) {

			final ObjectOp object = op.object(code, exit);

			if (!op.isOverridden()) {
				object.writeValue(code, result);
			} else {
				object.objectType(code).writeOverriddenValue(code, result);
			}
		}

	},

	REQUIREMENT("Requirement", "requirement") {

		@Override
		Definitions valuePart(ValuePartRef ex, Definitions definitions) {
			return voidDef(
					ex,
					ex.distribute(),
					definitions.getRequirement().fullLogical())
					.toDefinitions();
		}

		@Override
		void writeLogicalValue(Code code, CodePos exit, ValuePartOp op) {

			final ObjectOp object = op.object(code, exit);

			if (!op.isOverridden()) {
				object.writeRequirement(code, exit);
			} else {

				final ObjectTypeOp data = object.objectType(code);

				data.writeOverriddenRequirement(code, exit);
			}
		}

		@Override
		void writeValue(Code code, CodePos exit, ValOp result, ValuePartOp op) {
			writeLogicalValue(code, exit, op);
			result.storeVoid(code);
		}

	},

	CONDITION("Condition", "condition") {

		@Override
		Definitions valuePart(ValuePartRef ex, Definitions definitions) {
			return voidDef(
					ex,
					ex.distribute(),
					definitions.getCondition().fullLogical())
					.toDefinitions();
		}

		@Override
		void writeLogicalValue(Code code, CodePos exit, ValuePartOp op) {

			final ObjectOp object = op.object(code, exit);

			if (!op.isOverridden()) {
				object.writeCondition(code, exit);
			} else {

				final ObjectTypeOp data = object.objectType(code);

				data.writeOverriddenCondition(code, exit);
			}
		}

		@Override
		void writeValue(Code code, CodePos exit, ValOp result, ValuePartOp op) {
			writeLogicalValue(code, exit, op);
			result.storeVoid(code);
		}

	},

	CLAIM("Claim", "claim") {

		@Override
		Definitions valuePart(ValuePartRef ex, Definitions definitions) {
			return definitions(
					ex,
					ex.getScope(),
					definitions.getClaims());
		}

		@Override
		void writeLogicalValue(Code code, CodePos exit, ValuePartOp op) {

			final ValOp result = code.allocate(VAL_TYPE).storeUnknown(code);

			writeValue(code, exit, result, op);
			result.loadCondition(code).go(code, null, exit);
		}

		@Override
		void writeValue(Code code, CodePos exit, ValOp result, ValuePartOp op) {

			final ObjectOp object = op.object(code, exit);

			if (!op.isOverridden()) {
				object.writeClaim(code, exit, result);
			} else {
				object.objectType(code).writeOverriddenClaim(code, exit, result);
			}
		}

	},

	PROPOSITION("Proposition", "proposition") {

		@Override
		Definitions valuePart(ValuePartRef ex, Definitions definitions) {
			return definitions(
					ex,
					ex.getScope(),
					definitions.getPropositions());
		}

		@Override
		void writeLogicalValue(Code code, CodePos exit, ValuePartOp op) {

			final ValOp result = code.allocate(VAL_TYPE).storeUnknown(code);

			writeValue(code, exit, result, op);
			result.loadCondition(code).goUnless(code, exit);
		}

		@Override
		void writeValue(Code code, CodePos exit, ValOp result, ValuePartOp op) {

			final ObjectOp object = op.object(code, exit);

			if (!op.isOverridden()) {
				object.writeProposition(code, exit, result);
			} else {

				final ObjectTypeOp data = object.objectType(code);

				data.writeOverriddenPsoposition(code, exit, result);
			}
		}

	};

	final String partName;

	ValuePart(String partName) {
		this.partName = partName;
	}

	ValuePart(String partName, String partId) {
		this.partName = partName;
		ValuePartRef.partsById.put(partId, this);
	}

	abstract Definitions valuePart(ValuePartRef ex, Definitions definitions);

	abstract void writeLogicalValue(Code code, CodePos exit, ValuePartOp op);

	abstract void writeValue(
			Code code,
			CodePos exit,
			ValOp result,
			ValuePartOp op);

}

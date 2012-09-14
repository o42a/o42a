package org.o42a.core.value.voids;

import static org.o42a.core.ir.field.FldKind.VOID_KEEPER;
import static org.o42a.core.ir.value.Val.VAL_CONDITION;
import static org.o42a.core.ir.value.Val.VAL_INDEFINITE;
import static org.o42a.core.value.voids.VoidKeeperIRType.VOID_KEEPER_IR_TYPE;

import org.o42a.codegen.data.Content;
import org.o42a.core.Scope;
import org.o42a.core.ir.field.FldKind;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.ir.object.ObjectIRBodyData;
import org.o42a.core.ir.object.state.KeeperIR;
import org.o42a.core.object.state.Keeper;
import org.o42a.core.value.Value;


final class VoidKeeperIR
		extends KeeperIR<VoidKeeperIROp, VoidKeeperIRType>
		implements Content<VoidKeeperIRType> {

	VoidKeeperIR(ObjectIRBody bodyIR, Keeper keeper) {
		super(bodyIR, keeper);
	}

	@Override
	public FldKind getKind() {
		return VOID_KEEPER;
	}

	@Override
	public void allocated(VoidKeeperIRType instance) {
	}

	@Override
	public void fill(VoidKeeperIRType instance) {

		final Scope scope = getBodyIR().getObjectIR().getObject().getScope();
		final Value<?> value = getKeeper().getValue().value(scope.resolver());

		if (!value.getKnowledge().isKnownToCompiler()) {
			instance.flags().setValue((byte) VAL_INDEFINITE);
		} else if (value.getKnowledge().isFalse()) {
			instance.flags().setValue((byte) 0);
		} else {
			instance.flags().setValue((byte) VAL_CONDITION);
		}
	}

	@Override
	protected VoidKeeperIRType allocateKeeper(ObjectIRBodyData data) {
		return data.getData().addInstance(
				getKeeper().toID(),
				VOID_KEEPER_IR_TYPE,
				this);
	}

}

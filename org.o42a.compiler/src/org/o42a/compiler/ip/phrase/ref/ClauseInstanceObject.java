package org.o42a.compiler.ip.phrase.ref;

import org.o42a.core.Scope;
import org.o42a.core.object.Obj;
import org.o42a.core.object.common.DefinedObject;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.st.sentence.DeclarativeBlock;


final class ClauseInstanceObject extends DefinedObject {

	private final ClauseInstanceConstructor constructor;

	ClauseInstanceObject(ClauseInstanceConstructor constructor) {
		super(
				constructor.instance().getLocation(),
				constructor.distribute());
		this.constructor = constructor;
	}

	@Override
	public String toString() {
		if (this.constructor == null) {
			return super.toString();
		}
		return this.constructor.toString();
	}

	@Override
	protected Ascendants buildAscendants() {
		return this.constructor.getAscendants().updateAscendants(
				new Ascendants(this));
	}

	@Override
	protected void buildDefinition(DeclarativeBlock definition) {
		this.constructor.instance().getDefinition().buildBlock(definition);
	}

	@Override
	protected Obj findObjectIn(Scope enclosing) {
		return this.constructor.resolve(enclosing);
	}

}
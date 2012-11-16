package org.o42a.core.value.impl;

import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.TypeParametersBuilder;


public class RefinedTypeParameters implements TypeParametersBuilder {

	private final TypeParametersBuilder refined;
	private final TypeParametersBuilder refinement;

	public RefinedTypeParameters(
			TypeParametersBuilder refinded,
			TypeParametersBuilder refinement) {
		this.refined = refinded;
		this.refinement = refinement;
	}

	@Override
	public TypeParameters<?> refine(TypeParameters<?> defaultParameters) {
		return this.refinement.refine(this.refined.refine(defaultParameters));
	}

	@Override
	public TypeParametersBuilder prefixWith(PrefixPath prefix) {
		return new RefinedTypeParameters(
				this.refined.prefixWith(prefix),
				this.refinement.prefixWith(prefix));
	}

	@Override
	public TypeParametersBuilder reproduce(Reproducer reproducer) {

		final TypeParametersBuilder refined =
				this.refined.reproduce(reproducer);

		if (refined == null) {
			return null;
		}

		final TypeParametersBuilder refinement =
				this.refinement.reproduce(reproducer);

		if (refinement == null) {
			return null;
		}

		return new RefinedTypeParameters(refined, refinement);
	}

	@Override
	public String toString() {
		if (this.refinement == null) {
			return super.toString();
		}
		return this.refined.toString() + this.refinement.toString();
	}

}

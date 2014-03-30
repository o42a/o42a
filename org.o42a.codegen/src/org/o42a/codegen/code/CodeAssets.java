/*
    Compiler Code Generator
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.codegen.code;

import static java.util.Objects.requireNonNull;

import org.o42a.util.ArrayUtil;


/**
 * Various code assets.
 *
 * <p>Arbitrary assets present in code at the given execution point.</p>
 *
 * <p>Asset can be added to code with {@link Code#addAsset(Class, CodeAsset)}
 * method, and removed with {@link Code#removeAsset(Class)} one. Assets
 * available at the current execution point could be retrieved with
 * {@link Code#assets()} method.</p>
 *
 * <p>Code transitions at the block beginning might make more assets available
 * at particular execution point within that block. But retrieving of these
 * assets will be possible only after such transition is made.</p>
 *
 * <p>Assets are only tracked within the same {@link Allocator allocation}.</p>
 */
public final class CodeAssets {

	private static final CodeAssets[] NO_AVAILABLE = new CodeAssets[0];

	private final Class<?> assetType;
	private final CodeAsset<?> asset;
	private CodeAssets[] available;

	CodeAssets() {
		this.assetType = null;
		this.asset = null;
		this.available = NO_AVAILABLE;
	}

	private CodeAssets(CodeAssets... available) {
		this.assetType = null;
		this.asset = null;
		this.available = available;
	}

	private CodeAssets(
			CodeAssets available,
			Class<?> assetType,
			CodeAsset<?> asset) {
		this.assetType = assetType;
		this.asset = asset;
		this.available = new CodeAssets[] {available};
	}

	public final <A extends CodeAsset<A>> A get(Class<? extends A> assetType) {
		requireNonNull(assetType, "Asset type not specified");
		return asset(this, assetType);
	}

	final <A extends CodeAsset<A>> CodeAssets update(
			Class<? extends A> assetType,
			A asset) {
		return new CodeAssets(this, assetType, asset);
	}

	final void addAvailable(CodeAssets assets) {
		this.available = ArrayUtil.append(this.available, assets);
	}

	final CodeAssets unite(CodeAssets assets) {
		return new CodeAssets(this, assets);
	}

	private <A extends CodeAsset<A>> A get(
			CodeAssets initial,
			Class<? extends A> assetType) {
		if (this == initial) {
			return null;
		}
		return asset(initial, assetType);
	}

	@SuppressWarnings("unchecked")
	private <A extends CodeAsset<A>> A asset(
			CodeAssets initial,
			Class<? extends A> assetType) {
		if (assetType == this.assetType) {
			return (A) this.asset;
		}

		A result = null;

		for (CodeAssets available : this.available) {

			final A asset = available.get(initial, assetType);

			if (asset == null) {
				continue;
			}
			if (result == null) {
				result = asset;
			} else {
				result = result.add(asset);
			}
		}

		return result;
	}

}

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

import java.util.HashSet;

import org.o42a.util.ArrayUtil;


/**
 * Various code assets.
 *
 * <p>Arbitrary assets present in code at the given execution point.</p>
 *
 * <p>Asset can be added to code with {@link Code#addAsset(Class, CodeAsset)}
 * method. Assets available at the current execution point could be retrieved
 * with {@link Code#assets()} method.</p>
 *
 * <p>Code transitions at the block beginning might make more assets available
 * at particular execution point within that block. But retrieving of these
 * assets will be possible only after such transition is made.</p>
 */
public final class CodeAssets implements CodeAssetsSource {

	private static final CodeAssetsSource[] NO_SOURCE =
			new CodeAssetsSource[0];

	private final Code code;
	private final String comment;
	private final Class<?> assetType;
	private final CodeAsset<?> asset;
	private CodeAssetsSource[] sources;

	CodeAssets(Code code, String comment) {
		this.code = code;
		this.comment = comment;
		this.assetType = null;
		this.asset = null;
		this.sources = NO_SOURCE;
	}

	CodeAssets(Code code, String comment, CodeAssetsSource... sources) {
		this.code = code;
		this.comment = comment;
		this.assetType = null;
		this.asset = null;
		this.sources = sources;
	}

	private CodeAssets(
			CodeAssets derived,
			String comment,
			Class<?> assetType,
			CodeAsset<?> asset) {
		this.code = derived.code;
		this.comment = comment;
		this.assetType = assetType;
		this.asset = asset;
		this.sources = new CodeAssetsSource[] {derived};
	}

	@Override
	public final CodeAssets assets() {
		return this;
	}

	public final <A extends CodeAsset<A>> A get(Class<? extends A> assetType) {
		assert assetType != null:
			"Asset type not specified";
		return asset(new HashSet<CodeAssets>(), assetType);
	}

	@Override
	public String toString() {
		if (this.code == null) {
			return super.toString();
		}
		return "CodeAssets[" + this.comment + " @" + this.code + ']';
	}

	final <A extends CodeAsset<A>> CodeAssets update(
			Class<? extends A> assetType,
			A asset) {
		return new CodeAssets(
				this,
				assetType + ": " + asset,
				assetType,
				asset);
	}

	final void addSource(CodeAssetsSource source) {
		this.sources = ArrayUtil.append(this.sources, source);
	}

	private <A extends CodeAsset<A>> A get(
			HashSet<CodeAssets> checked,
			Class<? extends A> assetType) {
		if (checked.contains(this)) {
			return null;
		}
		return asset(checked, assetType);
	}

	private <A extends CodeAsset<A>> A asset(
			HashSet<CodeAssets> checked,
			Class<? extends A> assetType) {
		checked.add(this);

		A result = null;

		for (CodeAssetsSource source : this.sources) {

			final A asset = source.assets().get(checked, assetType);

			if (asset == null) {
				continue;
			}
			if (result == null) {
				result = asset;
			} else {
				result = result.combineWith(asset);
			}
		}

		if (assetType == this.assetType) {

			@SuppressWarnings("unchecked")
			final A asset = (A) this.asset;

			if (result == null) {
				result = asset;
			} else {
				result = result.overwriteBy(asset);
			}
		}

		return result;
	}

}

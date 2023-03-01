/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import io.leo40git.sltbg.util.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

public final class FacePool {
	public static final long DEFAULT_ORDER_BASE = 1000;

	public static long getNextOrder(long order) {
		order += DEFAULT_ORDER_BASE;
		long rem = order % DEFAULT_ORDER_BASE;
		if (rem > 0) {
			order += DEFAULT_ORDER_BASE - rem;
		}
		return order;
	}

	private @NotNull String name;
	private String @NotNull [] description, credits;
	final @NotNull LinkedHashMap<String, FaceCategory> categories;
	private @Nullable FaceCategory lastCategory;
	private boolean needsSort;

	public FacePool(@NotNull String name) {
		this.name = name;
		categories = new LinkedHashMap<>();
		description = ArrayUtils.EMPTY_STRING_ARRAY;
		credits = ArrayUtils.EMPTY_STRING_ARRAY;

		lastCategory = null;
		needsSort = false;
	}

	public @NotNull String getName() {
		return name;
	}

	public void setName(@NotNull String name) {
		this.name = name;
	}

	public String @NotNull [] getDescription() {
		return ArrayUtils.clone(description);
	}

	public void setDescription(String @NotNull [] description) {
		this.description = ArrayUtils.clone(description);
	}

	public String @NotNull [] getCredits() {
		return ArrayUtils.clone(credits);
	}

	public void setCredits(String @NotNull [] credits) {
		this.credits = ArrayUtils.clone(credits);
	}

	private static final ThreadLocal<ArrayList<FaceCategory>> TL_SORT_BUF = new ThreadLocal<>();

	void markDirty() {
		needsSort = true;
	}

	public void sortIfNeeded() {
		if (needsSort) {
			var sortBuf = TL_SORT_BUF.get();
			if (sortBuf == null) {
				TL_SORT_BUF.set(sortBuf = new ArrayList<>(categories.size()));
			} else {
				sortBuf.ensureCapacity(categories.size());
			}

			try {
				sortBuf.addAll(categories.values());
				sortBuf.sort(Comparator.naturalOrder());

				categories.clear();
				for (var category : sortBuf) {
					categories.put(category.getName(), category);
					category.sortIfNeeded();
				}
			} finally {
				sortBuf.clear();
			}

			needsSort = false;
		}
	}

	public @NotNull @UnmodifiableView Map<String, FaceCategory> getCategories() {
		sortIfNeeded();
		return Collections.unmodifiableMap(categories);
	}

	public @Nullable FaceCategory getCategory(@NotNull String name) {
		return categories.get(name);
	}

	public @Nullable Face getFace(@NotNull String path) {
		int delIdx = path.indexOf(Face.PATH_DELIMITER);
		if (delIdx < 0) {
			throw new IllegalArgumentException("Path \"%s\" is missing delimiter '%s'".formatted(path, Face.PATH_DELIMITER));
		}

		var category = getCategory(path.substring(0, delIdx));
		if (category == null) {
			return null;
		}

		return category.getFace(path.substring(delIdx + 1));
	}

	public void add(@NotNull FaceCategory category) {
		if (category.getPool() != null) {
			throw new IllegalArgumentException("Category is already part of other pool");
		}

		if (categories.containsKey(category.getName())) {
			throw new IllegalArgumentException("Category with name \"" + category.getName() + "\" already exists in this pool");
		}

		categories.put(category.getName(), category);
		category.setPool(this);

		if (!category.isOrderSet() && lastCategory != null) {
			category.setOrder(getNextOrder(lastCategory.getOrder()));
		}
		lastCategory = category;

		markDirty();
	}

	void rename(@NotNull FaceCategory category, @NotNull String newName) {
		if (categories.containsKey(newName)) {
			throw new IllegalArgumentException("Category with name \"" + newName + "\" already exists in this pool");
		}

		categories.remove(category.getName(), category);
		categories.put(newName, category);
	}

	public boolean contains(@NotNull String category) {
		return categories.containsKey(category);
	}

	public @Nullable FaceCategory remove(@NotNull String category) {
		var catObj = categories.remove(category);
		if (catObj == null) {
			return null;
		}

		catObj.setPool(null);
		if (lastCategory == catObj) {
			lastCategory = null;
			for (var anCat : categories.values()) {
				lastCategory = anCat;
			}
		}
		markDirty();
		return catObj;
	}

	public void clear() {
		for (var category : categories.values()) {
			category.setPool(null);
		}

		categories.clear();
		lastCategory = null;
		needsSort = false;
	}
}

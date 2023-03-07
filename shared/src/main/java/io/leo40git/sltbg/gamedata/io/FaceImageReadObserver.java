/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata.io;

import io.leo40git.sltbg.gamedata.Face;
import io.leo40git.sltbg.gamedata.FaceCategory;
import io.leo40git.sltbg.gamedata.NamedFacePool;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface FaceImageReadObserver {
    default void preReadFacePoolImages(@NotNull NamedFacePool pool) {}
    default void preReadFaceCategoryImages(@NotNull FaceCategory category) {}
    default void preReadFaceImage(@NotNull Face face) {}
    default void postReadFaceImage(@NotNull Face face, @Nullable FaceIOException exc) {}
    default void postReadFaceCategoryImages(@NotNull FaceCategory category, @Nullable FaceCategoryIOException exc) {}
    default void postReadFacePoolImages(@NotNull NamedFacePool pool, @Nullable FacePoolIOException exc) {}
}

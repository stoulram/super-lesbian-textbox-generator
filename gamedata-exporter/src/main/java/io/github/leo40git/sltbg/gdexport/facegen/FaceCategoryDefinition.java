/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.gdexport.facegen;

import java.util.ArrayList;
import java.util.List;

import io.leo40git.sltbg.util.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public final class FaceCategoryDefinition {
    public final int lineNumber;
    public final @NotNull String name;
    public final long order;
    public final @Nullable String characterName;
    public final @NotNull @Unmodifiable List<String> description;
    public @Nullable ArrayList<FaceDefinition> faces;

    public FaceCategoryDefinition(int lineNumber,
                                  @NotNull String name, long order, @Nullable String characterName,
                                  @Nullable List<String> description) {
        this.lineNumber = lineNumber;
        this.name = name;
        this.order = order;
        this.characterName = characterName;
        this.description = CollectionUtils.copyOrEmpty(description);
    }

    public @Nullable List<FaceDefinition> getFaces() {
        return faces;
    }

    public void addFace(@NotNull FaceDefinition face) {
        if (faces == null) {
            faces = new ArrayList<>();
        }
        faces.add(face);
    }
}

package org.vander.konsist

import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test
import org.vander.konsist.debt.ArchitectureDebt

/**
 * A contract's place follows from who uses it: one used by another module belongs in a
 * contract module (core/domain), one used only inside spotify-lib is `internal`.
 */
class ContractPlacementTest {
    @Test
    fun `spotify-lib exposes no contract to other modules`() {
        spotifyLibInterfaces
            .filterNot { it.fullyQualifiedName in ArchitectureDebt.contractsToMoveToCoreDomain }
            .assertFalse(strict = true) { it.isUsedOutsideSpotifyLib() }
    }

    @Test
    fun `spotify-lib contracts used only inside it are internal`() {
        spotifyLibInterfaces
            .filterNot { it.isUsedOutsideSpotifyLib() }
            .filterNot { it.fullyQualifiedName in ArchitectureDebt.contractsToMakeInternal }
            .assertTrue(strict = false) { it.hasInternalModifier }
    }
}

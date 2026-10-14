package org.vander.konsist

import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test
import org.vander.konsist.debt.ArchitectureDebt

/**
 * A contract's place follows from who uses it: one used by another module belongs in a
 * contract module (core/domain), one used only inside spotify-lib is `internal`, and a
 * technical one stays next to its implementation.
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

    @Test
    fun `core domain holds only ports`() {
        val contracts = coreDomainInterfaces
        // Checked before the debt filter, which may leave nothing to check.
        check(contracts.isNotEmpty()) { "No interface found in core/domain: the rule checks nothing." }

        contracts
            .filterNot { it.fullyQualifiedName in ArchitectureDebt.coreDomainContractsUsedOnlyBySpotifyLib }
            .assertTrue(strict = false) { it.hasConsumerOutsideSpotifyLib() }
    }

    @Test
    fun `data source contracts are internal and sit next to their implementations`() {
        spotifyLibInterfaces
            .filter { it.name.endsWith("DataSource") }
            .assertTrue(strict = true) {
                it.hasInternalModifier && it.resideInPackage("org.vander.spotifyclient.data.remote.datasource")
            }
    }
}

package org.vander.konsist.debt

import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import org.junit.Test
import org.vander.konsist.FRAMEWORK_IMPORTS
import org.vander.konsist.SPOTIFY_LIB_INTERNALS
import org.vander.konsist.SPOTIFY_LIB_OUTER_LAYERS
import org.vander.konsist.appViewModels
import org.vander.konsist.importsAndroidLog
import org.vander.konsist.importsAny
import org.vander.konsist.productionScope
import org.vander.konsist.receivesOnlyContractTypes

class ArchitectureDebtTest {
    @Test
    fun `every debt entry still violates its rule`() {
        assertStillViolates(ArchitectureDebt.spotifyLibDomainImpure) { it.importsAny(FRAMEWORK_IMPORTS) }
        assertStillViolates(ArchitectureDebt.spotifyLibDomainWrongDirection) { it.importsAny(SPOTIFY_LIB_OUTER_LAYERS) }
        assertStillViolates(ArchitectureDebt.appDependsOnSpotifyLibInternals) { it.importsAny(SPOTIFY_LIB_INTERNALS) }
        assertStillViolates(ArchitectureDebt.androidUtilLog) { it.importsAndroidLog() }
    }

    @Test
    fun `every indebted ViewModel still receives a non-contract type`() {
        ArchitectureDebt.viewModelsDependingOnSpotifyLib.forEach { name ->
            val viewModel =
                checkNotNull(appViewModels.singleOrNull { it.name == name }) {
                    "$name: not found, renamed or deleted? Update ArchitectureDebt."
                }
            check(!viewModel.receivesOnlyContractTypes()) {
                "$name no longer violates its rule: remove it from ArchitectureDebt."
            }
        }
    }

    private fun assertStillViolates(
        debt: Set<String>,
        violates: (KoFileDeclaration) -> Boolean,
    ) {
        debt.forEach { entry ->
            val file =
                checkNotNull(productionScope.files.singleOrNull { it.path.endsWith(entry) }) {
                    "$entry: not found, renamed or deleted? Update ArchitectureDebt."
                }
            check(violates(file)) { "$entry no longer violates its rule: remove it from ArchitectureDebt." }
        }
    }
}

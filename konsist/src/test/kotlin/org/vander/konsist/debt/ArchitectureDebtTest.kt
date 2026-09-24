package org.vander.konsist.debt

import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import com.lemonappdev.konsist.api.declaration.KoInterfaceDeclaration
import org.junit.Test
import org.vander.konsist.FRAMEWORK_IMPORTS
import org.vander.konsist.SPOTIFY_LIB_INTERNALS
import org.vander.konsist.SPOTIFY_LIB_OUTER_LAYERS
import org.vander.konsist.appViewModels
import org.vander.konsist.hasIPrefix
import org.vander.konsist.hasImplSuffix
import org.vander.konsist.importsAndroidLog
import org.vander.konsist.importsAny
import org.vander.konsist.isUsedOutsideSpotifyLib
import org.vander.konsist.productionScope
import org.vander.konsist.receivesOnlyContractTypes
import org.vander.konsist.spotifyLibInterfaces

class ArchitectureDebtTest {
    @Test
    fun `every debt entry still violates its rule`() {
        assertStillViolates(ArchitectureDebt.spotifyLibDomainImpure) { it.importsAny(FRAMEWORK_IMPORTS) }
        assertStillViolates(ArchitectureDebt.spotifyLibDomainWrongDirection) { it.importsAny(SPOTIFY_LIB_OUTER_LAYERS) }
        assertStillViolates(ArchitectureDebt.appDependsOnSpotifyLibInternals) { it.importsAny(SPOTIFY_LIB_INTERNALS) }
        assertStillViolates(ArchitectureDebt.androidUtilLog) { it.importsAndroidLog() }
    }

    @Test
    fun `every misplaced contract is still misplaced`() {
        assertContractStillViolates(ArchitectureDebt.contractsToMoveToCoreDomain) { it.isUsedOutsideSpotifyLib() }
        assertContractStillViolates(ArchitectureDebt.contractsToMakeInternal) {
            !it.isUsedOutsideSpotifyLib() && !it.hasInternalModifier
        }
    }

    private fun assertContractStillViolates(
        debt: Set<String>,
        violates: (KoInterfaceDeclaration) -> Boolean,
    ) {
        debt.forEach { fqn ->
            val contract =
                checkNotNull(spotifyLibInterfaces.singleOrNull { it.fullyQualifiedName == fqn }) {
                    "$fqn: not found, renamed or moved? Update ArchitectureDebt."
                }
            check(violates(contract)) { "$fqn no longer violates its rule: remove it from ArchitectureDebt." }
        }
    }

    @Test
    fun `every misnamed declaration is still misnamed`() {
        val interfaces = productionScope.interfaces()
        val classes = productionScope.classes()
        assertStillMisnamed(ArchitectureDebt.interfacesWithIPrefix, interfaces.map { it.fullyQualifiedName to it.hasIPrefix() })
        assertStillMisnamed(ArchitectureDebt.classesWithImplSuffix, classes.map { it.fullyQualifiedName to it.hasImplSuffix() })
    }

    private fun assertStillMisnamed(
        debt: Set<String>,
        declarations: List<Pair<String?, Boolean>>,
    ) {
        debt.forEach { fqn ->
            val misnamed =
                checkNotNull(declarations.singleOrNull { it.first == fqn }) {
                    "$fqn: not found, renamed or moved? Update ArchitectureDebt."
                }.second
            check(misnamed) { "$fqn no longer violates its rule: remove it from ArchitectureDebt." }
        }
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

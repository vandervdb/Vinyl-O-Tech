package org.vander.konsist

import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test
import org.vander.konsist.debt.ArchitectureDebt

/**
 * A name says what the type, package or module does not already say: a port names its role,
 * an implementation names what sets it apart (its technology or source), never just "Impl".
 */
class NamingConventionTest {
    @Test
    fun `production interfaces have no I prefix`() {
        productionScope
            .interfaces()
            .filterNot { it.fullyQualifiedName in ArchitectureDebt.interfacesWithIPrefix }
            .assertFalse(strict = true) { it.hasIPrefix() }
    }

    @Test
    fun `production classes have no Impl suffix`() {
        productionScope
            .classes()
            .filterNot { it.fullyQualifiedName in ArchitectureDebt.classesWithImplSuffix }
            .assertFalse(strict = true) { it.hasImplSuffix() }
    }

    @Test
    fun `spotify-lib adapters of core domain ports are named after Spotify`() {
        productionScope
            .classes()
            .filter { it.resideInModule("spotify-lib") && it.implementsCoreDomainPort() }
            .filterNot { it.fullyQualifiedName in ArchitectureDebt.adaptersNotNamedAfterSpotify }
            .assertTrue(strict = true) { it.name.startsWith("Spotify") }
    }

    @Test
    fun `app ViewModels are named Spotify Feature ViewModel`() {
        appViewModels.assertTrue(strict = true) { it.name.startsWith("Spotify") && it.name.endsWith("ViewModel") }
    }

    @Test
    fun `fake ViewModels are named Fake Feature ViewModel`() {
        productionScope
            .classes()
            .filter { viewModel ->
                viewModel.resideInModule("fake") &&
                    viewModel.hasParentImportedFrom("org.vander.core.ui.") &&
                    viewModel.parents().any { it.name.endsWith("ViewModel") }
            }.assertTrue(strict = true) { it.name.startsWith("Fake") && it.name.endsWith("ViewModel") }
    }
}

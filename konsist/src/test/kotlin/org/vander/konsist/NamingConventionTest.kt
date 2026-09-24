package org.vander.konsist

import com.lemonappdev.konsist.api.verify.assertFalse
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
}

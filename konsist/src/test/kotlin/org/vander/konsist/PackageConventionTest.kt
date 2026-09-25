package org.vander.konsist

import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

/**
 * Kotlin accepts a `package` line that does not match the file's directory, but every rule here
 * selects files by package: a data source dragged into `data/` while still declaring a `domain`
 * package was checked as domain code.
 */
class PackageConventionTest {
    @Test
    fun `every production file declares the package of its directory`() {
        productionScope.packages.assertTrue(strict = true) { it.hasMatchingPath }
    }
}

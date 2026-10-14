package org.vander.konsist

import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.Test
import org.vander.konsist.debt.ArchitectureDebt
import org.vander.konsist.debt.isListedIn

class LoggingConventionTest {
    @Test
    fun `production code logs through Logger, not android util Log`() {
        productionScope
            .files
            .filterNot { it.isListedIn(ArchitectureDebt.androidUtilLog) }
            .assertFalse(strict = true) { it.importsAndroidLog() }
    }
}

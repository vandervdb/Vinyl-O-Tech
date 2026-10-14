package org.vander.konsist

import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.Test

class CoreArchitectureTest {
    @Test
    fun `core domain imports no framework or DTO type`() {
        productionScope
            .files
            .withPackage("org.vander.core.domain..")
            .assertFalse(strict = true) { it.importsAny(FRAMEWORK_IMPORTS) }
    }

    @Test
    fun `core security api imports no implementation or backend library`() {
        productionScope
            .files
            .withPackage("org.vander.core.security.api..")
            .assertFalse(strict = true) { it.importsAny(SECURITY_BACKEND_IMPORTS) }
    }
}

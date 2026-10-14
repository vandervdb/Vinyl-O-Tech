package org.vander.konsist

import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test
import org.vander.konsist.debt.ArchitectureDebt

class AppViewModelTest {
    @Test
    fun `app ViewModels receive only contract module types`() {
        val viewModels = appViewModels
        // Checked before the debt filter: every ViewModel is indebted today, so strict mode
        // would fail on the filtered list, yet an empty lookup must still fail the rule.
        check(viewModels.isNotEmpty()) { "No @HiltViewModel found in app: the rule checks nothing." }

        viewModels
            .filterNot { it.name in ArchitectureDebt.viewModelsDependingOnSpotifyLib }
            .assertTrue(strict = false) { it.receivesOnlyContractTypes() }
    }
}

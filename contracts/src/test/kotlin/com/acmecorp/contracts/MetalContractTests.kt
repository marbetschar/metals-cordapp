package com.acmecorp.contracts

import org.junit.Test
import net.corda.core.contracts.Contract
import kotlin.test.assertTrue

class MetalContractTests {

    @Test
    fun `metal contract - implements contract`() {
        assertTrue(MetalContract() is Contract)
    }
}
package com.acmecorp.states

import net.corda.core.contracts.ContractState
import net.corda.core.identity.CordaX500Name
import org.junit.Test
import net.corda.testing.core.TestIdentity
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MetalStateTests {

    private val mintIdentity = TestIdentity(CordaX500Name("Mint", "London", "GB"))
    private val traderIdentity = TestIdentity(CordaX500Name("Trader", "London", "GB"))

    @Test
    fun `metal state implements contract state`() {
        assertTrue(MetalState("Gold", 10, mintIdentity.party, traderIdentity.party) is ContractState)
    }

    @Test
    fun `metal state has two participants - the issuer and the owner`() {
        val metalState = MetalState("Gold", 10, mintIdentity.party, traderIdentity.party)
        assertEquals(2, metalState.participants.size)
        assertTrue(metalState.participants.contains(mintIdentity.party))
        assertTrue(metalState.participants.contains(traderIdentity.party))
    }

    @Test
    fun `metal state getters provide expected values`() {
        val metalState = MetalState("Gold", 10, mintIdentity.party, traderIdentity.party)

        assertEquals("Gold", metalState.material)
        assertEquals(10, metalState.weight)
        assertEquals(mintIdentity.party, metalState.issuer)
        assertEquals(traderIdentity.party, metalState.owner)
    }
}
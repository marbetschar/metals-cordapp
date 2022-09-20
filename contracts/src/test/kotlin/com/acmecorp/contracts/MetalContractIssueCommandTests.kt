package com.acmecorp.contracts

import com.acmecorp.states.MetalState
import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import org.junit.Test
import net.corda.core.contracts.Contract
import net.corda.testing.contracts.DummyState
import net.corda.testing.core.DummyCommandData
import net.corda.testing.node.transaction
import kotlin.test.assertTrue

class MetalContractIssueCommandTests {

    private val mintIdentity = TestIdentity(CordaX500Name("Mint", "London", "GB"))
    private val traderAIdentity = TestIdentity(CordaX500Name("TraderA", "London", "GB"))

    private val ledgerServices = MockServices(listOf("com.acmecorp"))
    private val metalState = MetalState("Gold", 10, mintIdentity.party, traderAIdentity.party)

    @Test
    fun `metal contract - issue command - require command of type Issue`() {
        ledgerServices.transaction {
            output(MetalContract.CONTRACT_ID, metalState)
            command(mintIdentity.publicKey, DummyCommandData)
            fails()
        }

        ledgerServices.transaction {
            output(MetalContract.CONTRACT_ID, metalState)
            command(mintIdentity.publicKey, MetalContract.Commands.Issue())
            verifies()
        }
    }

    @Test
    fun `metal contract - issue command - require zero inputs`() {
        ledgerServices.transaction {
            input(MetalContract.CONTRACT_ID, metalState)
            command(mintIdentity.publicKey, MetalContract.Commands.Issue())
            fails()
        }
    }

    @Test
    fun `metal contract - issue command - require one output`() {
        ledgerServices.transaction {
            output(MetalContract.CONTRACT_ID, metalState)
            command(mintIdentity.publicKey, MetalContract.Commands.Issue())
            verifies()
        }
    }

    @Test
    fun `metal contract - issue command - require output of type MetalState`() {
        ledgerServices.transaction {
            output(MetalContract.CONTRACT_ID, DummyState())
            command(mintIdentity.publicKey, MetalContract.Commands.Issue())
            fails()
        }

        ledgerServices.transaction {
            output(MetalContract.CONTRACT_ID, metalState)
            command(mintIdentity.publicKey, MetalContract.Commands.Issue())
            verifies()
        }
    }

    @Test
    fun `metal contract - issue command - requires to be signed by issuer`() {
        ledgerServices.transaction {
            output(MetalContract.CONTRACT_ID, metalState)
            command(traderAIdentity.publicKey, MetalContract.Commands.Issue())
            fails()
        }

        ledgerServices.transaction {
            output(MetalContract.CONTRACT_ID, metalState)
            command(mintIdentity.publicKey, MetalContract.Commands.Issue())
            verifies()
        }
    }
}
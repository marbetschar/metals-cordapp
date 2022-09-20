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

class MetalContractTransferCommandTests {

    private val mintIdentity = TestIdentity(CordaX500Name("Mint", "London", "GB"))
    private val traderAIdentity = TestIdentity(CordaX500Name("TraderA", "London", "GB"))
    private val traderBIdentity = TestIdentity(CordaX500Name("TraderB", "London", "GB"))

    private val ledgerServices = MockServices(listOf("com.acmecorp"))

    private val inputMetalState = MetalState("Gold", 10, mintIdentity.party, traderAIdentity.party)
    private val outputMetalState = MetalState("Gold", 10, mintIdentity.party, traderBIdentity.party)

    @Test
    fun `metal contract - transfer command - require command of type Transfer`() {
        ledgerServices.transaction {
            input(MetalContract.CONTRACT_ID, inputMetalState)
            output(MetalContract.CONTRACT_ID, outputMetalState)
            command(traderAIdentity.publicKey, DummyCommandData)
            fails()
        }

        ledgerServices.transaction {
            input(MetalContract.CONTRACT_ID, inputMetalState)
            output(MetalContract.CONTRACT_ID, outputMetalState)
            command(traderAIdentity.publicKey, MetalContract.Commands.Transfer())
            verifies()
        }
    }

    @Test
    fun `metal contract - transfer command - require one input and one output`() {
        ledgerServices.transaction {
            output(MetalContract.CONTRACT_ID, outputMetalState)
            command(traderAIdentity.publicKey, MetalContract.Commands.Transfer())
            fails()
        }

        ledgerServices.transaction {
            input(MetalContract.CONTRACT_ID, inputMetalState)
            command(traderAIdentity.publicKey, MetalContract.Commands.Transfer())
            fails()
        }

        ledgerServices.transaction {
            input(MetalContract.CONTRACT_ID, inputMetalState)
            output(MetalContract.CONTRACT_ID, outputMetalState)
            command(traderAIdentity.publicKey, MetalContract.Commands.Transfer())
            verifies()
        }
    }

    @Test
    fun `metal contract - transfer command - requires to be signed by owner of the input state`() {
        ledgerServices.transaction {
            input(MetalContract.CONTRACT_ID, inputMetalState)
            output(MetalContract.CONTRACT_ID, outputMetalState)
            command(mintIdentity.publicKey, MetalContract.Commands.Transfer())
            fails()
        }

        ledgerServices.transaction {
            input(MetalContract.CONTRACT_ID, inputMetalState)
            output(MetalContract.CONTRACT_ID, outputMetalState)
            command(traderBIdentity.publicKey, MetalContract.Commands.Transfer())
            fails()
        }

        ledgerServices.transaction {
            input(MetalContract.CONTRACT_ID, inputMetalState)
            output(MetalContract.CONTRACT_ID, outputMetalState)
            command(traderAIdentity.publicKey, MetalContract.Commands.Transfer())
            verifies()
        }
    }
}
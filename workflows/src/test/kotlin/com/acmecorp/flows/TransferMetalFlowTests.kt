package com.acmecorp.flows

import com.acmecorp.contracts.MetalContract
import net.corda.testing.node.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import net.corda.core.identity.CordaX500Name
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class TransferMetalFlowTests {
    private lateinit var network: MockNetwork
    private lateinit var mint: StartedMockNode
    private lateinit var traderA: StartedMockNode
    private lateinit var traderB: StartedMockNode

    @Before
    fun setup() {
        network = MockNetwork(MockNetworkParameters(
            cordappsForAllNodes = listOf(
                TestCordapp.findCordapp("com.acmecorp.contracts"),
                TestCordapp.findCordapp("com.acmecorp.flows")
            ),
            notarySpecs = listOf(MockNetworkNotarySpec(CordaX500Name("Notary","London","GB")))
        ))
        mint = network.createPartyNode()
        traderA = network.createPartyNode()
        traderB = network.createPartyNode()
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    fun `transfer metal flow - transaction has exactly one input and exactly one output both of type MetalState`() {
        val issueMetalFlow = IssueMetalFlow("Gold", 10, traderA.info.legalIdentities.first())
        val transferMetalFlow = TransferMetalFlow("Gold", 10, traderB.info.legalIdentities.first())

        mint.startFlow(issueMetalFlow).also{ network.runNetwork() }.get()
        val signedTransferTransaction = traderA.startFlow(transferMetalFlow).also{ network.runNetwork() }.get()

        assertEquals(1, signedTransferTransaction.tx.inputs.size)
        assertEquals(1, signedTransferTransaction.tx.outputStates.size)
    }

    @Test
    fun `transfer metal flow - transaction has correct contract with one transfer command and signed by owner`() {
        val issueMetalFlow = IssueMetalFlow("Gold", 10, traderA.info.legalIdentities.first())
        val transferMetalFlow = TransferMetalFlow("Gold", 10, traderB.info.legalIdentities.first())

        mint.startFlow(issueMetalFlow).also{ network.runNetwork() }.get()
        val signedTransferTransaction = traderA.startFlow(transferMetalFlow).also{ network.runNetwork() }.get()

        val output = signedTransferTransaction.tx.outputs.singleOrNull()
        assertEquals(MetalContract.CONTRACT_ID, output?.contract)

        assertEquals(1, signedTransferTransaction.tx.commands.size)
        val command = signedTransferTransaction.tx.commands.singleOrNull()
        assertTrue(command?.value is MetalContract.Commands.Transfer)

        assertEquals(1, command?.signers?.size)
        assertTrue(command?.signers?.contains(traderA.info.legalIdentities.singleOrNull()?.owningKey) ?: false)
    }
}
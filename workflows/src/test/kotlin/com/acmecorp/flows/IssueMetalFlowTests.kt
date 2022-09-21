package com.acmecorp.flows

import com.acmecorp.contracts.MetalContract
import com.acmecorp.states.MetalState
import net.corda.testing.node.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import net.corda.core.identity.CordaX500Name
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class IssueMetalFlowTests {
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
    fun `issue metal flow - transaction has no inputs and exactly one metal state output with the correct owner`() {
        val issueMetalFlow = IssueMetalFlow("Gold", 10, traderA.info.legalIdentities.first())
        val signedIssueTransaction = mint.startFlow(issueMetalFlow).also{ network.runNetwork() }.get()

        assertEquals(0, signedIssueTransaction.tx.inputs.size)
        assertEquals(1, signedIssueTransaction.tx.outputStates.size)

        val outputMetalState = signedIssueTransaction.tx.outputsOfType<MetalState>().singleOrNull()
        assertEquals(traderA.info.legalIdentities.singleOrNull(), outputMetalState?.owner)
    }

    @Test
    fun `issue metal flow - transaction has correct contract with one issue command and signed by issuer`() {
        val issueMetalFlow = IssueMetalFlow("Gold", 10, traderA.info.legalIdentities.first())
        val signedIssueTransaction = mint.startFlow(issueMetalFlow).also{ network.runNetwork() }.get()

        val output = signedIssueTransaction.tx.outputs.singleOrNull()
        assertEquals(MetalContract.CONTRACT_ID, output?.contract)

        assertEquals(1, signedIssueTransaction.tx.commands.size)
        val command = signedIssueTransaction.tx.commands.singleOrNull()
        assertTrue(command?.value is MetalContract.Commands.Issue)

        assertEquals(1, command?.signers?.size)
        assertTrue(command?.signers?.contains(mint.info.legalIdentities.singleOrNull()?.owningKey) ?: false)
    }
}
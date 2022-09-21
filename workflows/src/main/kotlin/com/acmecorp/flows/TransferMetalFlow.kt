package com.acmecorp.flows

import co.paralleluniverse.fibers.Suspendable
import com.acmecorp.contracts.MetalContract
import com.acmecorp.states.MetalState
import net.corda.core.flows.*
import net.corda.core.utilities.ProgressTracker
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.CollectSignaturesFlow
import net.corda.core.transactions.SignedTransaction
import java.util.stream.Collectors
import net.corda.core.flows.FlowSession
import net.corda.core.identity.Party
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.identity.AbstractParty
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria


// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class TransferMetalFlow(
    private val material: String,
    private val weight: Int,
    private val newOwner: Party
) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {
        // Step 1. Get a reference to the notary service on our network and our key pair.
        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        // Step 2. Compose the input and output states which carry the MetalState
        val input = serviceHub.vaultService.queryBy(
            contractStateType = MetalState::class.java,
            criteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)
        ).states.firstOrNull { it.state.data.material == material && it.state.data.weight == weight } ?: throw FlowException()
        val output = MetalState(material, weight, input.state.data.issuer, newOwner)

        // Step 3. Create a new TransactionBuilder object.
        val builder = TransactionBuilder(notary)
                .addCommand(MetalContract.Commands.Transfer(), listOf(ourIdentity.owningKey))
                .addInputState(input)
                .addOutputState(output)

        // Step 4. Verify and sign it with our KeyPair.
        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)

        // Step 5. Collect the party's signatures using the SignTransactionFlow.
        val otherParties: MutableList<Party> = output.participants.stream().map { el: AbstractParty? -> el as Party? }.collect(Collectors.toList())
        otherParties.remove(ourIdentity)
        val sessions = otherParties.stream().map { el: Party? -> initiateFlow(el!!) }.collect(Collectors.toList())

        val stx = subFlow(CollectSignaturesFlow(ptx, sessions))

        // Step 6. Assuming no exceptions, we can now finalise the transaction
        return subFlow<SignedTransaction>(FinalityFlow(stx, sessions))
    }
}

@InitiatedBy(TransferMetalFlow::class)
class TransferMetalResponderFlow(private val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        println("Received transferred Metal.")

        return subFlow(ReceiveFinalityFlow(counterpartySession))
    }
}
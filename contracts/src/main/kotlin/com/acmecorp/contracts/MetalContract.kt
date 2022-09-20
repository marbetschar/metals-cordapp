package com.acmecorp.contracts

import com.acmecorp.states.MetalState
import com.template.states.TemplateState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.contracts.requireThat
// ************
// * Contract *
// ************
class MetalContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val CONTRACT_ID = "com.acmecorp.contracts.MetalContract"
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        // Verification logic goes here.
        val command = tx.commands.requireSingleCommand<Commands>()
        when (command.value) {
            is Commands.Issue -> requireThat {
                // Shape Rules
                "Issue command can't have any input states".using(tx.inputStates.isEmpty())
                "Issue command needs exactly one output state".using(tx.outputStates.size == 1)

                // Content Rules
                "Issue command output state must be of type MetalState".using(tx.outputStates.singleOrNull() is MetalState)

                val outputState = tx.outputsOfType<MetalState>().singleOrNull()
                "Issue command output state material must be either Gold or Silver".using(listOf("Gold", "Silver").contains(outputState?.material))

                // Signer Rules
                "Issue command needs to be signed by the issuer".using(command.signers.contains(outputState?.issuer?.owningKey))
            }
            is Commands.Transfer -> requireThat {
                // Shape Rules
                "Transfer command needs exactly one input states".using(tx.inputStates.size == 1)
                "Transfer command needs exactly one output state".using(tx.outputStates.size == 1)

                // Content Rules
                "Transfer command input state must be of type MetalState".using(tx.inputStates.singleOrNull() is MetalState)
                "Transfer command output state must be of type MetalState".using(tx.outputStates.singleOrNull() is MetalState)

                val inputState =  tx.inputsOfType<MetalState>().singleOrNull()
                "Transfer command input state material must be either Gold or Silver".using(listOf("Gold", "Silver").contains(inputState?.material))

                val outputState = tx.outputsOfType<MetalState>().singleOrNull()
                "Transfer command output state material must be either Gold or Silver".using(listOf("Gold", "Silver").contains(outputState?.material))

                // Signer Rules
                "Transfer command needs to be signed by the owner of the input state".using(command.signers.contains(inputState?.owner?.owningKey))
            }
            else -> {
                throw IllegalArgumentException("${command.value} command is not verified by MetalContract")
            }
        }
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class Issue : Commands
        class Transfer : Commands
    }
}
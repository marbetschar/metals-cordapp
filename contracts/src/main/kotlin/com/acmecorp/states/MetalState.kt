package com.acmecorp.states

import com.acmecorp.contracts.MetalContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

// *********
// * State *
// *********
@BelongsToContract(MetalContract::class)
data class MetalState(
    val material: String,
    val weight: Int,
    val issuer: Party,
    val owner: Party,
    override val participants: List<AbstractParty> = listOf(issuer, owner)
) : ContractState

/*
 * Copyright (c) 2015-2020, Virgil Security, Inc.
 *
 * Lead Maintainer: Virgil Security Inc. <support@virgilsecurity.com>
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  
 *     (1) Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 *
 *     (2) Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *     (3) Neither the name of virgil nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.virgilsecurity.android.common.worker

import com.virgilsecurity.android.common.CardFilterHelper
import com.virgilsecurity.android.common.exception.FindUsersException
import com.virgilsecurity.android.common.manager.LookupManager
import com.virgilsecurity.android.common.model.FindUsersResult
import com.virgilsecurity.android.common.model.LookupResult
import com.virgilsecurity.common.model.Completable
import com.virgilsecurity.common.model.Result
import com.virgilsecurity.sdk.cards.Card

/**
 * SearchWorker
 */
internal class SearchWorker internal constructor(
        private val lookupManager: LookupManager
) {

    internal fun findCachedUsers(identities: List<String>,
                                 checkResult: Boolean = true): Result<FindUsersResult> =
            object : Result<FindUsersResult> {
                override fun get(): FindUsersResult {
                    return lookupManager.lookupCachedCards(identities, checkResult)
                }
            }

    internal fun findCachedUser(identity: String): Result<Card?> = object : Result<Card?> {
        override fun get(): Card? {
            return try {
                lookupManager.lookupCachedCard(identity)
            } catch (exception: FindUsersException) {
                null
            }
        }
    }

    internal fun findUsers(identities: List<String>,
                           forceReload: Boolean = false,
                           checkResult: Boolean = true,
                           cardFilter: (card: Card) -> Boolean): Result<FindUsersResult> =
            object : Result<FindUsersResult> {
                override fun get(): FindUsersResult {
                    return lookupManager.lookupCards(identities, forceReload, checkResult, cardFilter)
                }
            }

    internal fun findUser(identity: String,
                          forceReload: Boolean = false, cardFilter: (card: Card) -> Boolean): Result<Card> = object : Result<Card> {
        override fun get(): Card {
            return lookupManager.lookupCard(identity, forceReload, cardFilter)
        }
    }

    internal fun updateCachedUsers(): Completable = object : Completable {
        override fun execute() {
            lookupManager.startUpdateCachedCards()
        }
    }

    @Deprecated("Use findUser instead.")
    internal fun lookupPublicKey(identity: String): Result<LookupResult> =
            lookupPublicKeys(listOf(identity), CardFilterHelper::AcceptAll)

    @Deprecated("Use findUsers instead.")
    internal fun lookupPublicKeys(identities: List<String>, cardFilter: (card: Card) -> Boolean): Result<LookupResult> =
            object : Result<LookupResult> {
                override fun get(): LookupResult {
                    require(identities.isNotEmpty()) { "\'identities\' should not be empty" }

                    val cards = findUsers(identities, forceReload = true, checkResult = true, cardFilter = cardFilter).get()
                    return cards.mapValues { it.component2().publicKey }
                }
            }
}

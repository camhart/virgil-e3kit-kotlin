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

import com.virgilsecurity.android.common.exception.EThreeException
import com.virgilsecurity.android.common.storage.local.LocalKeyStorage
import com.virgilsecurity.common.model.Completable
import com.virgilsecurity.sdk.cards.Card
import com.virgilsecurity.sdk.cards.CardManager
import com.virgilsecurity.sdk.crypto.VirgilKeyPair

/**
 * AuthorizationWorker
 */
internal class AuthorizationWorker internal constructor(
        private val cardManager: CardManager,
        private val localKeyStorage: LocalKeyStorage,
        private val identity: String,
        private val publishCardThenSaveLocal: (VirgilKeyPair?, String?, Map<String, String>?) -> Card,
        private val privateKeyDeleted: () -> Unit
) {

    @Synchronized
    @JvmOverloads
    internal fun register(keyPair: VirgilKeyPair? = null, cardFilter: (card: Card) -> Boolean, additionalData: Map<String, String>, onSuccess: (card: Card) -> Unit) = object : Completable {
        override fun execute() {
            if (localKeyStorage.exists())
                throw EThreeException(EThreeException.Description.PRIVATE_KEY_EXISTS)

            val tempCards = cardManager.searchCards(this@AuthorizationWorker.identity)

            val cards = arrayListOf<Card>();
            for(card in tempCards) {
                if(cardFilter(card)) {
                    cards.add(card)
                }
            }

            if (cards.isNotEmpty()) {
                throw EThreeException(
                    EThreeException.Description.USER_IS_ALREADY_REGISTERED
                )
            }

            val card = publishCardThenSaveLocal(keyPair, null, additionalData)
            onSuccess(card)
        }
    }

    @Synchronized internal fun unregister() = object : Completable {
        override fun execute() {
            val cards = cardManager.searchCards(this@AuthorizationWorker.identity)
            val card = cards.firstOrNull()
                       ?: throw EThreeException(EThreeException.Description.USER_IS_NOT_REGISTERED)

            cardManager.revokeCard(card.identifier)
            localKeyStorage.delete()
            privateKeyDeleted()
        }
    }

    @Synchronized internal fun rotatePrivateKey(additionalData: Map<String, String>, cardFilter: (card: Card) -> Boolean, onSuccess: (card: Card) -> Unit) = object : Completable {
        override fun execute() {
            if (localKeyStorage.exists())
                throw EThreeException(EThreeException.Description.PRIVATE_KEY_EXISTS)

            val tempCards = cardManager.searchCards(this@AuthorizationWorker.identity)
            val cards = tempCards.filter(cardFilter);
            val card = cards.firstOrNull()
                       ?: throw EThreeException(EThreeException.Description.USER_IS_NOT_REGISTERED)

            val publishedCard = publishCardThenSaveLocal(null, card.identifier, additionalData)
            onSuccess(publishedCard)
        }
    }

    internal fun hasLocalPrivateKey() = localKeyStorage.exists()

    internal fun cleanup() {
        localKeyStorage.delete()
        privateKeyDeleted()
    }
}

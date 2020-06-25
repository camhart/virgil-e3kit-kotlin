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

package com.virgilsecurity.android.common.storage.local

import com.virgilsecurity.android.common.exception.EThreeException
import com.virgilsecurity.common.model.Data
import com.virgilsecurity.sdk.crypto.VirgilCrypto
import com.virgilsecurity.sdk.crypto.VirgilKeyPair
import com.virgilsecurity.sdk.crypto.exceptions.KeyEntryNotFoundException
import com.virgilsecurity.sdk.storage.JsonKeyEntry
import com.virgilsecurity.sdk.storage.KeyStorage

/**
 * Local KeyStorage.
 */
class LocalKeyStorage internal constructor(
        internal val identity: String,
        private val keyStorage: KeyStorage,
        internal val crypto: VirgilCrypto,
        internal val keyPrefix: String
) {

    protected fun key() = "$keyPrefix.$identity"

    internal fun exists() = keyStorage.exists(key())

    internal fun store(privateKeyData: Data) =
            keyStorage.store(JsonKeyEntry(key(), privateKeyData.value))

    internal fun retrieveKeyPair(): VirgilKeyPair = try {
        val privateKeyData = keyStorage.load(key())
        crypto.importPrivateKey(privateKeyData.value)
    } catch (e: KeyEntryNotFoundException) {
        throw EThreeException(EThreeException.Description.MISSING_PRIVATE_KEY)
    }

    internal fun delete() = try {
        keyStorage.delete(key())
    } catch (exception: KeyEntryNotFoundException) {
        throw EThreeException(EThreeException.Description.MISSING_PRIVATE_KEY)
    }
}

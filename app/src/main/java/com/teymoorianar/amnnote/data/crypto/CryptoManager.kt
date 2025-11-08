package com.teymoorianar.amnnote.data.crypto

import android.security.keystore.*
import java.security.KeyStore
import javax.crypto.*
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject


class CryptoManager @Inject constructor() {

    private val keyAlias = "notes_aes_key"
    private val transformation = "AES/GCM/NoPadding"
    private val keyStoreType = "AndroidKeyStore"

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(keyStoreType).apply { load(null) }
        val existingKey = keyStore.getKey(keyAlias, null)
        if (existingKey is SecretKey) return existingKey

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, keyStoreType)
        val parameterSpec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(false)
            .build()

        keyGenerator.init(parameterSpec)
        return keyGenerator.generateKey()
    }

    fun encrypt(plainText: String): ByteArray {
        val key = getOrCreateKey()
        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        // store iv + ciphertext
        return iv + cipherBytes
    }

    fun decrypt(data: ByteArray): String {
        val key = getOrCreateKey()

        val ivSize = 12 // AES-GCM standard IV length
        val iv = data.copyOfRange(0, ivSize)
        val cipherBytes = data.copyOfRange(ivSize, data.size)

        val cipher = Cipher.getInstance(transformation)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        val plainBytes = cipher.doFinal(cipherBytes)
        return plainBytes.toString(Charsets.UTF_8)
    }
}

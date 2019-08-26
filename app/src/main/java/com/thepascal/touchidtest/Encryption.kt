package com.thepascal.touchidtest

import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec


class Encryption {

    fun keyStoreEncrypt(dataToEncrypt: ByteArray): HashMap<String, ByteArray>{
        val map = HashMap<String, ByteArray>()

        try{
            //Get the keyStore
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            val secretKeyEntry = keyStore.getEntry("MyKeyAlias", null) as KeyStore.SecretKeyEntry
            val secretKey = secretKeyEntry.secretKey

            //Encrypt data
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val ivBytes = cipher.iv
            val encryptedBytes = cipher.doFinal(dataToEncrypt)

            map["iv"] = ivBytes
            map["encrypted"] = encryptedBytes
        }catch (t: Throwable){
            t.printStackTrace()
        }
        return map
    }

    fun keyStoreDecrypt(map: HashMap<String, ByteArray>): ByteArray?{
        var decrypted: ByteArray? = null
        try{
            //Get the key
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            val secretKeyEntry = keyStore.getEntry("MyKeyAlias", null) as KeyStore.SecretKeyEntry
            val secretKey = secretKeyEntry.secretKey

            //Extract info from map
            val encryptedBytes = map["encrypted"]
            val ivBytes = map["iv"]

            //Decrypt data
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, ivBytes)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            decrypted = cipher.doFinal(encryptedBytes)
        }catch (t: Throwable){
            t.printStackTrace()
        }

        return decrypted
    }
}
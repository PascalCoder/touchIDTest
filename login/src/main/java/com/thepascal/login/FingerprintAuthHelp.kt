package com.thepascal.login

import android.Manifest
import android.annotation.TargetApi
import android.app.KeyguardManager
import android.content.Context
import android.content.Context.FINGERPRINT_SERVICE
import android.content.Context.KEYGUARD_SERVICE
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import android.os.CancellationSignal
import android.util.Log
import androidx.core.app.ActivityCompat
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.IllegalArgumentException
import java.lang.StringBuilder
import java.nio.charset.Charset
import java.security.*
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.security.cert.CertificateException

class FingerprintAuthHelp(var context: Context) {

    companion object{
        private const val FINGER_PRINT_HELPER = "FingerprintAuthHelper"
        private const val ENCRYPTED_PASS_SHARED_PREF_KEY = "Encrypted_Pass_Shared_Pref_Key"
        private const val LAST_USED_IV_SHARED_PREF_KEY = "Last_Used_IV_Shared_Pref_Key"
        private const val MY_APP_ALIAS = "My_App_Alias"
    }

    private lateinit var keyguardManager: KeyguardManager
    private lateinit var fingerprintManager: FingerprintManager

    //private lateinit var context: Context
    private lateinit var keyStore: KeyStore
    private lateinit var keyGenerator: KeyGenerator

    private lateinit var lastError: String

    interface Callback{
        fun onSuccess(savedPass: String)
        fun onFailure(message: String)
        fun onHelp(helpCode: Int, helpString: String)
    }

    fun getLastError() = lastError

    @TargetApi(Build.VERSION_CODES.M)
    fun init(): Boolean{
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            setError("This phone does not support fingerprint authentication")
            return false
        }

        keyguardManager = context.getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        fingerprintManager = context.getSystemService(FINGERPRINT_SERVICE) as FingerprintManager

        if(!keyguardManager.isKeyguardSecure){
            setError("User hasn't enabled Lock Screen")
            return false
        }

        if(!hasPermission()){
            setError("User hasn't granted permission to use Fingerprint")
            return false
        }

        if(!fingerprintManager.hasEnrolledFingerprints()){
            setError("User hasn't registered any fingerprints")
            return false
        }

        if(!initKeyStore()){
            return false
        }

        return false
    }

    @Nullable
    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun createCipher(mode: Int): Cipher? {

        val cipher: Cipher?
        try {
            cipher = Cipher.getInstance(
                KeyProperties.KEY_ALGORITHM_AES + "/"
                        + KeyProperties.BLOCK_MODE_CBC + "/"
                        + KeyProperties.ENCRYPTION_PADDING_PKCS7)
        }catch (ae: NoSuchAlgorithmException){ // NoSuchPaddingException
            throw RuntimeException("Failed to get Cipher")
        }catch (nsp: NoSuchPaddingException){
            throw RuntimeException("Failed to get Cipher")
        }

        val key:SecretKey?
        try {
            key = keyStore.getKey(MY_APP_ALIAS, null) as SecretKey?

            if(key == null){
                return null
            }
            if (mode == Cipher.ENCRYPT_MODE){
                cipher.init(mode, key)
                val iv: ByteArray = cipher.iv
                saveIv(iv)
            }else{
                val lastIv: ByteArray? = getLastIv()
                cipher.init(mode, key, IvParameterSpec(lastIv))
            }

        } catch (e: KeyPermanentlyInvalidatedException) {

        } catch (e: KeyStoreException) {

            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: CertificateException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: UnrecoverableKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: IOException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: InvalidKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        }

        return cipher
    }

    @NonNull
    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun createKeyGenParameterSpec(): KeyGenParameterSpec {
        return KeyGenParameterSpec.Builder(MY_APP_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setUserAuthenticationRequired(true)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .build()
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    fun initKeyStore(): Boolean{

        try{
            keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            keyStore.load(null)

            if(getLastIv() == null ){
                val keyGeneratorSpec: KeyGenParameterSpec = createKeyGenParameterSpec()
                keyGenerator.init(keyGeneratorSpec)
                keyGenerator.generateKey()
            }
        }catch (t: Throwable){
            setError("Failed init of keyStore & keyGenerator: " + t.message)
            return false
        }
        return true
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun authenticate(cancellationSignal: CancellationSignal,
                             authListener: FingerPrintAuthenticationListener, mode: Int){

        try{
            if(hasPermission()){
                val cipher = createCipher(mode)
                val crypto: FingerprintManager.CryptoObject = FingerprintManager.CryptoObject(cipher)
                fingerprintManager.authenticate(crypto, cancellationSignal, 0, authListener, null)
            }else{
                authListener.callback.onFailure("User hasn't granted permission to use Fingerprint")
            }
        }catch (t: Throwable){
            authListener.callback.onFailure("An error occurred: " + t.message)
        }
    }

    private fun getSavedEncryptedPassword(): String?{
        val sharedPreferences: SharedPreferences? = getSharedPreferences()
        if(sharedPreferences != null){
            return sharedPreferences.getString(ENCRYPTED_PASS_SHARED_PREF_KEY, null)
        }
        return null
    }

    private fun saveEncryptedPassword(encryptedPassword: String){
        val edit: SharedPreferences.Editor = getSharedPreferences().edit()
        edit.putString(ENCRYPTED_PASS_SHARED_PREF_KEY, encryptedPassword)
        edit.commit()
    }

    private fun getLastIv(): ByteArray?{
        val sharedPreferences: SharedPreferences? = getSharedPreferences()

        if(sharedPreferences != null){
            val ivString = sharedPreferences.getString(LAST_USED_IV_SHARED_PREF_KEY, null)

            if(ivString != null){
                return decodeBytes(ivString)
            }
        }
        return null
    }

    private fun saveIv(iv: ByteArray){
        val edit: SharedPreferences.Editor = getSharedPreferences().edit()
        val string = encodeBytes(iv)
        edit.putString(LAST_USED_IV_SHARED_PREF_KEY, string)
        edit.commit()
    }

    private fun getSharedPreferences(): SharedPreferences{
        return context.getSharedPreferences(FINGER_PRINT_HELPER, 0)
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun hasPermission(): Boolean{
        return (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT)
                == PackageManager.PERMISSION_GRANTED)
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    fun savePassword(password: String, cancellationSignal: CancellationSignal, callback: Callback){
        authenticate(cancellationSignal, FingerPrintEncryptPasswordListener(callback, password),
                    Cipher.ENCRYPT_MODE)
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    fun getPassword(cancellationSignal: CancellationSignal, callback: Callback){
        authenticate(cancellationSignal, FingerPrintDecryptPasswordListener(callback),
            Cipher.DECRYPT_MODE)
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    fun encryptPassword(cipher: Cipher?, password: String): Boolean{
        try{
            //Encrypt the text
            if(password.isEmpty()){
                setError("Password is empty")
                return false
            }

            if(cipher == null){
                setError("Could not create cipher")
                return false
            }

            val outputStream: ByteArrayOutputStream = ByteArrayOutputStream()
            val cipherOutputStream: CipherOutputStream = CipherOutputStream(outputStream, cipher)
            val bytes: ByteArray = password.toByteArray(Charset.defaultCharset()) //password.getBytes(Charset.defaultCharset())
            cipherOutputStream.write(bytes)
            cipherOutputStream.flush()
            cipherOutputStream.close()
            saveEncryptedPassword(encodeBytes(outputStream.toByteArray()))
        }catch (t: Throwable){
            setError("Encryption failed " + t.message)
            return false
        }

        return true
    }

    private fun decodeBytes(s: String): ByteArray{
        val len = s.length

        //"111" is not a valid hex encoding
        if(len % 2 != 0) throw IllegalArgumentException("hexBinary needs to be even-length: $s")

        val out: ByteArray = ByteArray(len/2)

        var i = 0
        while( i < len){
            val h = hexToBin(s[i])
            val l = hexToBin(s[i+1])
            if(h == -1 || l == -1) throw IllegalArgumentException("Contains illegal character for hexBinary: $s")

            out[i/2] = (h*16 + l) as Byte

            i += 2
        }

        return out
    }

    private fun hexToBin(ch: Char): Int {
        if('0' <= ch && ch <= '9') return ch-'0' //ch in '0' .. '9'
        if('A' <= ch && ch <= 'F') return ch-'A'+10
        if('a'<= ch && ch <= 'f') return ch-'a'+10

        return -1
    }

    private val hexCode: CharArray = "0123456789ABCDEF".toCharArray()

    fun encodeBytes(data: ByteArray): String{
        var r: StringBuilder = StringBuilder(data.size * 2)

        for(b in data){
            r.append(hexCode[(b.toInt() shr 4) and 0xF])
            r.append(hexCode[(b.toInt() and 0xF)])
        }

        return r.toString()
    }

    @NonNull
    private fun decipher(cipher: Cipher?): String? {
        var retVal: String? = null
        val savedEncryptedPassword = getSavedEncryptedPassword()
        if (savedEncryptedPassword != null){
            val decodedPassword = decodeBytes(savedEncryptedPassword)
            val cipherInputStream = CipherInputStream(ByteArrayInputStream(decodedPassword), cipher)

            val values = ArrayList<Byte>()
            var nextByte: Int = cipherInputStream.read()
            while ((nextByte) != -1){
                values.add(nextByte as Byte)

                nextByte = cipherInputStream.read()
            }
            cipherInputStream.close()

            val bytes = ByteArray(values.size)
            var i = 0
            while(i < values.size){
                bytes[i] = values[i]

                i++
            }

            retVal = String(bytes, Charset.defaultCharset())
        }

        return retVal
    }

    private fun setError(error: String){
        lastError = error
        Log.w(FINGER_PRINT_HELPER, lastError)
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    open class FingerPrintAuthenticationListener(var callback: Callback): FingerprintManager.AuthenticationCallback(){

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence){
            callback.onFailure("Authentication error [ $errorCode ] $errString")
        }

        /**
         * Called when a recoverable error has been encountered during authentication. The help
         * string is provided to give the user guidance for what went wrong, such as
         * "Sensor dirty, please clean it."
         * @param helpCode An integer identifying the error message
         * @param helpString A human-readable string that can be shown in UI
         */
        override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence){
            callback.onHelp(helpCode, helpString.toString())
        }

        /**
         * Called when a fingerprint is recognized.
         * @param result An object containing authentication-related data
         */
        override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult){
            super.onAuthenticationSucceeded(result)
        }

        /**
         * Called when a fingerprint is valid but not recognized.
         */
        override fun onAuthenticationFailed(){
            callback.onFailure("Authentication failed")
        }

        /*fun getCallback(): Callback {
            return callback
        }*/
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    inner class FingerPrintEncryptPasswordListener(callback: Callback, var password: String): FingerPrintAuthenticationListener(callback){

        override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult){
            val cipher: Cipher = result.cryptoObject.cipher

            try{
                if (encryptPassword(cipher, password)){
                    callback.onSuccess("Encrypted")
                }else{
                    callback.onFailure("Encryption failed")
                }
            }catch (e: Exception){
                callback.onFailure("Encryption failed ${e.message}")
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    inner class FingerPrintDecryptPasswordListener(callback: Callback): FingerPrintAuthenticationListener(callback){

        override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult){
            val cipher: Cipher = result.cryptoObject.cipher

            try{
                val savedPass: String? = decipher(cipher)
                if (savedPass != null){
                    callback.onSuccess(savedPass)
                }else{
                    callback.onFailure("Failed deciphering")
                }
            }catch (e: Exception){
                callback.onFailure("Deciphering failed ${e.message}")
            }
        }
    }
}
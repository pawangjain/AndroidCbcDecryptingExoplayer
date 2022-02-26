package ar.cryptotest.exoplayer2

import android.media.MediaCodecList
import android.media.MediaCodecInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import ar.cryptotest.exoplayer2.exoplayer2.R
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import kotlinx.coroutines.launch
import java.io.File
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class MainActivity : AppCompatActivity() {
    private var mCipher: Cipher? = null
    private val secretKeySpec: SecretKeySpec get() = SecretKeySpec(secretKey, AES_ALGORITHM)
    private val ivParameterSpec: IvParameterSpec? get() = IvParameterSpec(initialIv)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        try {
            mCipher = Cipher.getInstance(AES_TRANSFORMATION)
            mCipher!!.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //Used with 'onclick'
    @Suppress("UNUSED_PARAMETER")
    fun encryptVideo(view: View) {
        lifecycleScope.launch {
            val encryptionCipher = Cipher.getInstance(AES_TRANSFORMATION)
            encryptionCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
            encryptFile(encryptionCipher)
        }
    }

    private fun selectCodec(mimeType: String): MediaCodecInfo? {
        val numCodecs = MediaCodecList.getCodecCount()
        for (i in 0 until numCodecs) {
            val codecInfo: MediaCodecInfo = MediaCodecList.getCodecInfoAt(i)
            if (!codecInfo.isEncoder()) {
                continue
            }

            val types: Array<String> = codecInfo.getSupportedTypes()
            for (j in types.indices) {
                if (types[j].equals(mimeType, ignoreCase = true)) {
                    return codecInfo
                }
            }
        }
        return null
    }

    fun listCodecs(view: View) {
        getCodecForMimeType("video/mp4");
    }

    fun getCodecForMimeType(mimeType: String): MediaCodecInfo? {
        Log.d(TAG, "DADADADAD2 getCodecForMimeType.")
        val mediaCodecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
        val codecInfos = mediaCodecList.codecInfos
        for (i in codecInfos.indices) {
            val codecInfo = codecInfos[i]



            if (codecInfo.isEncoder) {
                continue
            }

//            Log.d(TAG, "DADADADAD " + codecInfo.name.toString())


            val types = codecInfo.supportedTypes

            if (types[0].contains("video", ignoreCase = true)) {
                Log.d(TAG, "DADADADAD " + codecInfo.name.toString() + "#" + types.joinToString("#").toString())

//                    return codecInfo
            }


            for (j in types.indices) {
                if (types[j].equals(mimeType, ignoreCase = true)) {
//                    return codecInfo
                }
            }
        }

        return null
    }

    fun playVideo(view: View) {
        val player = SimpleExoPlayer.Builder(view.context).build()
        findViewById<PlayerView>(R.id.simpleexoplayerview).player = player

        val uri = Uri.fromFile(encryptedFile)
        val dataSourceFactory: DataSource.Factory = BlockCipherEncryptedDataSourceFactory(
                secretKeySpec,
                uri,
                AES_TRANSFORMATION
        )
        try {
            val videoSource =
                ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
                        MediaItem.Builder().setUri(uri).build()
                )
            player.setMediaSource(videoSource)
            player.prepare()
            player.playWhenReady = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playLocalEncVideo(view: View) {
        val assetEncryptedFile = File(filesDir, "asset_encrypted.mp4")

        if (!assetEncryptedFile.exists()) {
            assetEncryptedFile.outputStream().use {
                assets.open("asset_encrypted.mp4").copyTo(it)
            }
        }

        Log.d(TAG, "DADADADAD successfully.")
//        return;

        val player = SimpleExoPlayer.Builder(view.context).build()
        findViewById<PlayerView>(R.id.simpleexoplayerview).player = player

        val uri = Uri.fromFile(assetEncryptedFile)
        val dataSourceFactory: DataSource.Factory = BlockCipherEncryptedDataSourceFactory(
                secretKeySpec,
                uri,
                AES_TRANSFORMATION
        )
        try {
            val videoSource =
                    ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
                            MediaItem.Builder().setUri(uri).build()
                    )
            player.setMediaSource(videoSource)
            player.prepare()
            player.playWhenReady = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        const val AES_ALGORITHM = "AES"
        const val AES_TRANSFORMATION = "AES/CBC/PKCS7Padding"

        //val secretKey = "TRFTXPN468F866J2".map { it.toByte() }.toByteArray()
        // Key is not a hex string; it an be any sting of 16 char len

        val secretKey = "85BE62F9AC34D107".map { it.toByte() }.toByteArray()
        var initialIv = "1234567890ABCDEF".map { it.toByte() }.toByteArray()
    }

    private val encryptedFile get() = File(filesDir, "encrypted.mp4")
//    private val assetEncryptedFile get() = File(filesDir, "asset_encrypted.mp4")


    private fun encryptFile(cipher: Cipher) {
        val unencryptedFile = File(filesDir, "unencrypted.mp4")

        if (!unencryptedFile.exists()) {
            unencryptedFile.outputStream().use {
                assets.open("oliver.mp4").copyTo(it)
            }
        }

        encryptedFile.delete()

        unencryptedFile.inputStream().use { unencryptedFileInputStream ->
            encryptedFile.outputStream().use { fileOutputStream ->
                fileOutputStream.write(initialIv)

                CipherOutputStream(fileOutputStream, cipher).use { cipherOutputStream ->
                    unencryptedFileInputStream.copyTo(cipherOutputStream)
                }
            }
        }

        Log.d(TAG, "File encrypted successfully.")
    }
}


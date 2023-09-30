package nl.koenhabets.yahtzeescore.dialog

import android.Manifest.permission.CAMERA
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.google.android.material.tabs.TabLayout
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import nl.koenhabets.yahtzeescore.R
import nl.koenhabets.yahtzeescore.databinding.AddPlayerDialogBinding
import java.util.*


class AddPlayerDialog(private var context: Context) {
    private lateinit var binding: AddPlayerDialogBinding
    private var listener: AddPlayerDialogListener? = null
    private var codeScanner: CodeScanner? = null
    private var mAlertDialog: AlertDialog? = null

    interface AddPlayerDialogListener {
        fun onAddPlayer(userId: String, pairCode: String)
        fun requestPermissions()
    }

    fun setAddPlayerDialogListener(listener: AddPlayerDialogListener) {
        this.listener = listener
    }

    fun showDialog(playerId: String, pairCode: String) {
        codeScanner = null
        val builder = AlertDialog.Builder(context)

        binding = AddPlayerDialogBinding.inflate(LayoutInflater.from(context))
        val view = binding.root

        binding.imageViewQrCode.setImageBitmap(encodeAsBitmap("$playerId;$pairCode"))

        binding.showCodeLayout.visibility = VISIBLE
        binding.scanCodeLayout.visibility = GONE

        builder.setView(view)

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(p0: TabLayout.Tab?) {
                if (p0?.position == 0) {
                    codeScanner?.releaseResources()
                    binding.showCodeLayout.visibility = VISIBLE
                    binding.scanCodeLayout.visibility = GONE
                } else if (p0?.position == 1) {
                    if (ContextCompat.checkSelfPermission(context, CAMERA)
                        == PackageManager.PERMISSION_DENIED
                    ) {
                        listener?.requestPermissions()
                    } else {
                        startCodeScanner()
                    }
                    binding.showCodeLayout.visibility = GONE
                    binding.scanCodeLayout.visibility = VISIBLE
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        builder.setNegativeButton(context.getString(R.string.close)) { _: DialogInterface?, _: Int -> }
        builder.setOnDismissListener {
            codeScanner?.releaseResources()
        }

        mAlertDialog = builder.create()
        mAlertDialog?.show()
    }

    fun startCodeScanner() {
        codeScanner?.releaseResources()
        if (codeScanner == null) {
            codeScanner = CodeScanner(context, binding.scannerView)
        }

        codeScanner?.decodeCallback = DecodeCallback {
            Log.i("AddPlayerDialog", "Scanned: ${it.text}")
            val split = it.text.split(";")
            if (split.size >= 2) {
                listener?.onAddPlayer(split[0], split[1])
            }
            mAlertDialog?.cancel()
        }

        codeScanner?.errorCallback = ErrorCallback {
            Log.e("AddPlayerDialog", "Error: ${it.message}")
        }

        codeScanner?.startPreview()
    }

    @Throws(WriterException::class)
    fun encodeAsBitmap(str: String): Bitmap {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(str, BarcodeFormat.QR_CODE, 500, 500)
        val w = bitMatrix.width
        val h = bitMatrix.height
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            for (x in 0 until w) {
                pixels[y * w + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
            }
        }
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
        return bitmap
    }

}
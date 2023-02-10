package nl.koenhabets.yahtzeescore.dialog

import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AlertDialog
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

    interface AddPlayerDialogListener {
        fun onAddPlayer(player: String)
    }

    fun setAddPlayerDialogListener(listener: AddPlayerDialogListener) {
        this.listener = listener
    }

    fun showDialog(playerId: String?, pairCode: String?) {
        val builder = AlertDialog.Builder(context)

        binding = AddPlayerDialogBinding.inflate(LayoutInflater.from(context))
        val view = binding.root

        if (playerId != null && pairCode != null) {
            binding.imageViewQrCode.setImageBitmap(encodeAsBitmap("$playerId;$pairCode"))
        }

        if (Date().time < 1678474613000) {
            binding.showCodeLayout.visibility = GONE
            binding.scanCodeLayout.visibility = VISIBLE
            binding.tabLayout.visibility = GONE
        } else {
            binding.showCodeLayout.visibility = VISIBLE
            binding.scanCodeLayout.visibility = GONE
        }

        builder.setView(view)

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(p0: TabLayout.Tab?) {
                if (p0?.position == 0) {
                    binding.showCodeLayout.visibility = VISIBLE
                    binding.scanCodeLayout.visibility = GONE
                } else if (p0?.position == 1) {
                    binding.showCodeLayout.visibility = GONE
                    binding.scanCodeLayout.visibility = VISIBLE
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        builder.setPositiveButton("Ok") { _: DialogInterface, _: Int ->
            //todo adding with username will be removed
            if (binding.editTextUsername.text.toString().trim() != "") {
                listener?.onAddPlayer(binding.editTextUsername.text.toString())
            }
        }
        builder.setNegativeButton(context.getString(R.string.close)) { dialog: DialogInterface?, id: Int -> }
        builder.show()
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
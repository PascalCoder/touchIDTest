package com.thepascal.touchidtest.biometric

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.thepascal.touchidtest.R
import kotlinx.android.synthetic.main.view_bottom_sheet.*

class BiometricDialogV23(context: Context): BottomSheetDialog(context), View.OnClickListener {

    private lateinit var biometricCallback: BiometricCallback

    private var mContext: Context

    init {
        mContext = context.applicationContext
        setDialogView()
    }

    constructor(context: Context, biometricCallback: BiometricCallback): this(context){
        mContext = context.applicationContext
        this.biometricCallback = biometricCallback
        setDialogView()
    }

    /*constructor(context: Context, theme: Int): this(context)

    constructor(context: Context, cancelable: Boolean,
                cancelListener: OnCancelListener): this(context)*/

    private fun setDialogView(){
        val bottomSheetView: View = layoutInflater.inflate(R.layout.view_bottom_sheet, null)
        setContentView(bottomSheetView)

        btnCancel.setOnClickListener(this)

        updateLogo()
    }

    fun setTitle(title: String){itemTitle.text = title}
    fun updateStatus(status: String){itemStatus.text = status}
    fun setSubtitle(subtitle: String){itemStatus.text = subtitle}
    fun setDescription(description: String){itemDescription.text = description}
    fun setButtonText(negativeButtonText: String){btnCancel.text = negativeButtonText}

    private fun updateLogo(){
        val drawable: Drawable = context.packageManager.getApplicationIcon(context.packageName)
        imgLogo.setImageDrawable(drawable)
    }

    override fun onClick(v: View?) {
        dismiss()
        biometricCallback.onAuthenticationCancelled()
    }
}
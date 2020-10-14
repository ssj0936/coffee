package com.timothy.coffee.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.timothy.coffee.R
import com.timothy.coffee.util.Utils
import kotlinx.android.synthetic.main.fragment_about_dialog_layout.*

class AboutFragment: DialogFragment(),View.OnClickListener {

    companion object {
        @JvmStatic
        private lateinit var INSTANCE:AboutFragment
        fun getInstance(): AboutFragment {
            if(!::INSTANCE.isInitialized) INSTANCE = AboutFragment()
            return INSTANCE
        }

        val TAG = "AboutFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_about_dialog_layout, container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_facebook.setOnClickListener(this)
        btn_github.setOnClickListener(this)
        btn_ig.setOnClickListener(this)
        btn_linkedin.setOnClickListener(this)
        btn_cafenomad.setOnClickListener(this)
    }

    //size of dialog
    override fun onStart() {
        super.onStart()

        dialog?.let{
            it.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            it.window?.setBackgroundDrawableResource(R.drawable.background_filter_dialog)
        }
    }

    override fun onClick(v: View) {
        startActivity(Utils.getURLIntent(
            when(v){
                btn_facebook -> getString(R.string.url_facebook)
                btn_github -> getString(R.string.url_github)
                btn_ig -> getString(R.string.url_ig)
                btn_linkedin -> getString(R.string.url_linkedin)
                btn_cafenomad -> getString(R.string.url_cafenomad_api)
                else -> getString(R.string.url_cafenomad_api)
            }
        ))
    }
}
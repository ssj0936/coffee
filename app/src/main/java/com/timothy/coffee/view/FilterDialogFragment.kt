package com.timothy.coffee.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.timothy.coffee.R
import com.timothy.coffee.util.*
import com.timothy.coffee.util.Utils.Companion.getFilterSetting
import com.timothy.coffee.util.Utils.Companion.setFilterSetting
import com.timothy.coffee.viewmodel.MainViewModel
import com.timothy.coffee.viewmodel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_filter_dialog_layout.*
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.pow

class FilterDialogFragment:DialogFragment(),View.OnClickListener {
    @Inject
    lateinit var mViewModelFactory: ViewModelFactory
    private lateinit var mMainViewModel: MainViewModel

    private lateinit var pressedColorStateList: ColorStateList
    private lateinit var unpressedColorStateList: ColorStateList
    private val mListNumberView = mutableListOf<Pair<Int,Int>>()

    companion object{

    }
    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainViewModel = activity?.run {
            ViewModelProvider(this,mViewModelFactory).get(MainViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_filter_dialog_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        initButtonState()
        tasty_rate_0.setOnClickListener(this)
        tasty_rate_1.setOnClickListener(this)
        tasty_rate_2.setOnClickListener(this)
        tasty_rate_3.setOnClickListener(this)
        tasty_rate_4.setOnClickListener(this)
        tasty_rate_5.setOnClickListener(this)
        environment_option_time_limit.setOnClickListener(this)
        environment_option_socket.setOnClickListener(this)
        environment_option_standing_desk.setOnClickListener(this)
        confirm_button.setOnClickListener(this)
        favorite_show_favorite.setOnClickListener(this)
    }

    //size of dialog
    override fun onStart() {
        super.onStart()

        dialog?.let{
            it.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            it.window?.setBackgroundDrawableResource(R.drawable.background_filter_dialog)
        }
    }

    private fun init(){
        pressedColorStateList = resources.getColorStateList(R.color.rate_orange,null)
        unpressedColorStateList = resources.getColorStateList(android.R.color.transparent,null)

        mListNumberView.add(Pair(2.0.pow(FILTER_TASTY_RATE_0).toInt(),R.id.tasty_rate_0))
        mListNumberView.add(Pair(2.0.pow(FILTER_TASTY_RATE_1).toInt(),R.id.tasty_rate_1))
        mListNumberView.add(Pair(2.0.pow(FILTER_TASTY_RATE_2).toInt(),R.id.tasty_rate_2))
        mListNumberView.add(Pair(2.0.pow(FILTER_TASTY_RATE_3).toInt(),R.id.tasty_rate_3))
        mListNumberView.add(Pair(2.0.pow(FILTER_TASTY_RATE_4).toInt(),R.id.tasty_rate_4))
        mListNumberView.add(Pair(2.0.pow(FILTER_TASTY_RATE_5).toInt(),R.id.tasty_rate_5))
        mListNumberView.add(Pair(2.0.pow(FILTER_NO_TIME_LIMIT).toInt(),R.id.environment_option_time_limit))
        mListNumberView.add(Pair(2.0.pow(FILTER_SOCKET).toInt(),R.id.environment_option_socket))
        mListNumberView.add(Pair(2.0.pow(FILTER_STANDING_DESK).toInt(),R.id.environment_option_standing_desk))
    }

    private fun initButtonState(){
        val filterSetting = getFilterSetting(requireContext())
        var tmp = filterSetting

        //favorite btn
        val isFavoriteOnly = mMainViewModel.isFavoriteOnly.value!!
        favorite_show_favorite.tag = isFavoriteOnly
        setButtonPressed(favorite_show_favorite,isFavoriteOnly)

        //filter
        mListNumberView.stream().sorted { o1, o2 ->
            o2.first - o1.first
        }.forEach {
//            Timber.d("$it")

            val filter = it.first
            val viewId = it.second
            val button = view?.findViewById<MaterialButton>(viewId)
//            Timber.d("filter:$filter, $tmp%$filter=$result")

            //pressed
            if(tmp/filter>0){
                button?.tag = true
                setButtonPressed(view?.findViewById<MaterialButton>(viewId),true)
            }
            //unpressed
            else{
                button?.tag = false
                setButtonPressed(view?.findViewById<MaterialButton>(viewId),false)
            }
            tmp %= filter
//            Timber.d("--------------------")
        }
    }

    @SuppressLint("CheckResult")
    override fun onClick(v: View?) {

        if(v == confirm_button){
            val newFilterSetting = getTempFilterSetting()
            setFilterSetting(newFilterSetting)

            mMainViewModel.getCafeList(isFetchFavOnly = mMainViewModel.isFavoriteOnly.value!!)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe ({
                    Timber.d("mMainViewModel.isFavoriteOnly: ${mMainViewModel.isFavoriteOnly.value}")
                    mMainViewModel.initialLocalCafeData(it)
                    dismiss()
                },{error->
                    Timber.e("filter settings error: $error")
                    mMainViewModel.isLoading.postValue(false)
                    dismiss()
                })
            return
        }else if(v is MaterialButton){
            val result = !(v.tag as Boolean)
            v.tag = result
            setButtonPressed(v,result)
        }
    }

    private fun setButtonPressed(view:MaterialButton?, pressed: Boolean){
        if(pressed) {
            view?.backgroundTintList =
                resources.getColorStateList(R.color.color_2, null)
            view?.setTextColor(resources.getColor(R.color.white,null))
        }else {
            view?.backgroundTintList =
                resources.getColorStateList(android.R.color.transparent, null)
            view?.setTextColor(resources.getColor(R.color.common_primary_context_text_color,null))
        }
    }

    private fun getTempFilterSetting():Int{
        return mListNumberView.stream().sorted { o1, o2 ->
            o2.first - o1.first
        }.filter {
            val button = view?.findViewById<MaterialButton>(it.second)
            button?.tag as Boolean
        }.mapToInt { i ->
            i.first
        }.sum()
    }

    private fun setFilterSetting(filter:Int){
        setFilterSetting(requireContext(),filter)

        //favorite filter
        mMainViewModel.isFavoriteOnly.value = favorite_show_favorite.tag as Boolean
    }
}
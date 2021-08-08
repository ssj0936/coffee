package com.timothy.coffee.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.timothy.coffee.R
import com.timothy.coffee.databinding.FragmentCafeInfoV2Binding
import com.timothy.coffee.ui.CafeInfoRecyclerViewAdapter
import com.timothy.coffee.util.Utils
import com.timothy.coffee.viewmodel.MainViewModel
import com.timothy.coffee.viewmodel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber
import javax.inject.Inject


class CafeInfoV2Fragment: Fragment() ,View.OnClickListener{
    private lateinit var mMainViewModel: MainViewModel
    @Inject
    lateinit var mViewModelFactory: ViewModelFactory
    var adapter:CafeInfoRecyclerViewAdapter = CafeInfoRecyclerViewAdapter()
    lateinit var binding:FragmentCafeInfoV2Binding
    private var ARGUMENT_DATA_CAFE_INDEX:Int = 0

    companion object {
        val ARGUMENT_KEY = "CAFE_DATA"

        @JvmStatic
        fun newInstance(cafeIndex: Int):CafeInfoV2Fragment = CafeInfoV2Fragment().apply {
            arguments = Bundle().apply {
                putInt(ARGUMENT_KEY,cafeIndex)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainViewModel = activity?.run {
            ViewModelProviders.of(this,mViewModelFactory).get(MainViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        ARGUMENT_DATA_CAFE_INDEX = arguments?.getInt(ARGUMENT_KEY,0) ?: 0

        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCafeInfoV2Binding.inflate(inflater,container,false)
        return binding.root
    }

    @SuppressLint("BinaryOperationInTimber")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width: Int = displayMetrics.widthPixels

        binding.cafeName.maxWidth =
            width - (resources.getDimensionPixelSize(R.dimen.cafeinfo_padding_side)*2
                    + resources.getDimensionPixelSize(R.dimen.cafeinfo_v2_card_padding)*2
                    + resources.getDimensionPixelSize(R.dimen.cafeinfo_favorite_btn_margin_side)*2
                    + resources.getDimensionPixelSize(R.dimen.cafeinfo_favorite_btn_size))

        binding.btnNavigate.setOnClickListener(this)
        binding.btnCafenomadIntro.setOnClickListener(this)
        binding.btnOfficial.setOnClickListener(this)
        binding.favoriteBtn.setOnClickListener(this)
        binding.cafeInfoRecyclerview.adapter = adapter
        val mgr = GridLayoutManager(context,2)
        binding.cafeInfoRecyclerview.layoutManager = mgr

        mMainViewModel.cafeListDisplay.value?.let {
            val cafe = it[ARGUMENT_DATA_CAFE_INDEX]

            adapter.setCafe(cafe, requireActivity())
            adapter.notifyDataSetChanged()

            binding.contentTimeLimit.text = when (cafe.cafenomad.isTimeLimited) {
                getString(R.string.info_value_yes) -> getString(R.string.info_time_limit_text_yes)
                getString(R.string.info_value_maybe) -> getString(R.string.info_time_limit_text_maybe)
                getString(R.string.info_value_no) -> getString(R.string.info_time_limit_text_no)
                else -> getString(R.string.no_data)
            }

            binding.contentSocketProvide.text = when (cafe.cafenomad.isSocketProvided) {
                getString(R.string.info_value_yes) -> getString(R.string.info_socket_provided_text_yes)
                getString(R.string.info_value_maybe) -> getString(R.string.info_socket_provided_text_maybe)
                getString(R.string.info_value_no) -> getString(R.string.info_socket_provided_text_no)
                else -> getString(R.string.no_data)
            }

            binding.contentStandingDesk.text = when (cafe.cafenomad.isStandingDeskAvailable) {
                getString(R.string.info_value_yes) -> getString(R.string.info_standing_desk_text_yes)
                getString(R.string.info_value_no) -> getString(R.string.info_standing_desk_text_no)
                else -> getString(R.string.no_data)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mMainViewModel.cafeListDisplay.value?.let {
            binding.cafe = it[ARGUMENT_DATA_CAFE_INDEX]
            binding.lifecycleOwner = viewLifecycleOwner
        }
    }

    override fun onClick(v: View?) {
        val cafe = mMainViewModel.cafeListDisplay.value?.get(ARGUMENT_DATA_CAFE_INDEX)

        if(cafe != null){
            when(v) {
                binding.btnNavigate -> {
                    if (mMainViewModel.screenCenterLoc.value != null && mMainViewModel.chosenCafe.value != null) {
                        val intent = Utils.getGoogleMapDirectionIntent(
                            mMainViewModel.screenCenterLoc.value!!.latitude,
                            mMainViewModel.screenCenterLoc.value!!.longitude,
                            "${mMainViewModel.chosenCafe.value!!.cafenomad.name} ${getString(R.string.postfix_navigation_keyword)}"
                        )
                        startActivity(intent)
                    }
                }

                binding.btnCafenomadIntro -> {
                    mMainViewModel.chosenCafe.value?.let {
                        startActivity(Utils.getCafenomadURLIntent(it.cafenomad.id))
                    }
                }

                binding.btnOfficial -> {
                    mMainViewModel.chosenCafe.value?.let {
                        startActivity(Utils.getURLIntent(it.cafenomad.url))
                    }
                }

                binding.favoriteBtn -> {
                    mMainViewModel.chosenCafe.value?.let {
                        if (it.isFavorite)
                            mMainViewModel.deleteFavorite(it.cafenomad.id)
                                .subscribe({
//                                    setFavoriteBtn(false)
                                    Timber.d("delete favorite success")
                                },{error ->
                                    Timber.d("delete favorite fail: $error")
                                    error.printStackTrace()
                                })
                        else
                            mMainViewModel.setFavorite(it.cafenomad.id)
                                .subscribe({
//                                    setFavoriteBtn(true)
                                    Timber.d("adding favorite success")
                                },{error ->
                                    Timber.d("adding favorite fail: $error")
                                    error.printStackTrace()
                                })
                    }
                }
            }
        }
    }

    fun scrollToTop(){
        binding.nestedScrollView.scrollY = 0
    }
}

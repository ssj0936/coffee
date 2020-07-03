package com.timothy.coffee.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.timothy.coffee.R
import com.timothy.coffee.data.model.CafenomadDisplay
import com.timothy.coffee.databinding.FragmentCafeInfoBinding
import com.timothy.coffee.ui.CafeInfoRecyclerViewAdapter
import com.timothy.coffee.util.Utils
import com.timothy.coffee.viewmodel.MainViewModel
import com.timothy.coffee.viewmodel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber
import javax.inject.Inject


class CafeInfoFragment: Fragment(),CafeBaseFragment ,View.OnClickListener{
    lateinit var binding:FragmentCafeInfoBinding
    private lateinit var mMainViewModel: MainViewModel
    @Inject
    lateinit var mViewModelFactory: ViewModelFactory
    var adapter:CafeInfoRecyclerViewAdapter = CafeInfoRecyclerViewAdapter()

    companion object {
        @JvmStatic
        private lateinit var INSTANCE:CafeInfoFragment
        fun getInstance():CafeInfoFragment{
            if(!::INSTANCE.isInitialized){
                INSTANCE = CafeInfoFragment()
            }
            return INSTANCE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainViewModel = activity?.run {
            ViewModelProviders.of(this,mViewModelFactory).get(MainViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCafeInfoBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val anchorOffset = resources.getDimensionPixelOffset(R.dimen.bottom_sheet_anchor_offset)
        view.setPadding(0, 0, 0, anchorOffset)

        binding.btnNavigate.setOnClickListener(this)
        binding.btnCafenomadIntro.setOnClickListener(this)
        binding.btnOfficial.setOnClickListener(this)
        binding.favoriteBtn.setOnClickListener(this)
        binding.cafeInfoRecyclerview.adapter = adapter

        val mgr = GridLayoutManager(context,2)
        binding.cafeInfoRecyclerview.layoutManager = mgr
        mMainViewModel.chosenCafe.observe(viewLifecycleOwner,
            Observer<CafenomadDisplay>{
                adapter.setCafe(it, requireActivity())
                adapter.notifyDataSetChanged()

                binding.contentTimeLimit.text = when (it.cafenomad.isTimeLimited) {
                    getString(R.string.info_value_yes) -> getString(R.string.info_time_limit_text_yes)
                    getString(R.string.info_value_maybe) -> getString(R.string.info_time_limit_text_maybe)
                    getString(R.string.info_value_no) -> getString(R.string.info_time_limit_text_no)
                    else -> getString(R.string.no_data)
                }

                binding.contentSocketProvide.text = when (it.cafenomad.isSocketProvided) {
                    getString(R.string.info_value_yes) -> getString(R.string.info_socket_provided_text_yes)
                    getString(R.string.info_value_maybe) -> getString(R.string.info_socket_provided_text_maybe)
                    getString(R.string.info_value_no) -> getString(R.string.info_socket_provided_text_no)
                    else -> getString(R.string.no_data)
                }

                binding.contentStandingDesk.text = when (it.cafenomad.isStandingDeskAvailable) {
                    getString(R.string.info_value_yes) -> getString(R.string.info_standing_desk_text_yes)
                    getString(R.string.info_value_no) -> getString(R.string.info_standing_desk_text_no)
                    else -> getString(R.string.no_data)
                }
            })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.viewmodel = mMainViewModel
        binding.lifecycleOwner = this

    }

    override fun onClick(v: View?) {
        when(v) {
            binding.btnNavigate -> {
                if (mMainViewModel.loc.value != null && mMainViewModel.chosenCafe.value != null) {
                    val intent = Utils.getGoogleMapDirectionIntent(
                        mMainViewModel.loc.value!!.latitude,
                        mMainViewModel.loc.value!!.longitude,
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
                     else
                        mMainViewModel.setFavorite(it.cafenomad.id)
                }
            }
        }
    }

    override fun setNestScrollingEnable(enable:Boolean){
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
            binding.nestedScrollView.isSmoothScrollingEnabled = enable
        }
    }
}

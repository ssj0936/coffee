package com.timothy.coffee.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.timothy.coffee.R
import com.timothy.coffee.ui.VerticalRecyclerviewDecoration
import com.timothy.coffee.viewmodel.MainViewModel
import com.timothy.coffee.viewmodel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_sort_dialog_layout.view.*
import javax.inject.Inject

class SortDialogFragment: DialogFragment(){

    @Inject
    lateinit var mViewModelFactory: ViewModelFactory
    private lateinit var mMainViewModel: MainViewModel

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainViewModel = activity?.run {
            ViewModelProviders.of(this,mViewModelFactory).get(MainViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sort_dialog_layout,container,false)

        val adapter = SortingListAdapter(
            resources.getStringArray(R.array.sort_item_display).toList(),
            resources.getStringArray(R.array.sort_item).toList()
        )

        val mListener = object :SortingListAdapter.OnSortItemSelectListener{
            override fun onSortItemClick(mSortType: String) {
//                mMainViewModel.sortType.value = mSortType
                dismiss()
            }
        }
        adapter.setListener(mListener)
        view.recycler_view.adapter = adapter
        view.recycler_view.addItemDecoration(
            VerticalRecyclerviewDecoration(requireContext(),
                LinearLayoutManager.VERTICAL,
                isDrawLastDivider = false,
                isDrawFirstDivider = false)
        )

        return view
    }

    //size of dialog
    override fun onStart() {
        super.onStart()

        dialog?.let{
            it.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    class SortingListAdapter(
        private val mSortTypeDisplay:List<String>,
        private val mSortType:List<String>

    ): RecyclerView.Adapter<SortingListAdapter.ViewHolder>() {

        lateinit var mListener:OnSortItemSelectListener

        class ViewHolder(val rootView:View):RecyclerView.ViewHolder(rootView){
            val mTextView:TextView = rootView.findViewById(R.id.title)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val root = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.sort_dialog_recyclerview_item_layout,parent,false)
            return ViewHolder(root)
        }

        override fun getItemCount(): Int = mSortTypeDisplay.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val sortTypeDisplay = mSortTypeDisplay[position]
            val sortType = mSortType[position]

            holder.mTextView.text = sortTypeDisplay
            holder.rootView.setOnClickListener{
                mListener.onSortItemClick(sortType)
            }
        }

        fun setListener(listener: OnSortItemSelectListener){
            mListener = listener
        }

        interface OnSortItemSelectListener{
            fun onSortItemClick(mSortType:String)
        }
    }
}

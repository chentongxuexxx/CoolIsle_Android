package com.huandao.app.ui.insights

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.huandao.app.databinding.FragmentInsightsBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * 消费洞察页（P1 占位）。
 *
 * V1.0 展示「即将上线」温暖占位文案。
 * V2 将实现周报摘要、月度报告、情绪-消费图谱。
 */
@AndroidEntryPoint
class InsightsFragment : Fragment() {

    private var _binding: FragmentInsightsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentInsightsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

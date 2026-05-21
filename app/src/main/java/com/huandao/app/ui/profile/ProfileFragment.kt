package com.huandao.app.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.huandao.app.R
import com.huandao.app.databinding.FragmentProfileBinding
import com.huandao.app.ui.pool.CoolPoolViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * 个人中心页（P1 占位 + 数据导出 P0）。
 *
 * V1.0 提供数据导出功能（隐私合规）和「更多功能即将上线」占位。
 * V2 将扩展偏好设置、成就墙、提醒管理等功能。
 */
@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupExportButton()
        observeViewModel()
    }

    /**
     * 数据导出按钮：将冷静池数据导出为 JSON 文件并通过系统分享发送。
     */
    private fun setupExportButton() {
        binding.btnExportData.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val json = viewModel.exportAllDataAsJson()
                    val file = saveJsonToCache(json)
                    shareFile(file)
                    Toast.makeText(requireContext(), R.string.profile_export_success, Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), R.string.profile_export_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.coolingCount.collect { count ->
                binding.tvCoolingCount.text = "$count"
                binding.tvCoolingCountLabel.isVisible = true
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.decidedCount.collect { count ->
                binding.tvDecidedCount.text = "$count"
                binding.tvDecidedCountLabel.isVisible = true
            }
        }
    }

    /**
     * 将 JSON 字符串保存到缓存目录。
     */
    private fun saveJsonToCache(json: String): File {
        val fileName = "huandao_export_${System.currentTimeMillis()}.json"
        val file = File(requireContext().cacheDir, fileName)
        file.writeText(json)
        return file
    }

    /**
     * 通过系统分享发送文件。
     */
    private fun shareFile(file: File) {
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file,
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.profile_export_data)))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

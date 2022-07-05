package karaokeit.song.split.presentation.upload

import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.*
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import karaokeit.song.split.R
import karaokeit.song.split.databinding.FragmentUploadBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class UploadFragment : Fragment() {

    lateinit var binding: FragmentUploadBinding
    val viewModel by viewModels<UploadViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_upload, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        attachObserver()
        if (!viewModel.isFakeDownloadStarted) {
            viewModel.isFakeDownloadStarted = true
            startFakeCounter()
        }

    }

    private fun startFakeCounter() {
        viewModel.startTestCounter {
            lifecycleScope.launch {
                viewModel.setUIState(UploadViewModel.UISTATE.SEPARATION)
                delay(5000)
                viewModel.setUIState(UploadViewModel.UISTATE.SUCCESS)
                //gotoPlayerFragment()
            }
        }
    }

    private fun attachObserver() {

        viewModel.ui_state.observe(viewLifecycleOwner) {
            when (it) {
                UploadViewModel.UISTATE.UPLOADING -> {
                    setBgColor(BG.DEFAULT)
                    animateCircle()
                }
                UploadViewModel.UISTATE.SEPARATION -> {
                    showSeparationUI()
                }
                UploadViewModel.UISTATE.FAILED -> {
                    showErrorMessage()
                }
                UploadViewModel.UISTATE.SUCCESS -> {
                    showSuccessUI()
                }
                else->{
                    gotoPlayerFragment()
                }
            }
        }

        viewModel.currentProgress.observe(viewLifecycleOwner) {
            binding.tvCounter.text = "$it%"
        }
    }

    private fun showSuccessUI() {
        viewModel.setUIState(UploadViewModel.UISTATE.SUCCESS)
        viewModel.cancelAllAnimators()
        binding.uploadAnimLottie.apply {
            setAnimation(R.raw.success)
            playAnimation()
            repeatCount = 0
        }

        binding.apply {
            labelFileUpload.text = getString(R.string.upload_success)

            tvCounter.isVisible = false
            btnDone.isVisible = true
        }
        setBgColor(BG.GREEN)
    }

    private fun setBgColor(color: BG) {
        val transitionDrawable = (binding.rootViewCl.background as TransitionDrawable)
        when (color) {
            BG.DEFAULT -> {
                binding.rootViewCl.background =
                    context?.getDrawable(R.drawable.upload_color_transition)
            }
            BG.RED -> {
                transitionDrawable.startTransition(1000)
            }
            else->{
                binding.rootViewCl.background =
                    context?.getDrawable(R.drawable.upload_color_transition)
            }
        }
    }

    private fun animateCircle(isExpand: Boolean = true) {
        viewModel.cancelCircleAnimator()
        val value = if (isExpand) 1.2f else 1f
        binding.circleView.animate()?.apply {
            scaleX(value)
            scaleY(value)
            interpolator = AnticipateOvershootInterpolator()
            duration = 1500
            withEndAction {
                animateCircle(!isExpand)
            }
            viewModel.setNewCircleAnimator(this)
            start()
        }
    }


    private fun showErrorMessage() {
        viewModel.setUIState(UploadViewModel.UISTATE.FAILED)
        viewModel.cancelAllAnimators()
        binding.apply {
            labelFileUpload.text = getString(R.string.upload_failed)
            uploadAnimLottie.pauseAnimation()
            tvCounter.isVisible = false
            btnTryAgain.isVisible = true
            btnTryAgain.setOnClickListener {
                tryAgain()
            }
        }
        setBgColor(BG.RED)
    }

    private fun tryAgain() {
        viewModel.setUIState(UploadViewModel.UISTATE.UPLOADING)
        binding.apply {
            labelFileUpload.text = getString(R.string.uploading_file)
            uploadAnimLottie.playAnimation()
            btnTryAgain.isVisible = false
            tvCounter.isVisible = true
        }
        viewModel.resetCurrentProgress()
        startFakeCounter()
    }

    private fun gotoPlayerFragment() {
        val action = UploadFragmentDirections.actionUploadFragmentToPlayerFragment()
        view?.findNavController()?.navigate(action)
    }

    private fun showSeparationUI() {
        viewModel.setUIState(UploadViewModel.UISTATE.SEPARATION)
        binding.tvCounter.isVisible = false
        viewModel.startSepeartionAimator {
            binding.labelFileUpload.text = getString(R.string.separating_audio) + it
        }
    }

    enum class BG { DEFAULT, GREEN, RED }
}
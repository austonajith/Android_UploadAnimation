package karaokeit.song.split.presentation.upload

import android.animation.ValueAnimator
import android.view.ViewPropertyAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import androidx.core.animation.doOnRepeat
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import karaokeit.song.split.R
import karaokeit.song.split.domain.KaraokeItUseCase
import karaokeit.song.split.extensions.notifyOnlyIfChanged
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(var karaokeItUseCase: KaraokeItUseCase) : ViewModel() {

    val ui_state = MutableLiveData(UISTATE.UPLOADING)
    private var progressAnimator: ValueAnimator? = null
    private var seperationAnimator: ValueAnimator? = null
    var circleAnimator: ViewPropertyAnimator? = null
    var currentProgress = MutableLiveData(0)
    var isFakeDownloadStarted = false

    fun setUploadProgress(newProgress: Int) {
        progressAnimator?.removeAllUpdateListeners()

        progressAnimator = ValueAnimator.ofInt(currentProgress.value ?: 0, newProgress).apply {
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                (it.animatedValue as Int).apply {
                    currentProgress.value = this
                }
            }
        }
        progressAnimator?.start()
    }

    fun cancelAllAnimators() {
        cancelSeperationAnimator()
        cancelCircleAnimator()
    }

    fun resetCurrentProgress() {
        currentProgress.value = 0
    }

    fun cancelSeperationAnimator() {
        seperationAnimator?.cancel()
        seperationAnimator?.removeAllUpdateListeners()
    }

    fun startSepeartionAimator(result: (animatedValue: String) -> Unit) {
        cancelSeperationAnimator()
        var dots = ""
        seperationAnimator = ValueAnimator.ofInt(0, 4).apply {
            duration = 1500
            interpolator = AccelerateInterpolator()
            addUpdateListener {
                dots = when (it.animatedValue) {
                    0 -> ""
                    1 -> "."
                    2 -> ".."
                    3 -> "..."
                    else -> "..."
                }
                result(dots)
            }
            repeatCount = ValueAnimator.INFINITE
            doOnRepeat {
                dots = ""
            }
            start()
        }
    }

    fun setNewCircleAnimator(viewPropertyAnimator: ViewPropertyAnimator) {
        circleAnimator = viewPropertyAnimator
    }


    fun startTestCounter(onComplete: () -> Unit) {
        val fakeProgressList = arrayOf(10, 15, 27, 39, 42, 64, 82, 96, 100)
        viewModelScope.launch {
            fakeProgressList.forEach {
                delay(1000)
                setUploadProgress(it)
            }
            onComplete()
        }
    }

    fun setUIState(state: UISTATE) {
        ui_state.notifyOnlyIfChanged(state)
    }

    fun cancelCircleAnimator() {
        circleAnimator?.cancel()
        circleAnimator?.setUpdateListener(null)
    }

    enum class UISTATE {
        UPLOADING,
        SEPARATION,
        FAILED,
        SUCCESS,
    }

}
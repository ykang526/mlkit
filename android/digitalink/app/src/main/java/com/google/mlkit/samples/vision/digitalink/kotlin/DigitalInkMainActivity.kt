package com.google.mlkit.samples.vision.digitalink.kotlin

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.annotation.VisibleForTesting
import androidx.core.view.isVisible
import com.google.android.gms.tasks.Tasks
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSortedSet
import com.google.mlkit.samples.vision.digitalink.R
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier
import kotlinx.android.synthetic.main.activity_digital_ink_main_kotlin.*
import java.util.Locale

/** Main activity which creates a StrokeManager and connects it to the DrawingView. */
class DigitalInkMainActivity : AppCompatActivity(), StrokeManager.ContentChangedListener {
  @JvmField @VisibleForTesting val strokeManager = StrokeManager()
  private val problemManager = ProblemManager()
  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_digital_ink_main_kotlin)
    val drawingView = findViewById<DrawingView>(R.id.drawing_view)
    val statusTextView = findViewById<StatusTextView>(R.id.status_text_view)
    val problemTextView = findViewById<TextView>(R.id.problemText)
    problemManager.onCreate(applicationContext)
    drawingView.setStrokeManager(strokeManager)
    statusTextView.setStrokeManager(strokeManager)
    strokeManager.setStatusChangedListener(statusTextView)
    strokeManager.setContentChangedListener(drawingView)
    strokeManager.setContentChangedListener(this)
    strokeManager.setClearCurrentInkAfterRecognition(true)
    strokeManager.setTriggerRecognitionAfterInput(false)
    strokeManager.refreshDownloadedModelsStatus()
    problemTextView.text = problemManager.currP.roumaji

    strokeManager.setActiveModel("ja")
    strokeManager.download()
    strokeManager.reset()
  }
  fun nextClick(v:View?){
    problemManager.nextQuestion()
    problemText.text = problemManager.currP.roumaji
  }
  fun downloadClick(v: View?) {
    strokeManager.download()
  }

  @SuppressLint("SetTextI18n")
  fun recognizeClick(v: View?) {
    strokeManager.recognize()
  }

  fun clearClick(v: View?) {
    strokeManager.reset()
    val drawingView = findViewById<DrawingView>(R.id.drawing_view)
    drawingView.clear()
  }

  fun deleteClick(v: View?) {
    strokeManager.deleteActiveModel()
  }

  override fun onContentChanged() {
    super.onContentChanged()
    val userContent = strokeManager.getContent()
    if (userContent.isEmpty()) return
    val alphabet = userContent.last().text ?: return
    if (problemManager.compareAnswer(alphabet)) {
      resultText.text = "Correct!"
    } else {
      resultText.text = "Wrong"
    }
  }

  private class ModelLanguageContainer
  private constructor(private val label: String, val languageTag: String?) :
    Comparable<ModelLanguageContainer> {

    var downloaded: Boolean = false

    override fun toString(): String {
      return when {
        languageTag == null -> label
        downloaded -> "   [D] $label"
        else -> "   $label"
      }
    }

    override fun compareTo(other: ModelLanguageContainer): Int {
      return label.compareTo(other.label)
    }

    companion object {
      /** Populates and returns a real model identifier, with label and language tag. */
      fun createModelContainer(label: String, languageTag: String?): ModelLanguageContainer {
        // Offset the actual language labels for better readability
        return ModelLanguageContainer(label, languageTag)
      }

      /** Populates and returns a label only, without a language tag. */
      fun createLabelOnly(label: String): ModelLanguageContainer {
        return ModelLanguageContainer(label, null)
      }
    }
  }

  private fun populateLanguageAdapter(): ArrayAdapter<ModelLanguageContainer> {
    val languageAdapter =
      ArrayAdapter<ModelLanguageContainer>(this, android.R.layout.simple_spinner_item)
    languageAdapter.add(ModelLanguageContainer.createLabelOnly("Select language"))
    languageAdapter.add(ModelLanguageContainer.createLabelOnly("Non-text Models"))

    // Manually add non-text models first
    for (languageTag in NON_TEXT_MODELS.keys) {
      languageAdapter.add(
        ModelLanguageContainer.createModelContainer(NON_TEXT_MODELS[languageTag]!!, languageTag)
      )
    }
    languageAdapter.add(ModelLanguageContainer.createLabelOnly("Text Models"))
    val textModels = ImmutableSortedSet.naturalOrder<ModelLanguageContainer>()
    for (modelIdentifier in DigitalInkRecognitionModelIdentifier.allModelIdentifiers()) {
      if (NON_TEXT_MODELS.containsKey(modelIdentifier.languageTag)) {
        continue
      }
      if (modelIdentifier.languageTag.endsWith(Companion.GESTURE_EXTENSION)) {
        continue
      }
      textModels.add(buildModelContainer(modelIdentifier, "Script"))
    }
    languageAdapter.addAll(textModels.build())
    languageAdapter.add(ModelLanguageContainer.createLabelOnly("Gesture Models"))
    val gestureModels = ImmutableSortedSet.naturalOrder<ModelLanguageContainer>()
    for (modelIdentifier in DigitalInkRecognitionModelIdentifier.allModelIdentifiers()) {
      if (!modelIdentifier.languageTag.endsWith(Companion.GESTURE_EXTENSION)) {
        continue
      }
      gestureModels.add(buildModelContainer(modelIdentifier, "Script gesture classifier"))
    }
    languageAdapter.addAll(gestureModels.build())
    return languageAdapter
  }

  private fun buildModelContainer(
    modelIdentifier: DigitalInkRecognitionModelIdentifier,
    labelSuffix: String
  ): ModelLanguageContainer {
    val label = StringBuilder()
    label.append(Locale(modelIdentifier.languageSubtag).displayName)
    if (modelIdentifier.regionSubtag != null) {
      label.append(" (").append(modelIdentifier.regionSubtag).append(")")
    }
    if (modelIdentifier.scriptSubtag != null) {
      label.append(", ").append(modelIdentifier.scriptSubtag).append(" ").append(labelSuffix)
    }
    return ModelLanguageContainer.createModelContainer(
      label.toString(),
      modelIdentifier.languageTag
    )
  }

  companion object {
    private const val TAG = "MLKDI.Activity"
    private val NON_TEXT_MODELS =
      ImmutableMap.of(
        "zxx-Zsym-x-autodraw",
        "Autodraw",
        "zxx-Zsye-x-emoji",
        "Emoji",
        "zxx-Zsym-x-shapes",
        "Shapes"
      )
    private const val GESTURE_EXTENSION = "-x-gesture"
  }
}

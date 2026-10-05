package com.nndwn.runtext.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.nndwn.runtext.data.model.FontData
import com.nndwn.runtext.data.model.TextColorType
import com.nndwn.runtext.data.model.TextConfig
import com.nndwn.runtext.domain.runtext.RunningTextLayoutCalculator
import com.nndwn.runtext.ui.LocalSizeHeight
import com.nndwn.runtext.ui.theme.toComposeColor
import com.nndwn.runtext.ui.utils.rememberFontFamily
import java.text.Bidi
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.isActive

@Composable
fun RunningTextRenderer(
  modifier: Modifier = Modifier,
  text: String = "PREVIEW",
  settings: TextConfig,
  fonts: List<FontData>,
  editor: Boolean = false,
) {
  val textMeasurer = rememberTextMeasurer()
  val density = LocalDensity.current
  val distanceShadow = 4f
  val localSizeHeight = LocalSizeHeight.current

  val rawText =
    remember(text) {
      text.ifEmpty { "PREVIEW" }
    }

  val isRtl =
    remember(rawText) { Bidi(rawText, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT).isRightToLeft }
  val fontResolver = LocalFontFamilyResolver.current

  val currentFont =
    remember(settings.textStyle.fontId, fonts) {
      fonts.find { it.idFont == settings.textStyle.fontId }
    }

  val fontFamily = rememberFontFamily(currentFont)

  val fontLoadState by
    produceState(
      initialValue = fontResolver.resolve(fontFamily).value,
      key1 = fontFamily,
    ) {
      snapshotFlow { fontResolver.resolve(fontFamily).value }.collect { value = it }
    }

  BoxWithConstraints(
    modifier = modifier.fillMaxSize().background(if (editor) Color.Transparent else settings.bgColorArgb.toComposeColor()),
    contentAlignment = Alignment.CenterStart,
  ) {
    val containerHeightPx = constraints.maxHeight.toFloat()
    val containerWidthPx = constraints.maxWidth.toFloat()

    val extraPaddingPx =
      remember(settings.shadow, settings.stroke, density) {
        with(density) {
          val strokePadding = if (settings.stroke.isEnabled) settings.stroke.width else 0f
          val shadowPadding = if (settings.shadow.isEnabled) (distanceShadow + settings.shadow.radius) else 0f
          (maxOf(strokePadding, shadowPadding) + 5f).dp.toPx()
        }
      }

    val isVertical = (localSizeHeight == WindowHeightSizeClass.Compact || localSizeHeight == WindowHeightSizeClass.Medium) && !editor
    val displayContainerDim = if (isVertical) containerHeightPx else containerWidthPx
    val maxAvailableWidthPx = (displayContainerDim - (extraPaddingPx * 2)).toInt().coerceAtLeast(1)

    val largeFontSizeSp =
      remember(containerHeightPx, density) {
        with(density) {
          (containerHeightPx * 0.55f).toSp().value.coerceIn(12f, 120f).sp
        }
      }

    val largeTextStyle =
      remember(fontFamily, largeFontSizeSp) {
        TextStyle(
          fontFamily = fontFamily,
          fontWeight = FontWeight.Normal,
          fontSize = largeFontSizeSp,
          textAlign = TextAlign.Center,
          lineHeight = largeFontSizeSp * 1.05f,
          lineHeightStyle =
            LineHeightStyle(
              alignment = LineHeightStyle.Alignment.Center,
              trim = LineHeightStyle.Trim.Both,
            ),
        )
      }

    val fitsInSingleLineAtLarge =
      remember(largeTextStyle, fontLoadState, rawText, maxAvailableWidthPx) {
        val singleLineResult =
          textMeasurer.measure(
            text = rawText,
            style = largeTextStyle,
            maxLines = 1,
            softWrap = false,
          )
        singleLineResult.size.width <= maxAvailableWidthPx
      }

    val isSingleWord = remember(rawText) { RunningTextLayoutCalculator.isSingleWord(rawText) }

    val fontScale =
      remember(
        settings.isMove,
        fitsInSingleLineAtLarge,
        isSingleWord,
        fontFamily,
        containerHeightPx,
        density,
        fontLoadState,
        rawText,
        maxAvailableWidthPx,
      ) {
        val singleWordWidthPx =
          if (!settings.isMove && !fitsInSingleLineAtLarge && isSingleWord) {
            val testFontSizeSp = with(density) { (containerHeightPx * 0.26f).toSp().value.coerceIn(12f, 120f).sp }
            val testStyle =
              TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = testFontSizeSp,
                textAlign = TextAlign.Center,
                lineHeight = testFontSizeSp * 1.05f,
                lineHeightStyle =
                  LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.Both,
                  ),
              )
            textMeasurer.measure(
              text = rawText,
              style = testStyle,
              maxLines = 1,
              softWrap = false,
            ).size.width.toFloat()
          } else {
            0f
          }

        RunningTextLayoutCalculator.calculateFontScale(
          isMove = settings.isMove,
          fitsInSingleLineAtLarge = fitsInSingleLineAtLarge,
          isSingleWord = isSingleWord,
          availableWidth = maxAvailableWidthPx.toFloat(),
          singleWordWidth = singleWordWidthPx,
        )
      }

    val dynamicFontSizeSp =
      remember(containerHeightPx, density, fontScale) {
        with(density) {
          (containerHeightPx * fontScale).toSp().value.coerceIn(12f, 120f).sp
        }
      }

    val baseTextStyle =
      remember(fontFamily, dynamicFontSizeSp, settings.isMove) {
        TextStyle(
          fontFamily = fontFamily,
          fontWeight = FontWeight.Normal,
          fontSize = dynamicFontSizeSp,
          textAlign = if (settings.isMove) TextAlign.Start else TextAlign.Center,
          lineHeight = dynamicFontSizeSp * 1.05f,
          lineHeightStyle =
            LineHeightStyle(
              alignment = LineHeightStyle.Alignment.Center,
              trim = LineHeightStyle.Trim.Both,
            ),
        )
      }

    val textLayoutResult =
      remember(baseTextStyle, fontLoadState, rawText, settings.isMove, fitsInSingleLineAtLarge, isSingleWord, maxAvailableWidthPx) {
        if (settings.isMove || fitsInSingleLineAtLarge || isSingleWord) {
          textMeasurer.measure(
            text = rawText,
            style = baseTextStyle,
            maxLines = 1,
            softWrap = false,
          )
        } else {
          textMeasurer.measure(
            text = rawText,
            style = baseTextStyle,
            maxLines = 2,
            softWrap = true,
            overflow = TextOverflow.Ellipsis,
            constraints = Constraints(maxWidth = maxAvailableWidthPx),
          )
        }
      }

    val mainBrush =
      remember(settings.textStyle, textLayoutResult) {
        if (settings.textStyle.colorType == TextColorType.GRADIENT) {
          val color1 =
            settings.textStyle.gradientColorsArgb.getOrElse(0) { settings.textStyle.colorArgb }.toComposeColor()
          val color2 =
            settings.textStyle.gradientColorsArgb.getOrElse(1) { settings.textStyle.colorArgb }.toComposeColor()
          val dist = settings.textStyle.gradientDistance.coerceIn(0f, 1f)

          val textWidth = textLayoutResult.size.width.toFloat()
          val textHeight = textLayoutResult.size.height.toFloat()

          val (startOffset, endOffset) =
            if (settings.textStyle.isGradientHorizontal) {
              val shift = (dist - 0.5f) * 2f * textWidth
              Offset(shift, 0f) to Offset(textWidth + shift, 0f)
            } else {
              val shift = (dist - 0.5f) * 2f * textHeight
              Offset(0f, shift) to Offset(0f, textHeight + shift)
            }

          Brush.linearGradient(
            colors = listOf(color1, color2),
            start = startOffset,
            end = endOffset,
          )
        } else null
      }

    val totalTextWidth = textLayoutResult.size.width.toFloat() + (extraPaddingPx * 2)

    val (startX, endX) =
      remember(
        displayContainerDim,
        totalTextWidth,
        isRtl,
        settings.isMirrorMode,
        isVertical,
      ) {
        RunningTextLayoutCalculator.calculateMarqueeRange(
          containerDim = displayContainerDim,
          totalTextWidth = totalTextWidth,
          isRtl = isRtl,
          isMirrorMode = settings.isMirrorMode,
        )
      }

    val durationMillis =
      remember(settings.speed, totalTextWidth, displayContainerDim) {
        RunningTextLayoutCalculator.calculateMarqueeDurationMillis(
          containerDim = displayContainerDim,
          totalTextWidth = totalTextWidth,
          speed = settings.speed,
        )
      }

    val editorProgress = remember { Animatable(0f) }

    if (editor && settings.isMove) {
      LaunchedEffect(durationMillis) {
        while (isActive) {
          val remainingRatio = (1f - editorProgress.value).coerceIn(0f, 1f)
          val adjustedDuration = (durationMillis * remainingRatio).toInt().coerceAtLeast(1)

          editorProgress.animateTo(
            targetValue = 1f,
            animationSpec =
              tween(
                durationMillis = adjustedDuration,
                easing = LinearEasing,
              ),
          )

          if (editorProgress.value >= 1f) {
            editorProgress.snapTo(0f)
          }
        }
      }
    }

    val marqueeTransition = rememberInfiniteTransition(label = "marquee")
    val marqueeOffsetState =
      marqueeTransition.animateFloat(
        initialValue = startX,
        targetValue = endX,
        animationSpec =
          infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
          ),
        label = "offsetX",
      )

    val animatedOffsetXProvider: () -> Float = remember(settings.isMove, editor, startX, endX, displayContainerDim) {
      if (!settings.isMove) {
        val staticCenterPos = displayContainerDim / 2f
        { staticCenterPos }
      } else if (editor) {
        { lerp(startX, endX, editorProgress.value) }
      } else {
        { marqueeOffsetState.value }
      }
    }

    val blinkTransition = rememberInfiniteTransition(label = "blink")
    val blinkAlphaState =
      blinkTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec =
          infiniteRepeatable(
            animation = tween(durationMillis = 400, easing = { if (it < 0.5f) 0f else 1f }),
            repeatMode = RepeatMode.Restart,
          ),
        label = "blinkAlpha",
      )

    val blinkAlphaProvider: () -> Float = remember(settings.isBlink) {
      if (settings.isBlink) {
        { blinkAlphaState.value }
      } else {
        { 1f }
      }
    }

    Spacer(
      modifier =
        Modifier.fillMaxSize()
          .graphicsLayer {
            alpha = blinkAlphaProvider()
            val textWidth = textLayoutResult.size.width.toFloat()
            val textHeight = textLayoutResult.size.height.toFloat()

            val currentCenterX = extraPaddingPx + (textWidth / 2f)
            val currentCenterY = (size.height - textHeight) / 2f + (textHeight / 2f)

            val currentOffsetX = animatedOffsetXProvider()
            if (isVertical) {
              translationY = currentOffsetX - currentCenterY
              translationX = (containerWidthPx / 2f) - currentCenterX
            } else {
              translationX = currentOffsetX - currentCenterX
            }
            clip = false
          }
          .drawWithCache {
            val topOffsetY = (size.height - textLayoutResult.size.height) / 2f
            val baseTopLeft = Offset(extraPaddingPx, topOffsetY)

            val isShadowEnabled = settings.shadow.isEnabled
            val shadowColor = settings.shadow.colorArgb.toComposeColor()
            val shadowRadius = settings.shadow.radius
            val angleInRadians = Math.toRadians(settings.shadow.rotation.toDouble())
            val baseDistancePx = distanceShadow.dp.toPx()
            val strokeWidthPx = settings.stroke.width.dp.toPx()
            val isStrokeEnabled = settings.stroke.isEnabled && settings.stroke.width > 0
            val strokeOffsetPx = if (isStrokeEnabled) strokeWidthPx else 0f
            val totalShadowDistancePx = baseDistancePx + strokeOffsetPx
            val shadowOffsetX = (totalShadowDistancePx * cos(angleInRadians)).toFloat()
            val shadowOffsetY = (totalShadowDistancePx * sin(angleInRadians)).toFloat()
            val shadowObj =
              Shadow(
                color = shadowColor,
                offset = Offset(shadowOffsetX, shadowOffsetY),
                blurRadius = shadowRadius,
              )

            val strokeColor = settings.stroke.colorArgb.toComposeColor()
            val strokeStyle =
              Stroke(
                width = strokeWidthPx * 2f,
                join = StrokeJoin.Round,
              )

            val textColor = settings.textStyle.colorArgb.toComposeColor()

            val textCenterX = baseTopLeft.x + (textLayoutResult.size.width / 2f)
            val textCenterY = baseTopLeft.y + (textLayoutResult.size.height / 2f)
            val pivot = Offset(textCenterX, textCenterY)

            onDrawWithContent {
              val drawContent: DrawScope.() -> Unit = {
                if (isShadowEnabled) {
                  drawText(
                    textLayoutResult = textLayoutResult,
                    color = shadowColor,
                    topLeft = baseTopLeft,
                    shadow = shadowObj,
                  )
                }

                if (isStrokeEnabled) {
                  drawText(
                    textLayoutResult = textLayoutResult,
                    color = strokeColor,
                    topLeft = baseTopLeft,
                    drawStyle = strokeStyle,
                  )
                }

                if (mainBrush != null) {
                  drawText(
                    textLayoutResult = textLayoutResult,
                    brush = mainBrush,
                    topLeft = baseTopLeft,
                    drawStyle = Fill,
                    shadow = Shadow.None,
                  )
                } else {
                  drawText(
                    textLayoutResult = textLayoutResult,
                    color = textColor,
                    topLeft = baseTopLeft,
                    drawStyle = Fill,
                    shadow = Shadow.None,
                  )
                }
              }

              val drawMirroredContent: DrawScope.() -> Unit = {
                if (settings.isMirrorMode) {
                  scale(scaleX = -1f, scaleY = 1f, pivot = pivot) {
                    drawContent()
                  }
                } else {
                  drawContent()
                }
              }

              if (isVertical) {
                rotate(degrees = 90F, pivot = pivot) {
                  drawMirroredContent()
                }
              } else {
                drawMirroredContent()
              }
            }
          },
    )
  }
}

package com.example.testeditor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.daasuu.gpuv.egl.filter.*
import java.io.IOException

enum class FilterType {
    DEFAULT,
    BILATERAL_BLUR,
    BOX_BLUR,
    BRIGHTNESS,
    BULGE_DISTORTION,
    CGA_COLORSPACE,
    CONTRAST,
    CROSSHATCH,
    EXPOSURE,
    FILTER_GROUP_SAMPLE,
    GAMMA,
    GAUSSIAN_FILTER,
    GRAY_SCALE,
    HAZE,
    HALFTONE,
    HIGHLIGHT_SHADOW,
    HUE,
    INVERT,
    LUMINANCE,
    LUMINANCE_THRESHOLD,
    MONOCHROME,
    OPACITY,
    OVERLAY,
    PIXELATION,
    POSTERIZE,
    RGB,
    SATURATION,
    SEPIA,
    SHARP,
    SOLARIZE,
    SPHERE_REFRACTION,
    SWIRL,
    TONE_CURVE_SAMPLE,
    TONE,
    VIBRANCE,
    VIGNETTE,
    WATERMARK,
    WEAK_PIXEL,
//    WHITE_BALANCE,
    ZOOM_BLUR;

    companion object {
        fun createFilterList(): Array<FilterType> {
            return FilterType.values()
        }

        fun createGlFilter(filterType: FilterType, context: Context, overlayImage: Bitmap?): GlFilter {
            Log.d("!!!", overlayImage.toString())
            when (filterType) {
                DEFAULT -> return GlFilter();
                BILATERAL_BLUR -> return GlBilateralFilter();
                BOX_BLUR -> return GlBoxBlurFilter();
                BRIGHTNESS -> {
                    val glBrightnessFilter = GlBrightnessFilter()
                    glBrightnessFilter.setBrightness(0.2f);
                    return glBrightnessFilter;
                }
                BULGE_DISTORTION -> return GlBulgeDistortionFilter();
                CGA_COLORSPACE -> return GlCGAColorspaceFilter();
                CONTRAST -> {
                    val glContrastFilter = GlContrastFilter();
                    glContrastFilter.setContrast(2.5f);
                    return glContrastFilter;
                }
                CROSSHATCH -> return GlCrosshatchFilter();
                EXPOSURE -> return GlExposureFilter();
                FILTER_GROUP_SAMPLE -> return GlFilterGroup(GlSepiaFilter(), GlVignetteFilter());
                GAMMA -> {
                    val glGammaFilter = GlGammaFilter()
                    glGammaFilter.setGamma(2f);
                    return glGammaFilter;
                }
                GAUSSIAN_FILTER -> return GlGaussianBlurFilter();
                GRAY_SCALE -> return GlGrayScaleFilter();
                HALFTONE -> return GlHalftoneFilter();
                HAZE -> {
                    val glHazeFilter = GlHazeFilter();
                    glHazeFilter.setSlope(-0.5f);
                    return glHazeFilter;
                }
                HIGHLIGHT_SHADOW -> return GlHighlightShadowFilter()
                HUE -> return GlHueFilter()
                INVERT -> return GlInvertFilter();
                LUMINANCE -> return GlLuminanceFilter();
                LUMINANCE_THRESHOLD -> return GlLuminanceThresholdFilter();
                MONOCHROME -> return GlMonochromeFilter();
                OPACITY -> return GlOpacityFilter();
/*                OVERLAY -> {
                    Log.d("!!!", "OVERLAY 선택됨")
//                    return GlBitmapOverlaySampleFilter(overlayImage!!)
                    return GlBitmapOverlaySampleFilter(BitmapFactory.decodeResource(context.resources, R.drawable.test_sticker))
                }*/
                PIXELATION -> return GlPixelationFilter();
                POSTERIZE -> return GlPosterizeFilter();
                RGB -> {
                    val glRGBFilter = GlRGBFilter();
                    glRGBFilter.setRed(0f);
                    return glRGBFilter;
                }
                SATURATION -> GlSaturationFilter();
                SEPIA -> GlSepiaFilter();
                SHARP -> {
                    val glSharpenFilter = GlSharpenFilter();
                    glSharpenFilter.setSharpness(4f);
                    return glSharpenFilter;
                }
                SOLARIZE -> return GlSolarizeFilter();
                SPHERE_REFRACTION -> return GlSphereRefractionFilter();
                SWIRL -> return GlSwirlFilter();
                TONE_CURVE_SAMPLE -> {
                    try {
                        val inputStream = context.getAssets().open("acv/tone_cuver_sample.acv");
                        return GlToneCurveFilter(inputStream);
                    } catch (e: IOException) {
                        Log.e("FilterType", "Error");
                    }
                    return GlFilter()
                }
                TONE -> return GlToneFilter();
                VIBRANCE -> {
                    val glVibranceFilter = GlVibranceFilter();
                    glVibranceFilter.setVibrance(3f);
                    return glVibranceFilter;
                }
                VIGNETTE -> GlVignetteFilter();
                WATERMARK -> GlWatermarkFilter(
                    BitmapFactory.decodeResource(context.resources, R.drawable.test_sticker), GlWatermarkFilter.Position.LEFT_BOTTOM
                );
                WEAK_PIXEL -> GlWeakPixelInclusionFilter();
/*                WHITE_BALANCE -> {
                    val glWhiteBalanceFilter = GlWhiteBalanceFilter();
                    glWhiteBalanceFilter.setTemperature(2400f);
                    glWhiteBalanceFilter.setTint(2f);
                    return glWhiteBalanceFilter;
                }*/
                ZOOM_BLUR -> GlZoomBlurFilter();
                else -> return GlFilter()
            }
            return GlFilter()
        }
    }
}
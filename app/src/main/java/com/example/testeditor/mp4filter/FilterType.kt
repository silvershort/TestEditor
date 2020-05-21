package com.example.testeditor.mp4filter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.daasuu.gpuv.egl.filter.*
import com.example.testeditor.R
import java.io.IOException

enum class FilterType {
    DEFAULT,
    AFTERGLOW,
    ALICE_IN_WONDERLAND,
    AMBERS,
    AURORA,
    BLUE_POPPIES,
    BLUE_YELLOW_FIELD,
    CAROUSEL,
    COLD_DESERT,
    COLD_HEART,
    DIGITAL_FILM,
    DOCUMENTARY,
    ELECTRIC,
    GROST_IN_YOUR_HEAD,
    GOOD_LUCK_CHARM,
    GREEN_ENVY,
    HUMMINGBIRDS,
    KISS_KISS,
    LEFT_HAND_BLUES,
    LIGHT_PARADES,
    LULLABYE,
    MOTH_WINGS,
    OLD_POSTCARD1,
    OLD_POSTCARD2,
    PEACOCK_FEATURES,
    PISTOL,
    REGDOLL,
    ROSE_THORNS1,
    ROSE_THORNS2,
    SET_YOU_FREE,
    SNOW_WHITE,
    TOES_IN_THE_OCEAN,
    WILD_AT_HEART,
    WINDOWS_WARMTH;

    companion object {
        fun createFilterList(): Array<FilterType> = values()

        fun createGlFilter(filterType: FilterType, context: Context, overlayImage: Bitmap?): GlFilter {
            Log.d("!!!", overlayImage.toString())
            when (filterType) {
                DEFAULT -> return GlFilter()
                AFTERGLOW -> {
                    try {
                        val `is` = context!!.assets.open("acv/afterglow.acv")
                        return GlToneCurveFilter(`is`)
                    } catch (e: IOException) {
                        printLogError()
                    }

                    return GlFilter()
                }
                ALICE_IN_WONDERLAND -> {
                    try {
                        val `is` = context!!.assets.open("acv/alice_in_wonderland.acv")
                        return GlToneCurveFilter(`is`)
                    } catch (e: IOException) {
                        printLogError()
                    }

                    return GlFilter()
                }
                AMBERS -> {
                    try {
                        val `is` = context!!.assets.open("acv/amber.acv")
                        return GlToneCurveFilter(`is`)
                    } catch (e: IOException) {
                        printLogError()
                    }

                    return GlFilter()
                }
                AURORA -> {
                    try {
                        val `is` = context!!.assets.open("acv/aurora.acv")
                        return GlToneCurveFilter(`is`)
                    } catch (e: IOException) {
                        printLogError()
                    }

                    return GlFilter()
                }
                BLUE_POPPIES -> {
                    try {
                        val `is` = context!!.assets.open("acv/blue_poppies.acv")
                        return GlToneCurveFilter(`is`)
                    } catch (e: IOException) {
                        printLogError()
                    }

                    return GlFilter()
                }
                BLUE_YELLOW_FIELD -> {
                    try {
                        val `is` = context!!.assets.open("acv/blue_yellow_field.acv")
                        return GlToneCurveFilter(`is`)
                    } catch (e: IOException) {
                        printLogError()
                    }

                    return GlFilter()
                }
                CAROUSEL -> {
                    try {
                        val `is` = context!!.assets.open("acv/carousel.acv")
                        return GlToneCurveFilter(`is`)
                    } catch (e: IOException) {
                        printLogError()
                    }

                    return GlFilter()
                }
                COLD_DESERT -> {
                    try {
                        val `is` = context!!.assets.open("acv/cold_desert.acv")
                        return GlToneCurveFilter(`is`)
                    } catch (e: IOException) {
                        printLogError()
                    }

                    return GlFilter()
                }
                COLD_HEART -> {
                    try {
                        val `is` = context!!.assets.open("acv/cold_heart.acv")
                        return GlToneCurveFilter(`is`)
                    } catch (e: IOException) {
                        printLogError()
                    }

                    return GlFilter()
                }
                DIGITAL_FILM -> {
                    try {
                        val `is` = context!!.assets.open("acv/digital_film.acv")
                        return GlToneCurveFilter(`is`)
                    } catch (e: IOException) {
                        printLogError()
                    }

                    return GlFilter()
                }
                DOCUMENTARY -> {
                    try {
                        val `is` = context!!.assets.open("acv/documentary.acv")
                        return GlToneCurveFilter(`is`)
                    } catch (e: IOException) {
                        printLogError()
                    }

                    return GlFilter()
                }
                ELECTRIC -> {
                    try {
                        val `is` = context!!.assets.open("acv/electric.acv")
                        return GlToneCurveFilter(`is`)
                    } catch (e: IOException) {
                        printLogError()
                    }

                    return GlFilter()
                }
                GROST_IN_YOUR_HEAD -> {
                    try {
                        val `is` = context!!.assets.open("acv/ghosts_in_your_head.acv")
                        return GlToneCurveFilter(`is`)
                    } catch (e: IOException) {
                        printLogError()
                    }

                    return GlFilter()
                }
                GOOD_LUCK_CHARM -> {
                    try {
                        val `is` = context!!.assets.open("acv/good_luck_charm.acv")
                        return GlToneCurveFilter(`is`)
                    } catch (e: IOException) {
                        printLogError()
                    }

                    return GlFilter()
                }
                GREEN_ENVY -> {
                    try {
                        val `is` = context!!.assets.open("acv/green_envy.acv")
                        return GlToneCurveFilter(`is`)
                    } catch (e: IOException) {
                        printLogError()
                    }

                    return GlFilter()
                }
                HUMMINGBIRDS -> {
                    try {
                        val `is` = context!!.assets.open("acv/hummingbirds.acv")
                        return GlToneCurveFilter(`is`)
                    } catch (e: IOException) {
                        printLogError()
                    }

                    return GlFilter()
                }
                KISS_KISS -> {
                    try {
                        val `is` = context!!.assets.open("acv/kiss_kiss.acv")
                        return GlToneCurveFilter(`is`)
                    } catch (e: IOException) {
                        printLogError()
                    }

                    return GlFilter()
                }
                LEFT_HAND_BLUES -> {
                    try {
                        val `is` = context!!.assets.open("acv/left_hand_blues.acv")
                        return GlToneCurveFilter(`is`)
                    } catch (e: IOException) {
                        printLogError()
                    }

                    return GlFilter()
                }
                LIGHT_PARADES -> {
                    try {
                        val `is` = context!!.assets.open("acv/light_parades.acv")
                        return GlToneCurveFilter(`is`)
                    } catch (e: IOException) {
                        printLogError()
                    }

                    return GlFilter()
                }
                LULLABYE -> {
                    try {
                        val `is` = context!!.assets.open("acv/lullabye.acv")
                        return GlToneCurveFilter(`is`)
                    } catch (e: IOException) {
                        printLogError()
                    }

                    return GlFilter()
                }
                MOTH_WINGS -> {
                    try {
                        val `is` = context!!.assets.open("acv/moth_wings.acv")
                        return GlToneCurveFilter(`is`)
                    } catch (e: IOException) {
                        printLogError()
                    }

                    return GlFilter()
                }
                OLD_POSTCARD1 -> {
                    try {
                        val `is` = context!!.assets.open("acv/old_postcards_01.acv")
                        return GlToneCurveFilter(`is`)
                    } catch (e: IOException) {
                        printLogError()
                    }

                    return GlFilter()
                }
                OLD_POSTCARD2 -> {
                    try {
                        val `is` = context!!.assets.open("acv/old_postcards_02.acv")
                        return GlToneCurveFilter(`is`)
                    } catch (e: IOException) {
                        printLogError()
                    }

                    return GlFilter()
                }
                REGDOLL -> {
                    try {
                        val `is` = context!!.assets.open("acv/regdoll.acv")
                        return GlToneCurveFilter(`is`)
                    } catch (e: IOException) {
                        printLogError()
                    }

                    return GlFilter()
                }
                PEACOCK_FEATURES -> {
                    try {
                        val `is` = context!!.assets.open("acv/peacock_feathers.acv")
                        return GlToneCurveFilter(`is`)
                    } catch (e: IOException) {
                        printLogError()
                    }

                    return GlFilter()
                }
                PISTOL -> {
                    try {
                        val `is` = context!!.assets.open("acv/pistol.acv")
                        return GlToneCurveFilter(`is`)
                    } catch (e: IOException) {
                        printLogError()
                    }

                    return GlFilter()
                }
                ROSE_THORNS1 -> {
                    try {
                        val `is` = context!!.assets.open("acv/rose_thorns_01.acv")
                        return GlToneCurveFilter(`is`)
                    } catch (e: IOException) {
                        printLogError()
                    }

                    return GlFilter()
                }
                ROSE_THORNS2 -> {
                    try {
                        val `is` = context!!.assets.open("acv/rose_thorns_02.acv")
                        return GlToneCurveFilter(`is`)
                    } catch (e: IOException) {
                        printLogError()
                    }

                    return GlFilter()
                }
                SET_YOU_FREE -> {
                    try {
                        val `is` = context!!.assets.open("acv/set_you_free.acv")
                        return GlToneCurveFilter(`is`)
                    } catch (e: IOException) {
                        printLogError()
                    }

                    return GlFilter()
                }
                SNOW_WHITE -> {
                    try {
                        val `is` = context!!.assets.open("acv/snow_white.acv")
                        return GlToneCurveFilter(`is`)
                    } catch (e: IOException) {
                        printLogError()
                    }

                    return GlFilter()
                }
                TOES_IN_THE_OCEAN -> {
                    try {
                        val `is` = context!!.assets.open("acv/toes_in_the_ocean.acv")
                        return GlToneCurveFilter(`is`)
                    } catch (e: IOException) {
                        printLogError()
                    }

                    return GlFilter()
                }
                WILD_AT_HEART -> {
                    try {
                        val `is` = context!!.assets.open("acv/wild_at_heart.acv")
                        return GlToneCurveFilter(`is`)
                    } catch (e: IOException) {
                        printLogError()
                    }

                    return GlFilter()
                }
                WINDOWS_WARMTH -> {
                    try {
                        val `is` = context!!.assets.open("acv/window_warmth.acv")
                        return GlToneCurveFilter(`is`)
                    } catch (e: IOException) {
                        printLogError()
                    }

                    return GlFilter()
                }
                else -> return GlFilter()
            }
        }

        private fun printLogError() {
            Log.e("FilterType", "Error")
        }
    }
}
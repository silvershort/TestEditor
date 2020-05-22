package com.example.testeditor.sticker.utils;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FontProvider {

    private static final String DEFAULT_FONT_NAME = "NanumBarunGothic";

    private final Map<String, Typeface> typefaces;
    private final Map<String, String> fontNameToTypefaceFile;
    private final Resources resources;
    private final List<String> fontNames;

    public FontProvider(Resources resources) {
        this.resources = resources;

        typefaces = new HashMap<>();

        // populate fonts
        fontNameToTypefaceFile = new HashMap<>();
        fontNameToTypefaceFile.put("NanumBarunGothic", "NanumBarunGothic.ttf");
        fontNameToTypefaceFile.put("NanumBarunGothic-YetHangul", "NanumBarunGothic-YetHangul.ttf");
        fontNameToTypefaceFile.put("NanumBarunGothicBold", "NanumBarunGothicBold.ttf");
        fontNameToTypefaceFile.put("NanumBarunGothicLight", "NanumBarunGothicLight.ttf");
        fontNameToTypefaceFile.put("NanumBarunGothicUltraLight", "NanumBarunGothicUltraLight.ttf");
        fontNameToTypefaceFile.put("NanumBarunpenB", "NanumBarunpenB.ttf");
        fontNameToTypefaceFile.put("NanumBarunpenR", "NanumBarunpenR.ttf");
        fontNameToTypefaceFile.put("NanumBrush", "NanumBrush.ttf");
        fontNameToTypefaceFile.put("NanumGothic", "NanumGothic.ttf");
        fontNameToTypefaceFile.put("NanumMyeongjo", "NanumMyeongjo.ttf");
        fontNameToTypefaceFile.put("NanumPen", "NanumPen.ttf");
        fontNameToTypefaceFile.put("NanumSquare_acB", "NanumSquare_acB.ttf");

        fontNames = new ArrayList<>(fontNameToTypefaceFile.keySet());
    }

    /**
     * @param typefaceName must be one of the font names provided from {@link FontProvider#getFontNames()}
     * @return the Typeface associated with {@code typefaceName}, or {@link Typeface#DEFAULT} otherwise
     */
    public Typeface getTypeface(@Nullable String typefaceName) {
        if (TextUtils.isEmpty(typefaceName)) {
            return Typeface.DEFAULT;
        } else {
            //noinspection Java8CollectionsApi
            if (typefaces.get(typefaceName) == null) {
                typefaces.put(typefaceName,
                        Typeface.createFromAsset(resources.getAssets(), "fonts/" + fontNameToTypefaceFile.get(typefaceName)));
            }
            return typefaces.get(typefaceName);
        }
    }

    /**
     * use {@link FontProvider#getTypeface(String) to get Typeface for the font name}
     *
     * @return list of available font names
     */
    public List<String> getFontNames() {
        return fontNames;
    }

    /**
     * @return Default Font Name - <b>Helvetica</b>
     */
    public String getDefaultFontName() {
        return DEFAULT_FONT_NAME;
    }
}
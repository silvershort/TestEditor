package com.example.testeditor.sound

import com.example.testeditor.sound.SoundSelectAdapter

interface OnSoundSelectClickListener {
    fun playButtonClick(holder: SoundSelectAdapter.SoundHolder, position: Int)
    fun selectButtonClick(holder: SoundSelectAdapter.SoundHolder, position: Int)
}
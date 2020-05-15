package com.example.testeditor

interface OnAudioSelectClickListener {
    fun playButtonClick(holder: AudioSelectAdapter.AudioHolder, position: Int)
    fun selectButtonClick(holder: AudioSelectAdapter.AudioHolder, position: Int)
}
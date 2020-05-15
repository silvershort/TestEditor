package com.example.testeditor

import com.example.testeditor.mp4filter.FilterAdapter

interface OnFilterClickListener {
    fun onFilterClick(holder: FilterAdapter.FilterHolder, position: Int)
}
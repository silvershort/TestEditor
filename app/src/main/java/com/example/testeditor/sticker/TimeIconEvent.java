package com.example.testeditor.sticker;

import android.view.MotionEvent;

/**
 * @author wupanjie
 */

public class TimeIconEvent implements StickerIconEvent {
  @Override public void onActionDown(StickerView stickerView, MotionEvent event) {

  }

  @Override public void onActionMove(StickerView stickerView, MotionEvent event) {
  }

  @Override public void onActionUp(StickerView stickerView, MotionEvent event) {
    if (stickerView.getOnStickerOperationListener() != null) {
      stickerView.getOnStickerOperationListener()
          .onStickerTimeClicked(stickerView.getCurrentSticker());
    }
  }
}

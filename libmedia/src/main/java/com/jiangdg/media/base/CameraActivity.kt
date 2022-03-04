/*
 * Copyright 2017-2022 Jiangdg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jiangdg.media.base

import android.graphics.SurfaceTexture
import android.view.Gravity
import android.view.SurfaceHolder
import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.jiangdg.media.CameraClient
import com.jiangdg.media.camera.Camera1Strategy
import com.jiangdg.media.camera.bean.CameraRequest
import com.jiangdg.media.render.filter.FilterBlackWhite
import com.jiangdg.media.utils.Logger
import com.jiangdg.media.widget.AspectRatioSurfaceView
import com.jiangdg.media.widget.AspectRatioTextureView
import com.jiangdg.media.widget.IAspectRatio

/** Extends from BaseActivity for CameraClient usage
 *
 * @author Created by jiangdg on 2022/1/28
 */
abstract class CameraActivity : BaseActivity(){
    private var mCameraClient: CameraClient? = null

    override fun initData() {
        val client = getCameraClient() ?: getDefault()
        when (val cameraView = getCameraView()) {
            is AspectRatioTextureView -> {
                handleTextureView(cameraView)
                cameraView
            }
            is AspectRatioSurfaceView -> {
                handleSurfaceView(cameraView)
                cameraView
            }
            else -> {
                null
            }
        }?.let { view->
            getCameraViewContainer()?.apply {
                removeAllViews()
                addView(view, getViewLayoutParams(this))
            }
            mCameraClient = getCameraClient() ?: getDefault()
        }
    }

    private fun handleTextureView(textureView: AspectRatioTextureView) {
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture?,
                width: Int,
                height: Int
            ) {
                Logger.i(TAG, "handleTextureView onSurfaceTextureAvailable")
                openCamera(textureView)
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture?,
                width: Int,
                height: Int
            ) {
                Logger.i(TAG, "handleTextureView onSurfaceTextureAvailable")
                surfaceSizeChanged(width, height)
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                Logger.i(TAG, "handleTextureView onSurfaceTextureDestroyed")
                closeCamera()
                return false
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
            }
        }
    }

    private fun handleSurfaceView(surfaceView: AspectRatioSurfaceView) {
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder?) {
                Logger.i(TAG, "handleSurfaceView surfaceCreated")
                openCamera(surfaceView)
            }

            override fun surfaceChanged(
                holder: SurfaceHolder?,
                format: Int,
                width: Int,
                height: Int
            ) {
                Logger.i(TAG, "handleSurfaceView surfaceChanged")
                surfaceSizeChanged(width, height)
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                Logger.i(TAG, "handleSurfaceView surfaceDestroyed")
                closeCamera()
            }
        })
    }

    private fun openCamera(st: IAspectRatio? = null) {
        mCameraClient?.openCamera(st)
    }

    protected fun closeCamera() {
        mCameraClient?.closeCamera()
    }

    protected fun surfaceSizeChanged(surfaceWidth: Int, surfaceHeight: Int) {
        mCameraClient?.setRenderSize(surfaceWidth, surfaceHeight)
    }

    private fun getViewLayoutParams(viewGroup: ViewGroup): ViewGroup.LayoutParams? {
        return when(viewGroup) {
            is FrameLayout -> {
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER
                )
            }
            is LinearLayout -> {
                LinearLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    gravity = Gravity.CENTER
                }
            }
            is RelativeLayout -> {
                RelativeLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE)
                    addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE)
                }
            }
            else -> null
        }
    }
    /**
     * Get camera view
     *
     * @return CameraView, such as AspectRatioTextureView etc.
     */
    protected abstract fun getCameraView(): IAspectRatio?

    protected abstract fun getCameraViewContainer(): ViewGroup?

    protected open fun getCameraClient(): CameraClient? {
        return null
    }

    private fun getDefault(): CameraClient {
        return CameraClient.newBuilder(this)
            .setEnableGLES(true)
            .setDefaultFilter(FilterBlackWhite(this))
            .setCameraStrategy(Camera1Strategy(this))
            .setCameraRequest(getCameraRequest())
            .openDebug(true)
            .build()
    }

    private fun getCameraRequest(): CameraRequest {
        return CameraRequest.CameraRequestBuilder()
            .setFrontCamera(false)
            .setContinuousAFModel(true)
            .setContinuousAFModel(true)
            .setPreviewWidth(1280)
            .setPreviewHeight(720)
            .create()
    }

    companion object {
        private const val TAG = "CameraActivity"
    }
}
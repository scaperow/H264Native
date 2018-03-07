package com.example.h264native;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

import android.hardware.Camera;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;

public class AvcEncoder {

	private byte[] sps;
	private byte[] pps;

	private MediaCodec mediaCodec;
	private BufferedOutputStream outputStream;

	

	
	public AvcEncoder(Surface surface) {
		File f = new File(Environment.getExternalStorageDirectory(),
				"Download/video_encoded.264");
		// touch(f);
		try {
			outputStream = new BufferedOutputStream(new FileOutputStream(f));
			Log.i("AvcEncoder", "outputStream initialized");
		} catch (Exception e) {
			e.printStackTrace();
		}

		// mediaCodec = MediaCodec.createByCodecName("OMX.google.h264.encoder");
		//
		// MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc",
		// 320, 240);
		// mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 125000);
		// mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
		// mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
		// MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
		// mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
		// mediaCodec.configure(mediaFormat, null, null,
		// MediaCodec.CONFIGURE_FLAG_ENCODE);

		mediaCodec = MediaCodec.createByCodecName("OMX.google.h264.encoder");
		MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", 320, 240);
		mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 500000);
		mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
		//mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,MediaCodecInfo.CodecCapabilities.);
		mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
				MediaCodecInfo.CodecCapabilities.COLOR_Format16bitRGB565); // <2>
		mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
		mediaCodec.configure(mediaFormat, null, null,
				MediaCodec.CONFIGURE_FLAG_ENCODE);
		mediaCodec.start();

		inputBuffers = mediaCodec.getInputBuffers();
		outputBuffers = mediaCodec.getOutputBuffers();
	}

	public void close() {
		try {
			mediaCodec.stop();
			mediaCodec.release();
			outputStream.flush();
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	ByteBuffer[] inputBuffers;
	ByteBuffer[] outputBuffers;

	// called from Camera.setPreviewCallbackWithBuffer(...) in other class
	public void offerEncoder(byte[] input) {
		try {

			Log.i("******", input.length+"");
			int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
			Log.i("+++++++", inputBufferIndex + "");
			if (inputBufferIndex >= 0) {
				ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
				inputBuffer.clear();
				inputBuffer.put(input);
				mediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length,
						0, 0);
			}
			MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
			int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo,
					0);
			Log.i("--------", outputBufferIndex + "");
			while (outputBufferIndex >= 0) {
				ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
				byte[] outData = new byte[bufferInfo.size];
				outputBuffer.get(outData);
				outputStream.write(outData, 0, outData.length);
				Log.i("AvcEncoder", outData.length + " bytes written");

				mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
				outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo,
						0);
			}

			if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
				// Subsequent data will conform to new format.
				MediaFormat format = mediaCodec.getOutputFormat();

			} else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
				outputBuffers = mediaCodec.getOutputBuffers();
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
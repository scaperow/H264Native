package com.example.h264native;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.Menu;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

	Camera camera;
	SurfaceView surface;
	SurfaceHolder holder;
	MediaRecorder recorder;
	MediaCodec codec;
	File file;
	FileOutputStream stream;

	SurfaceView surface2;
	SurfaceHolder holder2;
	Button buttonClose;
	AvcEncoder encoder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// new Thread(new Runnable(){
		//
		// @Override
		// public void run() {
		// // TODO Auto-generated method stub
		// EncoderTest test = new EncoderTest();
		// test.testAACEncoders();
		// test.testAMRNBEncoders();
		// test.testAMRWBEncoders();
		// }
		//
		// }).start();

		//setControls();

		// for (int i = 0; i < MediaCodecList.getCodecCount(); i++) {
		// MediaCodecInfo codec = MediaCodecList.getCodecInfoAt(i);
		// String types = "";
		// for (int j = 0; j < codec.getSupportedTypes().length; j++) {
		// types = types + ", " + codec.getSupportedTypes()[j];
		// }
		// Log.i("CODES", codec.getName() + ": " + types);
		// }
	}

	// private void setCode() {
	//
	// codec = MediaCodec.createByCodecName("OMX.google.h264.encoder");
	// MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc",
	// 320, 240);
	// mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 500000);
	// mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
	// mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
	// MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
	// mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
	// codec.configure(mediaFormat, null, null,
	// MediaCodec.CONFIGURE_FLAG_ENCODE);
	// codec.start();
	//
	// inputBuffers = codec.getInputBuffers();
	// outputBuffers = codec.getOutputBuffers();
	//
	// }

	ByteBuffer[] inputBuffers;
	ByteBuffer[] outputBuffers;

	//
	// public void offerEncoder(byte[] input) {
	// try {
	//
	// if (stream == null) {
	// return;
	// }
	//
	// int inputBufferIndex = codec.dequeueInputBuffer(10);
	// if (inputBufferIndex >= 0) {
	// codec.queueInputBuffer(inputBufferIndex, 0, input.length, 10, 0);
	// }
	//
	// MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
	// int outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 10);
	//
	// if (outputBufferIndex >= 0) {
	//
	// ByteBuffer outputBuffer = inputBuffers[outputBufferIndex];
	// byte[] outData = new byte[bufferInfo.size];
	// outputBuffer.get(outData);
	// stream.write(outData, 0, outData.length);
	// codec.releaseOutputBuffer(outputBufferIndex,false);
	//
	// } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
	// outputBuffers = codec.getOutputBuffers();
	// }
	// // }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// }

	public static byte[] YV12toYUV420PackedSemiPlanar(final byte[] input,
			final byte[] output, final int width, final int height) {
		/*
		 * COLOR_TI_FormatYUV420PackedSemiPlanar is NV12 We convert by putting
		 * the corresponding U and V bytes together (interleaved).
		 */
		final int frameSize = width * height;
		final int qFrameSize = frameSize / 4;

		System.arraycopy(input, 0, output, 0, frameSize); // Y

		for (int i = 0; i < qFrameSize; i++) {
			output[frameSize + i * 2] = input[frameSize + i + qFrameSize]; // Cb
																			// (U)
			output[frameSize + i * 2 + 1] = input[frameSize + i]; // Cr (V)
		}
		return output;
	}

	private void setControls() {
		// encoder = new AvcEncoder();
		file = new File("/mnt/sdcard/record.mp4");
		try {
			stream = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		surface = (SurfaceView) this.findViewById(R.id.surfaceView1);
		surface2 = (SurfaceView) this.findViewById(R.id.surfaceView2);
		holder = surface.getHolder();
		holder.addCallback(new Callback() {

			@Override
			public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void surfaceCreated(SurfaceHolder arg0) {
				// TODO Auto-generated method stub
				holder = arg0;

				// run();
				openCamera();

			}

			@Override
			public void surfaceDestroyed(SurfaceHolder arg0) {
				// TODO Auto-generated method stub
				holder = null;
			}

		});

		holder2 = surface2.getHolder();
		holder.addCallback(new Callback() {

			@Override
			public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2,
					int arg3) {
				holder2 = arg0;
			}

			@Override
			public void surfaceCreated(SurfaceHolder arg0) {
				// TODO Auto-generated method stub
				holder2 = arg0;
				encoder = new AvcEncoder(holder2.getSurface());
				// openCamera();
			}

			@Override
			public void surfaceDestroyed(SurfaceHolder arg0) {
				// TODO Auto-generated method stub
				holder2 = null;
			}

		});

		buttonClose = (Button) this.findViewById(R.id.button1);
		buttonClose.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				camera.stopPreview();

				try {
					stream.flush();
					stream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		});

	}

	private void openCamera() {
		camera = Camera.open();
		try {
			camera.setPreviewDisplay(holder);
			Parameters params = camera.getParameters();
			params.setPreviewSize(320, 240);
			params.setPictureFormat(ImageFormat.RGB_565);
			camera.setParameters(params);

			camera.setPreviewCallback(new PreviewCallback() {
				// TODO Auto-generated method stub
				@Override
				public void onPreviewFrame(byte[] arg0, Camera arg1) {
					// TODO Auto-generated method stub
					// encoder.offerEncoder(arg0);
					// encode(arg0);
					if (encoder != null) {
						byte[] sp = new byte[320 * 240];
						YV12toYUV420PackedSemiPlanar(arg0, sp, 320, 240);
						encoder.offerEncoder(sp);
					}
				}

			});
			camera.startPreview();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private MediaCodec mediaCodec;

	public void encode(byte[] input) {
		ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
		ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
		BufferInfo info = new BufferInfo();
		boolean isEOS = false;
		long startMs = System.currentTimeMillis();

		while (!Thread.interrupted()) {
			if (!isEOS) {
				int inIndex = mediaCodec.dequeueInputBuffer(10000);
				if (inIndex >= 0) {
					ByteBuffer inputBuffer = inputBuffers[inIndex];
					inputBuffer.clear();
					inputBuffer.put(input);
					mediaCodec.queueInputBuffer(inIndex, 0, 0, 0,
							MediaCodec.BUFFER_FLAG_END_OF_STREAM);
				}
			}

			int outIndex = mediaCodec.dequeueOutputBuffer(info, 10000);
			switch (outIndex) {
			case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
				Log.d("DecodeActivity", "INFO_OUTPUT_BUFFERS_CHANGED");
				outputBuffers = mediaCodec.getOutputBuffers();
				break;
			case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
				Log.d("DecodeActivity",
						"New format " + mediaCodec.getOutputFormat());
				break;
			case MediaCodec.INFO_TRY_AGAIN_LATER:
				Log.d("DecodeActivity", "dequeueOutputBuffer timed out!");
				break;
			default:
				ByteBuffer buffer = outputBuffers[outIndex];
				Log.v("DecodeActivity",
						"We can't use this buffer but render it due to the API limit, "
								+ buffer);

				// We use a very simple clock to keep the video FPS, or the
				// video
				// playback will be too fast
				while (info.presentationTimeUs / 1000 > System
						.currentTimeMillis() - startMs) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
						break;
					}
				}
				mediaCodec.releaseOutputBuffer(outIndex, true);
				break;
			}

			if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
				Log.d("DecodeActivity",
						"OutputBuffer BUFFER_FLAG_END_OF_STREAM");
				break;
			}
		}
	}

	public void close() {

		mediaCodec.stop();
		mediaCodec.release();
	}

	private BufferedOutputStream outputStream;
	// public void run() {
	// File f = new File(Environment.getExternalStorageDirectory(),
	// "Download/video_encoded.264");
	//
	// final int kBitRates[] =
	// { 4750, 5150, 5900, 6700, 7400, 7950, 10200, 12200 };
	// // touch(f);
	// try {
	// outputStream = new BufferedOutputStream(new FileOutputStream(f));
	// Log.i("AvcEncoder", "outputStream initialized");
	// } catch (Exception e) {
	// e.printStackTrace();
	// }

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

	// mediaCodec.start();

	// inputBuffers = mediaCodec.getInputBuffers();
	// outputBuffers = mediaCodec.getOutputBuffers();

	// }
}
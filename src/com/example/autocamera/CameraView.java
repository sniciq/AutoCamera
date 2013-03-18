package com.example.autocamera;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.net.Uri;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
//import android.hardware.Camera;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback, Camera.PictureCallback {

	private SurfaceHolder holder;
	private Camera camera;
	private Context context;
	private Timer timer = null;

	public CameraView(Context context) {
		super(context);
		this.context = context;
		holder = getHolder();
		holder.addCallback(this);
	}

	private int getCameraId(boolean isFront) {
		CameraInfo ci = new CameraInfo();
		for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
			Camera.getCameraInfo(i, ci);
			if (isFront) {
				if (ci.facing == CameraInfo.CAMERA_FACING_FRONT) {
					return i;
				}
			} else {
				if (ci.facing == CameraInfo.CAMERA_FACING_BACK) {
					return i;
				}
			}
		}
		return -1; // No front-facing camera found
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			int frontCamera = getCameraId(false);
			camera = Camera.open(frontCamera);
			camera.setDisplayOrientation(90);
			camera.setPreviewDisplay(holder);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		try {
			Camera.Parameters perameters = camera.getParameters();
//			perameters.setPreviewSize(240, 320);
			camera.setParameters(perameters);
			camera.autoFocus(null);
			camera.startPreview();
			startTimer();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		camera.setPreviewCallback(null);
		camera.stopPreview();
		camera.release();
		camera = null;
	}
	
	private File getAlbumDir() {
		return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "album");
	}
	
	private void galleryAddPic(String path) {
		Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
		File f = new File(path);
		Uri contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);
		context.sendBroadcast(mediaScanIntent);
	}

	private void savePicToGallery(byte[] data) throws Exception {
		String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date());
		String imageFileName = "IMG_" + timeStamp + "_";
		File albumF = getAlbumDir();
		File imageF = File.createTempFile(imageFileName, ".jpg", albumF);
		OutputStream outStream = new FileOutputStream(imageF);
		outStream.write(data);
		outStream.close();
		galleryAddPic(imageF.getPath());
//		String saved = MediaStore.Images.Media.insertImage(this.context.getContentResolver(), picture, imageFileName, "description");
//        Uri sdCardUri = Uri.parse("file://" + Environment.getExternalStorageDirectory());
//        this.context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, sdCardUri));
	}
	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		try {
			System.out.println("onPictureTaken");
//			Bitmap picture = BitmapFactory.decodeByteArray(data, 0, data.length);
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			picture.compress(Bitmap.CompressFormat.JPEG, 100, baos);
			savePicToGallery(data);
//			baos.close();
			camera.startPreview();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void pause() {
		this.timer.cancel();
		this.timer.purge();
		this.timer = null;
	}

	public void resume() {
		this.startTimer();
	}
	
	private void startTimer() {
		if(timer != null)
			return;
		
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if(camera != null) {
					System.out.println("take picture start ...");
					camera.takePicture(null, null, CameraView.this);
					System.out.println("BBBBBBBBBB");
					System.out.println("-------------------");
				}
			}
		}, 3 * 1000, 5 * 1000);
	}
}

////ºáÊúÆÁÇÐ»»
//@Override
//public void onConfigurationChanged(Configuration newConfig) {
//        // TODO Auto-generated method stub
//        super.onConfigurationChanged(newConfig);
//        if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
//                //ºáÆÁ
//    mCamera.setDisplayOrientation(0);
//}else{
//        //ÊúÆÁ
//        mCamera.setDisplayOrientation(90);
//}
//}

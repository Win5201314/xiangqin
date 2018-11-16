package com.socialassistant_youyuelive.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.socialassistant_youyuelive.R;
import com.socialassistant_youyuelive.commomentity.ActionSheetDialog;
import com.socialassistant_youyuelive.commomentity.RoundProgressBar;
import com.socialassistant_youyuelive.util.ShowToast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * 单张图片显示Fragment
 */
public class AlbumFragment extends Fragment {

    private String mImageUrl;
    private ImageView mImageView;
    private RoundProgressBar progressbar;
    private PhotoView test_iv;
    private PhotoViewAttacher mAttacher;

    public AlbumFragment() {}

    public static AlbumFragment newInstance(String imageUrl) {
        final AlbumFragment f = new AlbumFragment();

        final Bundle args = new Bundle();
        args.putString("url", imageUrl);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageUrl = getArguments() != null ? getArguments().getString("url") : null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album, container, false);
        progressbar = (RoundProgressBar) view.findViewById(R.id.roundProgressBar);
        test_iv = (PhotoView) view.findViewById(R.id.test_iv);
        mImageView = (ImageView) view.findViewById(R.id.large_image);
        //加载本地文件如项目中assets下文件
        File file = ImageLoader.getInstance().getDiskCache().get(mImageUrl);
        if (file != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            if (bitmap != null) {
                test_iv.setImageBitmap(bitmap);
                progressbar.setVisibility(View.GONE);
                mImageView.setVisibility(View.GONE);
                test_iv.setVisibility(View.VISIBLE);
                mAttacher = new PhotoViewAttacher(test_iv);
                //设置缩放
                mAttacher.setZoomable(true);
                mAttacher.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        show();
                        return false;
                    }
                });
            } else {
                progressbar.setVisibility(View.VISIBLE);
                mImageView.setVisibility(View.VISIBLE);
                test_iv.setVisibility(View.GONE);
                /*Animation animation;
                animation = AnimationUtils.loadAnimation(context, R.anim.en);
                img.startAnimation(animation);*/
                if (TextUtils.isEmpty(mImageUrl)) {
                    progressbar.setVisibility(View.GONE);
                } else {
                    //显示图片的配置
                    DisplayImageOptions options = new DisplayImageOptions.Builder()
                            //*.showImageOnLoading(R.drawable.logo)
                            .showImageOnFail(R.drawable.logo)
                            .cacheInMemory(true)
                            .cacheOnDisk(true)
                            .bitmapConfig(Bitmap.Config.RGB_565)
                            .build();
                    ImageLoader.getInstance().displayImage(mImageUrl, mImageView, options, new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            //开始加载的时候执行
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            //加载失败的时候执行
                            progressbar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            //加载成功的时候执行
                            progressbar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadingCancelled(String imageUri, View view) {
                            //加载取消的时候执行
                        }
                    }, new ImageLoadingProgressListener() {
                        @Override
                        public void onProgressUpdate(String imageUri, View view, int current, int total) {
                            //在这里更新 ProgressBar的进度信息
                            //设置进度条图片的总大小
                            progressbar.setMax(total);
                            // 设置当前加载进度
                            progressbar.setProgress(current);
                            if (current == total) {
                                progressbar.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }
        }
        return view;
    }

    private void show() {
        ActionSheetDialog actionSheetDialog = new ActionSheetDialog(getActivity())
                .builder()
                .setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .addSheetItem("保存到相册", ActionSheetDialog.SheetItemColor.Blue,
                        new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                saveImageToGallery(getActivity(), mImageUrl);
                            }
                        })
                .addSheetItem("分享到", ActionSheetDialog.SheetItemColor.Blue,
                        new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                shareSingleImage(mImageUrl);
                            }
                        });
        actionSheetDialog.show();
    }

    public static void saveImageToGallery(Context context, String url) {
        File file1 = ImageLoader.getInstance().getDiskCache().get(url);
        if (file1 == null) return;
        Bitmap bmp = BitmapFactory.decodeFile(file1.getAbsolutePath());
        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory(), "有约");
        if (!appDir.exists()) appDir.mkdir();
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String path = file.getAbsolutePath();
        // 最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
        ShowToast.normalShow(context, "保存成功!", true);
    }

    //分享单张图片
    public void shareSingleImage(String url) {
        //mImageLoader.getMemoryCache().get(url)
        //你当前长按的图片一定是在内存中的，要不怎么会在屏幕上显示嘛，所以用这个方法应该是可以的，
        // 如果是要获取硬盘上的，则是getDiskCache
        File file = ImageLoader.getInstance().getDiskCache().get(url);
        if (file == null) return;
        //由文件得到uri
        Uri imageUri = Uri.fromFile(file);
        //Log.d("share", "uri:" + imageUri);  //输出：file:///storage/emulated/0/test.jpg

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.setType("image/*");
        startActivity(Intent.createChooser(shareIntent, "分享到"));
    }

}

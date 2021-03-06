package com.e7yoo.e7.util;

import android.app.Activity;
import android.app.Dialog;
//import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.Target;
import com.e7yoo.e7.R;
import com.e7yoo.e7.adapter.GridAdapter;
import com.e7yoo.e7.model.AppConfigs;
import com.e7yoo.e7.model.GridItem;
import com.e7yoo.e7.model.GridItemClickListener;
import com.e7yoo.umeng.UmengUtils;
import com.sdsmdg.tastytoast.TastyToast;
import com.tencent.bugly.crashreport.CrashReport;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;

import java.io.File;
import java.util.ArrayList;
//import java.util.HashMap;
import java.util.concurrent.ExecutionException;

//import cn.jiguang.share.android.api.JShareInterface;
//import cn.jiguang.share.android.api.PlatActionListener;
//import cn.jiguang.share.android.api.Platform;
//import cn.jiguang.share.android.api.ShareParams;
//import cn.jiguang.share.qqmodel.QQ;
//import cn.jiguang.share.qqmodel.QZone;
//import cn.jiguang.share.wechat.Wechat;
//import cn.jiguang.share.wechat.WechatMoments;
//import cn.jiguang.share.weibo.SinaWeibo;

public class ShareDialogUtil {
    public static final String SHARE_URL = "http://a.app.qq.com/o/simple.jsp?pkgname=com.e7yoo.e7";//"http://itlao5.com/wp/app";
    public static final String SHARE_TITLE = "闲暇时光·【小萌伴】陪你";
    public static final String SHARE_CONTENT = "陪聊·段子·小游戏···还能帮你找手机";
    public static final String SHARE_IMAGEPATH = null;
    private static String share_url = SHARE_URL;
    private static String share_title = SHARE_TITLE;
    private static String share_content = SHARE_CONTENT;
    private static String share_imagePath = SHARE_IMAGEPATH;

    private static Dialog dialog;
    private static Activity context;

    public static void show(Activity act, String url, String title, String content, String iamgePath) {
        show(act);
        share_url = TextUtils.isEmpty(url) ? AppConfigsUtil.getShareUrl(SHARE_URL) : url;
        share_title = TextUtils.isEmpty(title) ? SHARE_TITLE : title;
        share_content = TextUtils.isEmpty(content) ? SHARE_CONTENT : content;
        share_imagePath = TextUtils.isEmpty(iamgePath) ? SHARE_IMAGEPATH : iamgePath;
    }

    public static void show(Activity act) {
        share_url = AppConfigsUtil.getShareUrl(SHARE_URL);
        share_title = SHARE_TITLE;
        share_content = SHARE_CONTENT;
        share_imagePath = SHARE_IMAGEPATH;
        if(ShareDialogUtil.context != act || dialog == null) {
            ShareDialogUtil.context = act;
            dialog = new Dialog(act, R.style.ShareDialogStyle);
            //填充对话框的布局
            View view = LayoutInflater.from(act).inflate(R.layout.activity_share, null);
            initDialog(view);
        }
        dialog.show();//显示对话框
    }

    public static void dismiss() {
        try {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void release() {
        dismiss();
        dialog = null;
        context = null;
    }

    private static void initDialog(View view) {
        initView(view);
        //将布局设置给Dialog
        dialog.setContentView(view);
        //获取当前Activity所在的窗体
        Window dialogWindow = dialog.getWindow();
        //设置Dialog从窗体底部弹出
        dialogWindow.setGravity(Gravity.BOTTOM);
        //获得窗体的属性
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.y = 20;//设置Dialog距离底部的距离
        // 将属性设置给窗体
        dialogWindow.setAttributes(lp);
    }

    private static void initView(View view) {
        GridView gridView = view.findViewById(R.id.share_gv);
        GridAdapter mAdapter = new GridAdapter(gridView.getContext(), getDatas(), true);
        if(CommonUtil.getWindowsWidth(context) > 720) {
            gridView.setNumColumns(5);
        }
        gridView.setAdapter(mAdapter);
    }

    private static ArrayList<GridItem> getDatas() {
        ArrayList<GridItem> items = new ArrayList<>();
        items.add(new GridItem(R.mipmap.share_wechat, R.string.share_to_wx, gridItemClickListener));
        items.add(new GridItem(R.mipmap.share_wxcircle, R.string.share_to_circle, gridItemClickListener));
        items.add(new GridItem(R.mipmap.share_qq, R.string.share_to_qq, gridItemClickListener));
        items.add(new GridItem(R.mipmap.share_qzone, R.string.share_to_qzone, gridItemClickListener));
        items.add(new GridItem(R.mipmap.share_sina, R.string.share_to_sina, gridItemClickListener));
        return items;
    }

    private static GridItemClickListener gridItemClickListener = new GridItemClickListener() {
        @Override
        public void onGridItemClick(int i, GridItem item) {
            if(item == null) {
                return;
            }

            SHARE_MEDIA media = null;
            String title = share_title;
//            String name = null;
//            ShareParams shareParams = new ShareParams();
            switch (item.getTextResId()) {
                case R.string.share_to_wx:
//                    name = Wechat.Name;
//                    shareParams.setTitle(share_title);
//                    shareParams.setText(share_content);
                    media = SHARE_MEDIA.WEIXIN;
                    break;
                case R.string.share_to_circle:
                    // 没有text
//                    name = WechatMoments.Name;
//                    shareParams.setTitle(share_content);
                    media = SHARE_MEDIA.WEIXIN_CIRCLE;
                    title = share_content;
                    break;
                case R.string.share_to_qq:
//                    name = QQ.Name;
//                    shareParams.setTitle(share_title);
//                    shareParams.setText(share_content);
                    media = SHARE_MEDIA.QQ;
                    break;
                case R.string.share_to_qzone:
//                    name = QZone.Name;
//                    shareParams.setTitle(share_title);
//                    shareParams.setText(share_content);
                    media = SHARE_MEDIA.QZONE;
                    break;
                case R.string.share_to_sina:
                    // 没有title
//                    name = SinaWeibo.Name;
//                    shareParams.setText(share_content);
                    media = SHARE_MEDIA.SINA;
                    break;
            }
//            if(name != null) {
//                shareParams.setShareType(Platform.SHARE_WEBPAGE);
//                shareParams.setUrl(share_url);//必须
//                setShareImg(name, shareParams);
//                share(name, shareParams);
//            }

            if(media != null) {
                //UMImage image = new UMImage(context, share_imagePath);
                UMImage image = getUMImage();
                UmengUtils.share(context, media, share_url, title, share_content, image, shareListener);
            }

            dismiss();
        }
    };

    private static UMImage getUMImage() {
        if(SHARE_IMAGE_PATH_TAKE_SCREENSHOT.equals(share_imagePath)) {
            if(context instanceof Activity) {
                Bitmap bitmap = BitmapUtils.takeScreenShot((Activity) context);
                if(bitmap != null) {
                    return new UMImage(context, bitmap);
                }
            }
            share_imagePath = null;
        }
        String imagePath = TextUtils.isEmpty(share_imagePath) ? getImagePath() : share_imagePath;
        if(TextUtils.isEmpty(imagePath)) {
            return new UMImage(context, "http://e7yoo.com/apk/logo_share.png");
        } else {
            if(share_imagePath.startsWith("http")) {
                return new UMImage(context, share_imagePath);
            } else {
                return new UMImage(context, new File(share_imagePath));
            }
        }
    }

    public static final String SHARE_IMAGE_PATH_TAKE_SCREENSHOT = "ScreenShot";
//    private static void setShareImg(String name, ShareParams shareParams) {
//        if(SHARE_IMAGE_PATH_TAKE_SCREENSHOT.equals(share_imagePath)) {
//            if(context instanceof Activity) {
//                Bitmap bitmap = BitmapUtils.takeScreenShot((Activity) context);
//                if(bitmap != null) {
//                    shareParams.setImageData(bitmap);
//                    return;
//                }
//            }
//            share_imagePath = null;
//        }
//        String imagePath = TextUtils.isEmpty(share_imagePath) ? getImagePath() : share_imagePath;
//        if(TextUtils.isEmpty(imagePath)) {
//            if(name.equals(Wechat.Name) || name.equals(WechatMoments.Name)) {
//                shareParams.setImageData(BitmapFactory.decodeResource(context.getResources(), R.mipmap.logo_share));
//            } else {
//                shareParams.setImageUrl("http://e7yoo.com/apk/logo_share.png");
//            }
//        } else {
//            if(share_imagePath.startsWith("http")) {
//                if(name.equals(Wechat.Name) || name.equals(WechatMoments.Name)) {
//                    shareParams.setImageData(BitmapFactory.decodeResource(context.getResources(), R.mipmap.logo_share));
//                } else {
//                    shareParams.setImageUrl(share_imagePath);
//                }
//            } else {
//                shareParams.setImagePath(imagePath);
//            }
//        }
//    }

//    private static void share(final String name, ShareParams shareParams) {
//        JShareInterface.share(name, shareParams, new PlatActionListener() {
//            @Override
//            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
//
//                Logs.isDebug();
//            }
//
//            @Override
//            public void onError(Platform platform, int i, int i1, Throwable throwable) {
//                Logs.isDebug();
//                if(name != null) {
//                    if(context != null) {
//                        String text = "QQ";
//                        if (name.equals(QQ.Name)) {
//                            text = context.getString(R.string.share_to_qq);
//                        } else if (name.equals(QZone.Name)) {
//                            text = context.getString(R.string.share_to_qq);
//                        } else if (name.equals(Wechat.Name)) {
//                            text = context.getString(R.string.share_to_wx);
//                        } else if (name.equals(WechatMoments.Name)) {
//                            text = context.getString(R.string.share_to_wx);
//                        } else {
//                            text = context.getString(R.string.share_to_sina);
//                        }
//                        if(context instanceof Activity) {
//                            final String finalText = text;
//                            ((Activity) context).runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    TastyToastUtil.toast(context, R.string.share_failed, finalText);
//                                }
//                            });
//                        }
//                    }
//                }
//                CrashReport.postCatchedException(throwable);
//            }
//
//            @Override
//            public void onCancel(Platform platform, int i) {
//                Logs.isDebug();
//
//            }
//        });
//    }

    private static UMShareListener shareListener = new UMShareListener() {
        /**
         * @descrption 分享开始的回调
         * @param platform 平台类型
         */
        @Override
        public void onStart(SHARE_MEDIA platform) {

        }

        /**
         * @descrption 分享成功的回调
         * @param platform 平台类型
         */
        @Override
        public void onResult(SHARE_MEDIA platform) {
        }

        /**
         * @descrption 分享失败的回调
         * @param platform 平台类型
         * @param throwable 错误原因
         */
        @Override
        public void onError(SHARE_MEDIA platform, Throwable throwable) {
            if(platform != null) {
                if(context != null) {
                    String text = "QQ";
                    if (platform == SHARE_MEDIA.QQ) {
                        text = context.getString(R.string.share_to_qq);
                    } else if (platform == SHARE_MEDIA.QZONE) {
                        text = context.getString(R.string.share_to_qq);
                    } else if (platform == SHARE_MEDIA.WEIXIN) {
                        text = context.getString(R.string.share_to_wx);
                    } else if (platform == SHARE_MEDIA.WEIXIN_CIRCLE) {
                        text = context.getString(R.string.share_to_wx);
                    } else {
                        text = context.getString(R.string.share_to_sina);
                    }
                    if(context instanceof Activity) {
                        final String finalText = text;
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TastyToastUtil.toast(context, R.string.share_failed, finalText);
                            }
                        });
                    }
                }
            }
            CrashReport.postCatchedException(throwable);
        }

        /**
         * @descrption 分享取消的回调
         * @param platform 平台类型
         */
        @Override
        public void onCancel(SHARE_MEDIA platform) {

        }
    };

    private static String getImagePath() {
        try {
            String filePath = FileUtil.getFilePath(context, "share.png");
            if(FileUtil.isFileExists(context, "share.png")) {
                return filePath;
            } else {
                boolean result = FileUtil.copyFromAssetsToSdcard(true, "log_share.png", filePath);
                if(result) {
                    return filePath;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 需要在子线程
     * @param url
     * @return
     */
    private static String getImagePathFromGlideCache(String url) {
        FutureTarget<File> futureTarget = Glide.with(context).load(url).downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
        try {
            File cacheFile = futureTarget.get();
            String path = cacheFile.getAbsolutePath();
            return path;
        } catch (InterruptedException e) {
            e.printStackTrace();
            CrashReport.postCatchedException(e);
        } catch (ExecutionException e) {
            e.printStackTrace();
            CrashReport.postCatchedException(e);
        } catch (Throwable e) {
            e.printStackTrace();
            CrashReport.postCatchedException(e);
        }
        return null;
    }

    public static void evaluate(Activity activity) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + activity.getPackageName()));
            activity.startActivity(intent);
        } catch (Exception e) {
            TastyToast.makeText(activity, activity.getString(R.string.about_evaluate_error), TastyToast.LENGTH_SHORT,
                    TastyToast.WARNING);
            CrashReport.postCatchedException(new Exception("evaluate no app shop"));
        }
    }
}

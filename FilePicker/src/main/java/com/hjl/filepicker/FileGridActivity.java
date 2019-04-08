package com.hjl.filepicker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.hjl.filepicker.bean.FileItem;
import com.hjl.filepicker.bean.MessageEvent;
import com.hjl.filepicker.fragment.FilesFragment;
import com.hjl.filepicker.fragment.fragmentAdapter;
import com.hjl.filepicker.ui.BaseActivity;
import com.hjl.filepicker.view.CustomViewPager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FileGridActivity extends BaseActivity {
    private static String TAG = "FileGridActivity";

    private Activity mActivity;

    //tab

    protected List<String> mTitles = new ArrayList<>();
    protected List<Fragment> mFragments = new ArrayList<>();
    protected fragmentAdapter mAdapter;
    //tab

    private FilesFragment mFragment1;
    private FilesFragment mFragment2;
    private FilesFragment mFragment3;
    private FilesFragment mFragment4;


    private ImageView mBtnBack;
    private TextView mTvDes;
    private Button mBtnOk;
    private TabLayout mTabLayout;
    private CustomViewPager mViewPager;


    private void initFindViewById() {
        mBtnBack = (ImageView) findViewById(R.id.btn_back);
        mTvDes = (TextView) findViewById(R.id.tv_des);
        mBtnOk = (Button) findViewById(R.id.btn_ok);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mViewPager = (CustomViewPager) findViewById(R.id.view_pager);


    }

    @Override
    public int intiLayout() {
        //设置子类的布局
        return R.layout.activity_file_grid;
    }

    @Override
    public void initView() {
        EventBus.getDefault().register(this);
        mActivity = FileGridActivity.this;
        initFindViewById();
    }

    @Override
    protected void initData() {
        initImage();
        initBtnOk();
    }

    @Override
    protected void setEvent() {
        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FilePicker.getInstance().mSelectedFiles.size() > 0) {
                    Intent intent = new Intent();
                    intent.putExtra(FilePicker.EXTRA_RESULT_FILES, FilePicker.getInstance().mSelectedFiles);
                    setResult(FilePicker.RESULT_CODE_FILES, intent);   //
                    finish();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void callBack(MessageEvent messageEvent) {
        if (messageEvent.getCode() == 1) {
            //选择事件
            initBtnOk();
        }

    }

    private void initBtnOk() {
        if (FilePicker.getInstance().mSelectedFiles.size() > 0) {
            mBtnOk.setText(getString(R.string.ip_select_complete, FilePicker.getInstance().mSelectedFiles.size(), FilePicker.getInstance().selectLimit));
            mBtnOk.setEnabled(true);
            mBtnOk.setTextColor(mActivity.getResources().getColor(R.color.ip_text_primary_inverted));
        } else {
            mBtnOk.setText(getString(R.string.ip_complete));
            mBtnOk.setEnabled(false);
        }
    }

    //doc,ppt,xls,pdf
    private void initTabView() {
        if(mAdapter == null ){
            mTitles.add("doc");
            mTitles.add("ppt");
            mTitles.add("xls");
            mTitles.add("pdf");
            mFragment1 = FilesFragment.indexInstance(mTitles.get(0));
            mFragment2 = FilesFragment.indexInstance(mTitles.get(1));
            mFragment3 = FilesFragment.indexInstance(mTitles.get(2));
            mFragment4 = FilesFragment.indexInstance(mTitles.get(3));
            mFragments.add(mFragment1);
            mFragments.add(mFragment2);
            mFragments.add(mFragment3);
            mFragments.add(mFragment4);

            mAdapter = new fragmentAdapter(getSupportFragmentManager(), mFragments, mTitles);
            mViewPager.setAdapter(mAdapter);
            mViewPager.setOffscreenPageLimit(mFragments.size() - 1);//设置缓存所有
            //禁止滑动
            //mViewPager.setScanScroll(false);
            //将tabLayout与viewpager连起来
            mTabLayout.setupWithViewPager(mViewPager);

            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    System.out.println("页面=" + position);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);

                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }

    }

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                initTabView();
                //mAdapter.notifyDataSetChanged();
            }
        }
    };

    private final String[] FILE_PROJECTION = {     //查询图片需要的数据列
            MediaStore.Files.FileColumns.DISPLAY_NAME,   //图片的显示名称  aaa.jpg
            MediaStore.Files.FileColumns.DATA,           //图片的真实路径  /storage/emulated/0/pp/downloader/wallpaper/aaa.jpg
            MediaStore.Files.FileColumns.SIZE,           //图片的大小，long型  132492
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.DATE_ADDED
    };    //图片被添加的时间，long型  1450518608


    private void initImage() {
        //由于扫描图片是耗时的操作，所以要在子线程处理。
        new Thread(new Runnable() {
            @Override
            public void run() {

//                结果：
//                doc :: application/msword
//                docx :: application/vnd.openxmlformats-officedocument.wordprocessingml.document
//                ppt :: application/vnd.ms-powerpoint
//                pptx :: application/vnd.openxmlformats-officedocument.presentationml.presentation
//                xlsx :: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
//                xls :: application/vnd.ms-excel
//                pdf :: application/pdf
//                txt :: text/plain
//                rar :: application/rar
//                zip :: application/zip


                //String[] selectionArgs = new String[]{"text/plain", "application/msword", "application/pdf", "application/vnd.ms-powerpoint", "application/vnd.ms-excel"};

                FilePicker.getInstance().clearList();

               /* String[] selectionArgs = new String[]{
                        "application/msword",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "application/pdf",
                        "application/vnd.ms-powerpoint",
                        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                        "application/vnd.ms-excel",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                };*/
                String[] selectionArgs = FilePicker.getArgs("doc,ppt,xls,pdf");
                //相当于我们常用sql where 后面的写法
                String selection = MediaStore.Files.FileColumns.MIME_TYPE + "= ? ";
                for (int i = 0; i < selectionArgs.length; i++) {
                    selection = selection + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ? ";
                }


                Cursor cursor = getContentResolver().query(
                        MediaStore.Files.getContentUri("external"),
                        FILE_PROJECTION,
                        selection, selectionArgs, FILE_PROJECTION[4] + " DESC");

                while (cursor.moveToNext()) {

                    // 获取图片的路径
                    //查询数据
                    //String imageName = cursor.getString(cursor.getColumnIndexOrThrow(FILE_PROJECTION[0]));

                    String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(FILE_PROJECTION[1]));
                    String imageName = getFileName(imagePath);

                    File file = new File(imagePath);
                    if (!file.exists() || file.length() <= 0) {
                        continue;
                    }
                    //获取图片的生成日期
                    long imageSize = cursor.getLong(cursor.getColumnIndexOrThrow(FILE_PROJECTION[2]));
                    String imageMimeType = cursor.getString(cursor.getColumnIndexOrThrow(FILE_PROJECTION[3]));
                    Long imageAddTime = cursor.getLong(cursor.getColumnIndexOrThrow(FILE_PROJECTION[4]));


                    Log.d(TAG, "************************************");
                    Log.d(TAG, "imageName: " + imageName);
                    Log.d(TAG, "imagePath: " + imagePath);
                    Log.d(TAG, "imageSize: " + imageSize);
                    Log.d(TAG, "imageMimeType: " + imageMimeType);
                    Log.d(TAG, "文件生成日期: " + imageAddTime);
                    Log.d(TAG, "************************************");


                    //封装实体
                    FileItem fileItem = new FileItem();
                    fileItem.name = imageName;
                    fileItem.path = imagePath;
                    fileItem.size = imageSize;
                    fileItem.mimeType = imageMimeType;
                    fileItem.addTime = imageAddTime;
                    FilePicker.getInstance().mAllFiles.add(fileItem);

                    if (imageName.indexOf("doc") != -1 || imageName.indexOf("docx") != -1) {
                        FilePicker.getInstance().docList.add(fileItem);
                    }
                    if (imageName.indexOf("ppt") != -1 || imageName.indexOf("pptx") != -1) {
                        FilePicker.getInstance().pptList.add(fileItem);
                    }
                    if (imageName.indexOf("xls") != -1 || imageName.indexOf("xlsx") != -1) {
                        FilePicker.getInstance().xlsList.add(fileItem);
                    }
                    if (imageName.indexOf("pdf") != -1) {
                        FilePicker.getInstance().pdfList.add(fileItem);
                    }
                }


                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
                cursor.close();

            }
        }).start();

    }

   /* public String getFileName(String pathandname) {

        int start = pathandname.lastIndexOf("/");
        int end = pathandname.lastIndexOf(".");
        if (start != -1 && end != -1) {
            return pathandname.substring(start + 1, end);
        } else {
            return null;
        }

    }*/

    public String getFileName(String pathandname) {

        int start = pathandname.lastIndexOf("/");
        if (start != -1) {
            return pathandname.substring(start + 1, pathandname.length());
        } else {
            return null;
        }

    }
}
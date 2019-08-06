package com.cc.appstudio.videocompressor;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.cc.appstudio.videocompressor.services.VideoCompressService;
import com.cc.appstudio.videocompressor.utilities.FileUtils;

import java.io.File;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final int COMPRESS_FILE_SELECTION_RQ = 6669;
    private static final int PERMISSION_RQ_GALLERY = 80;
    private File saveDir = null;

    public static final String ROOT_FOLDER_NAME = "VideoCompressor";
    public static final String SUB_FOLDER_NAME = ROOT_FOLDER_NAME
            + File.separator + "Compressed Files";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        saveDir = FileUtils.createFileDir(this, ROOT_FOLDER_NAME);
        if (saveDir.exists()) {
            FileUtils.createFileDir(this, SUB_FOLDER_NAME);
        }

    }

    @OnClick(R.id.btn_select_compress_video)
    public void selectVideoFromGallery(View view) {
        openGalleryIntent();
    }

    //endregion

    //region Helper methods for Permission request and callbacks
    @AfterPermissionGranted(PERMISSION_RQ_GALLERY)
    public void openGalleryIntent() {
        String perm = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if (!EasyPermissions.hasPermissions(this, perm)) {
            EasyPermissions.requestPermissions(this,
                    "Need external permission " + "for accessing Gallery",
                    PERMISSION_RQ_GALLERY, perm);
        } else {
            Intent intent = new Intent();
            intent.setType("video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, COMPRESS_FILE_SELECTION_RQ);
            }
        }
    }

    //region override methods
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {

        } else if (requestCode == COMPRESS_FILE_SELECTION_RQ) {
            if (resultCode == RESULT_OK) {
                final File file = FileUtils.getFileFromUri(this, data.getData());
                startCompressServiceForVideo(file.getAbsolutePath());
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    private void startCompressServiceForVideo(String filePath) {
        Bundle bundle = new Bundle();
        bundle.putString("filePath", filePath);
        Intent msgIntent = new Intent(this, VideoCompressService.class);
        msgIntent.putExtras(bundle);
        startService(msgIntent);
    }
}

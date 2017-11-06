package com.kulsdroid.custompdfviewer.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kulsdroid.custompdfviewer.R;
import com.kulsdroid.custompdfviewer.customClass.TouchImageView;

import java.io.File;
import java.io.IOException;

public class PDFActivity extends Activity implements View.OnClickListener, View.OnTouchListener {

    private static final String TAG = PDFActivity.class.getSimpleName();

    private Context mContext;
    private Activity mActivity;

    private TouchImageView imgPdfView;
    private Button btnGoTo;
    private ParcelFileDescriptor fileDescriptor;
    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page currentPage;
    private TextView txtTotalPage;
    private ImageView imgSearch;
    private ImageView imgClose;
    private EditText etPageNumber;
    private ImageView imgBack, imgNext;

    private LinearLayout linearSearch, linearButton;
    private int intSearchIndex;
    private int index;
    private GestureDetector mGestureDetector;

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    public static final int MY_PERMISSIONS_REQUEST_WRITE_CALENDAR = 123;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pdf);
        initialization();

        if (checkPermission()) {
            openRenderer();
        }


    }

    private void initialization() {

        mContext = PDFActivity.this;
        mActivity = PDFActivity.this;

        imgBack = findViewById(R.id.imgBack);
        imgNext = findViewById(R.id.imgNext);
        btnGoTo = (Button) findViewById(R.id.btnGoTo);

        txtTotalPage = (TextView) findViewById(R.id.txtTotalPage);

        imgPdfView = (TouchImageView) findViewById(R.id.pdfView);
        imgSearch = (ImageView) findViewById(R.id.imgSearch);
        imgClose = (ImageView) findViewById(R.id.imgClose);

        etPageNumber = (EditText) findViewById(R.id.etPageNumber);

        linearButton = (LinearLayout) findViewById(R.id.layoutButton);
        linearSearch = (LinearLayout) findViewById(R.id.linearSearch);
        linearSearch.setVisibility(View.GONE);
        linearButton.setVisibility(View.VISIBLE);

        btnGoTo.setOnClickListener(this);
        imgNext.setOnClickListener(this);
        imgBack.setOnClickListener(this);
        imgSearch.setOnClickListener(this);
        imgClose.setOnClickListener(this);
        mGestureDetector = new GestureDetector(mContext, new GestureListener());
        imgPdfView.setOnTouchListener(this);

        index = 0;

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void openRenderer() {

        String s = Environment.getExternalStorageDirectory() + "/.mycc/_pdfViewerTest2";
        File file = new File(s);


        try {
            fileDescriptor = ParcelFileDescriptor.open(
                    file, ParcelFileDescriptor.MODE_READ_ONLY);
            pdfRenderer = new PdfRenderer(fileDescriptor);
            showPage(index);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void closeRenderer() {
        if (null != currentPage) {
            currentPage.close();
        }
        pdfRenderer.close();
        try {
            fileDescriptor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void showPage(int index) {
        if (pdfRenderer.getPageCount() <= index) {
            return;
        }
        if (null != currentPage) {
            currentPage.close();
        }
        currentPage = pdfRenderer.openPage(index);
        Bitmap bitmap = Bitmap.createBitmap(currentPage.getWidth(), currentPage.getHeight(),
                Bitmap.Config.ARGB_8888);
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        imgPdfView.setImageBitmap(bitmap);
        updateUIData();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void updateUIData() {
        imgPdfView.setZoom(1);
        int index = currentPage.getIndex();
        int pageCount = pdfRenderer.getPageCount();
        imgBack.setEnabled(0 != index);
        imgNext.setEnabled(index + 1 < pageCount);
        txtTotalPage.setText(index + " of " + pageCount);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgNext:
                showPage(currentPage.getIndex() + 1);
                break;
            case R.id.imgBack:
                showPage(currentPage.getIndex() - 1);
                break;
            case R.id.imgSearch:
                linearButton.setVisibility(View.GONE);
                linearSearch.setVisibility(View.VISIBLE);
                break;

            case R.id.imgClose:
                linearButton.setVisibility(View.VISIBLE);
                linearSearch.setVisibility(View.GONE);
                break;

            case R.id.btnGoTo:
                goToPage();
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void goToPage() {

        if (etPageNumber.getEditableText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter page number", Toast.LENGTH_SHORT).show();
        } else {
            intSearchIndex = Integer.parseInt(etPageNumber.getEditableText().toString());
            int pageCount = pdfRenderer.getPageCount();

            if (intSearchIndex <= pageCount) {
                showPage(intSearchIndex);
            } else {
                Toast.makeText(this, "Page number does not exists", Toast.LENGTH_SHORT).show();
            }

        }

    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (imgPdfView.getCurrentZoom() == 1) {
            mGestureDetector.onTouchEvent(event);
        }

        return true;

    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                int pageCount = pdfRenderer.getPageCount();
                if (currentPage.getIndex() + 1 < pageCount)
                    imgNext.performClick();
                return false; // Right to left
            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                if ((0 != currentPage.getIndex()))
                    imgBack.performClick();
                return false; // Left to right
            }

            return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public boolean checkPermission() {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("Read storage permission is necessary to read pdf!!!");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_CALENDAR);
                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                } else {
                    ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_CALENDAR);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_CALENDAR:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openRenderer();
                } else {
                    finish();
                }
                break;
        }
    }
}

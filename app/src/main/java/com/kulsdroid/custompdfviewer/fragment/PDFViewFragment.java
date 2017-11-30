package com.kulsdroid.custompdfviewer.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * Created by KulsDroid on 11/8/2017.
 */

public class PDFViewFragment extends Fragment implements View.OnClickListener, View.OnTouchListener {

    View mView;
    private Context mContext;
    private Activity mActivity;
    private LinearLayout linearPageIndication;
    private LinearLayout linearControls;
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
    private float scale;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_pdf_view, container, false);
        initialization();
        openRenderer();
        return mView;
    }

    private void initialization() {

        mContext = getActivity();
        mActivity = getActivity();

        scale = getContext().getResources().getDisplayMetrics().density;

        imgBack = mView.findViewById(R.id.imgBack);
        imgNext = mView.findViewById(R.id.imgNext);
        btnGoTo = (Button) mView.findViewById(R.id.btnGoTo);

        txtTotalPage = (TextView) mView.findViewById(R.id.txtTotalPage);

        imgPdfView = (TouchImageView) mView.findViewById(R.id.pdfView);
        imgSearch = (ImageView) mView.findViewById(R.id.imgSearch);
        imgClose = (ImageView) mView.findViewById(R.id.imgClose);

        etPageNumber = (EditText) mView.findViewById(R.id.etPageNumber);

        linearControls = mView.findViewById(R.id.linearControls);
        linearPageIndication = mView.findViewById(R.id.linearPageIndication);
        linearButton = (LinearLayout) mView.findViewById(R.id.layoutButton);
        linearSearch = (LinearLayout) mView.findViewById(R.id.linearSearch);
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

//        hideControls();

    }


    /**
     * You can hide your controls
     */
    private void hideControls() {
        linearControls.setVisibility(View.GONE);
        linearPageIndication.setVisibility(View.GONE);
    }

    /**
     * This function will setup PDF file
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void openRenderer() {

        //This is your file path. You need to set your pathfile over here.
        String s = Environment.getExternalStorageDirectory() + "/.mycc/bii_30bigtechpredictions_2017";
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
    private void showPage(int index) {
        if (pdfRenderer.getPageCount() <= index) {
            return;
        }
        if (null != currentPage) {
            currentPage.close();
        }
        currentPage = pdfRenderer.openPage(index);
        Bitmap bitmap = Bitmap.createBitmap(
                getResources().getDisplayMetrics().densityDpi * currentPage.getWidth() / 72,
                getResources().getDisplayMetrics().densityDpi * currentPage.getHeight() / 72,
                Bitmap.Config.ARGB_8888);
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        imgPdfView.setImageBitmap(bitmap);
        updateUIData();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
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




    /**
     * This function updates UI
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void updateUIData() {
        imgPdfView.setZoom(1);
        int index = currentPage.getIndex();
        int pageCount = pdfRenderer.getPageCount();
        imgBack.setEnabled(0 != index);
        imgNext.setEnabled(index + 1 < pageCount);
        txtTotalPage.setText((index + 1) + " of " + pageCount);
    }

    /**
     * This function moves to particular page
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void goToPage() {

        if (etPageNumber.getEditableText().toString().isEmpty()) {
            Toast.makeText(mActivity, "Please enter page number", Toast.LENGTH_SHORT).show();
        } else {
            intSearchIndex = Integer.parseInt(etPageNumber.getEditableText().toString());
            int pageCount = pdfRenderer.getPageCount();

            if (intSearchIndex <= pageCount) {
                showPage(intSearchIndex);
            } else {
                Toast.makeText(mActivity, "Page number does not exists", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (imgPdfView.getCurrentZoom() == 1) {
            mGestureDetector.onTouchEvent(motionEvent);
        }
        return true;
    }

    /**
     * Swipe listener withe use of Gesture
     */
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

}

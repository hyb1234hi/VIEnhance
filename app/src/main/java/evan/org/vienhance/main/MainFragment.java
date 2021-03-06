package evan.org.vienhance.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.flyco.tablayout.SegmentTabLayout;
import com.flyco.tablayout.listener.OnTabSelectListener;
import evan.org.vienhance.R;
import evan.org.vienhance.domain.model.AlgContext;
import org.jetbrains.annotations.NotNull;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.text.DecimalFormat;
import java.util.HashMap;
import static evan.org.vienhance.domain.enhanceFilter.*;

/**
 * Create By yejiaquan in 2018/10/25 14:13
 */
public class MainFragment extends Fragment implements MainContract.View {

    private static final String TAG = "Main::Fragment";

    private MainContract.Presenter presenter = null;
    private CameraBridgeViewBase mOpenCvCameraView;

    private AlgContext context;

    private boolean isPos;

    public static MainFragment newInstance(){
        return new MainFragment();
    }

    @Override
    public boolean isActive() {
        return this.isAdded();
    }

    @Override
    public void setPresenter(@NotNull MainContract.Presenter presenter) {
        this.presenter = presenter;
    }


    private String[] mTitles_1 = {"原画", "灰度", "Laplace", "直方图", "伽马", "Face", "MSRCR"};

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.main_frag, container, false);

        mOpenCvCameraView = root.findViewById(R.id.surface_view);

        final LinearLayout l2 = root.findViewById(R.id.l2);
        l2.setVisibility(View.GONE);

        SegmentTabLayout tabLayout_1 = root.findViewById(R.id.tl_1);
        tabLayout_1.setTabData(mTitles_1);

        tabLayout_1.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                if (position == 2){
                    l2.setVisibility(View.VISIBLE);
                }else {
                    l2.setVisibility(View.GONE);
                }
                mOpenCvCameraView.setCvCameraViewListener(new MyCvCameraViewListener2(position));
            }

            @Override
            public void onTabReselect(int position) {
            }
        });

        FloatingActionButton mReverseButton = root.findViewById(R.id.reverse);
        SeekBar arg1SeekBar = root.findViewById(R.id.arg1);
        SeekBar arg2SeekBar = root.findViewById(R.id.arg2);
        SeekBar arg3SeekBar = root.findViewById(R.id.arg3);
        TextView txt1 = root.findViewById(R.id.txt1);
        TextView txt2 = root.findViewById(R.id.txt2);
        TextView txt3 = root.findViewById(R.id.txt3);

        arg1SeekBar.setOnSeekBarChangeListener(new MyOnSeekBarChangeListener(txt1, 0, -8, 8));
        arg2SeekBar.setOnSeekBarChangeListener(new MyOnSeekBarChangeListener(txt2, 1, -2, 2));
        arg3SeekBar.setOnSeekBarChangeListener(new MyOnSeekBarChangeListener(txt3, 2, -2, 2));

        arg1SeekBar.setProgress(81, true);
        arg2SeekBar.setProgress(25, true);
        arg3SeekBar.setProgress(25, true);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(new MyCvCameraViewListener2(ORIGINAL));
        mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);
        isPos = true;

        mReverseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reverse();
            }
        });
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }else{
            start();
        }
        presenter.start();
    }

    private void start(){
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, getContext(), mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                start();
            }else {
                // Permission Denied
                Toast.makeText(getContext(), getString(R.string.no_camera_permission), Toast.LENGTH_LONG).show();
                getActivity().finish();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(getContext()) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i(TAG, "CameraView loaded successfully");
                mOpenCvCameraView.enableView();
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    @Override
    public void show(Mat dst, Handler handler, int type) {
        if (context.isEnhance(type)) {
            Message m = new Message();
            m.obj = dst;
            handler.sendMessage(m);
        }
    }

//    private HashMap<String, Float> argsMap = new HashMap<>();
    private float[]argsmap = new float[3];

    @Override
    public void setContext(@NotNull AlgContext context) {
        this.context = context;
    }

    class MyOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener{

//        private String n;
        private int n;
        private int max;
        private int min;
        private TextView tv;

        MyOnSeekBarChangeListener(TextView tv, int n, int min, int max){
            this.n = n;
            this.min = min;
            this.max = max;
            this.tv = tv;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            float v = (float) i / 100 * (max - min);
//            argsMap.put(n, min + v);
            argsmap[n] = min + v;
            DecimalFormat df =new DecimalFormat("0.00");
            tv.setText(df.format(min + v));
            presenter.setArgs(argsmap);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    class MyCvCameraViewListener2 implements CameraBridgeViewBase.CvCameraViewListener2 {
        private int enh;

        private MyCvCameraViewListener2(int enh){
            this.enh = enh;
        }

        @Override
        public void onCameraViewStarted(int width, int height) {

        }

        @Override
        public void onCameraViewStopped() {

        }

        @Override
        public void onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame, Handler handler) {
            Mat mRgba = inputFrame.rgba();
            if (!isPos) Core.flip(mRgba, mRgba, 1);
            presenter.setRGBA(mRgba);
            presenter.getEnhance(enh, handler);
        }
    }

    @Override
    public void reverse() {
        mOpenCvCameraView.setVisibility(SurfaceView.INVISIBLE);
        if (isPos) {
            mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
            isPos = false;
        }else{
            mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);
            isPos = true;
        }
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
    }
}

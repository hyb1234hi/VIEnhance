#include <jni.h>
#include <string>
#include <opencv/cv.h>
#include <opencv/highgui.h>
#include "opencv2/face.hpp"
#include "MSRCR.cpp"
#include "FindFace.cpp"

using namespace cv;
using namespace std;

extern "C"
{
    JNIEXPORT void JNICALL Java_evan_org_vienhance_util_OpenCVNDKHelper_detectFeatures
        (JNIEnv *, jclass, jlong srcMatAddr, jlong dstMatAddr) {
        Mat& srcMat = *(Mat*)srcMatAddr;
        Mat& descriptors = *(Mat*)dstMatAddr;
        vector<double> sigma;
        vector<double> weight;
        for(int i = 0; i < 3; i++)
            weight.push_back(1./3);
        sigma.push_back(30);
        sigma.push_back(150);
        sigma.push_back(300);

        Msrcr msrcr;
        msrcr.MultiScaleRetinexCR(srcMat, descriptors, weight, sigma);
    }
}

// 加载人脸识别的级联分类器
CascadeClassifier cascadeClassifier;
extern "C"
JNIEXPORT void JNICALL
Java_com_darren_ndk_day05_FaceDetection_loadCascade(JNIEnv *env, jobject instance,
                                                    jstring filePath_) {
    const char *filePath = env->GetStringUTFChars(filePath_, 0);
    cascadeClassifier.load(filePath);
    env->ReleaseStringUTFChars(filePath_, filePath);
}

extern "C"
{
    JNIEXPORT void JNICALL Java_evan_org_vienhance_util_OpenCVNDKHelper_findFace
        (JNIEnv *, jclass, jlong srcMatAddr, jlong dstMatAddr) {
        Mat& srcMat = *(Mat*)srcMatAddr;
        Mat& descriptors = *(Mat*)dstMatAddr;
        FindFaces(srcMat, descriptors, cascadeClassifier);
    }
}

extern "C"
{
    JNIEXPORT void JNICALL Java_evan_org_vienhance_util_OpenCVNDKHelper_equalize
        (JNIEnv *, jclass, jlong srcMatAddr, jlong dstMatAddr) {
        Mat& srcMat = *(Mat*)srcMatAddr;
        Mat& descriptors = *(Mat*)dstMatAddr;
        Mat imageRGB[4];
        split(srcMat, imageRGB);
        for (int i = 0; i < 4; i++){
            equalizeHist(imageRGB[i], imageRGB[i]);
        }
        merge(imageRGB, 4, srcMat);
        descriptors = srcMat;
    }
}


extern "C"
{
    JNIEXPORT void JNICALL Java_evan_org_vienhance_util_OpenCVNDKHelper_lap
        (JNIEnv *, jclass, jlong srcMatAddr, jlong dstMatAddr, jfloat center, jfloat row, jfloat col) {
        Mat& srcMat = *(Mat*)srcMatAddr;
        Mat& descriptors = *(Mat*)dstMatAddr;
        Mat kernel = (Mat_<float>(3, 3) << 0, col, 0, row, center, row, 0, col, 0);
        filter2D(srcMat, descriptors, CV_8UC3, kernel);
    }
}


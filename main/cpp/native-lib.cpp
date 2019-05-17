#include <jni.h>
#include <string>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <iostream>
#include <numeric>
#include <stdio.h>
#include <stdlib.h>
#include "jni.h"

using namespace cv;
using namespace std;

extern "C" bool compareContourAreas(std::vector<cv::Point> contour1, std::vector<cv::Point> contour2) {

    double i = fabs(contourArea(cv::Mat(contour1)));
    double j = fabs(contourArea(cv::Mat(contour2)));
    return (i > j);
}

Mat MatCrop(Mat mat) {

    Mat &image = mat;
    Rect bounding_rect;

    Mat thr(image.rows, image.cols, CV_8UC1);
    cvtColor(image, thr, CV_BGR2GRAY); //Convert to gray
    threshold(thr, thr, 150, 255, THRESH_BINARY + THRESH_OTSU); //Threshold the gray

    vector<vector<Point> > contours; // Vector for storing contour
    vector<Vec4i> hierarchy;
    RotatedRect rect;

    findContours(thr, contours, hierarchy, CV_RETR_CCOMP,
                 CV_CHAIN_APPROX_SIMPLE); // Find the contours in the image

    sort(contours.begin(), contours.end(),
         compareContourAreas);            //Store the index of largest contour

    bounding_rect = boundingRect(contours[0]);
    rect = minAreaRect(contours[0]);

    // matrices we'll use
    Mat rot_mat, rotated;

    // get angle and size from the bounding box
    float angle = rect.angle;
    Size rect_size = rect.size;

    if (rect.angle < -45.) {
        angle += 90.0;
        swap(rect_size.width, rect_size.height);
    }

    rot_mat = getRotationMatrix2D(rect.center, angle, 1);
    warpAffine(image, rotated, rot_mat, image.size(), INTER_CUBIC);
    image = rotated;
    cvtColor(image, thr, CV_BGR2GRAY); //Convert to gray
    threshold(thr, thr, 150, 255, THRESH_BINARY + THRESH_OTSU); //Threshold the gray

    findContours(thr, contours, hierarchy, CV_RETR_CCOMP, CV_CHAIN_APPROX_SIMPLE);
    sort(contours.begin(), contours.end(), compareContourAreas);
    bounding_rect = boundingRect(contours[0]);
    image = Mat(image, bounding_rect);
    return image;
}

std::vector<cv::Rect> detectLetters(cv::Mat img)
{
    std::vector<cv::Rect> boundRect;
    cv::Mat img_gray, img_sobel, img_threshold, element;
    cvtColor(img, img_gray, CV_BGR2GRAY);
    cv::Sobel(img_gray, img_sobel, CV_8U, 1, 0, 3, 1, 0, cv::BORDER_DEFAULT);
    cv::threshold(img_sobel, img_threshold, 0, 255, CV_THRESH_OTSU+CV_THRESH_BINARY);
    element = getStructuringElement(cv::MORPH_RECT, cv::Size(50, 3) ); // Other option is 17, 3 or 30,30
    cv::morphologyEx(img_threshold, img_threshold, CV_MOP_CLOSE, element); //Does the trick
    std::vector< std::vector< cv::Point> > contours;
    cv::findContours(img_threshold, contours, 0, 1);
    std::vector<std::vector<cv::Point> > contours_poly( contours.size() );
    for( int i = 0; i < contours.size(); i++ )
        if (contours[i].size()>100)
        {
            cv::approxPolyDP( cv::Mat(contours[i]), contours_poly[i], 3, true );
            cv::Rect appRect( boundingRect( cv::Mat(contours_poly[i]) ));

            if (appRect.width>appRect.height)
                boundRect.push_back(appRect);
        }
    return boundRect;
}

bool IsMoreWhiteThanBlack(Mat mat){

    Mat  partROI;
    //cvtColor(mat, partROI, CV_BGR2GRAY);
    int count_white = 0;
    int count_black = 0;
    threshold( partROI, partROI, 200, 255, THRESH_BINARY );
    count_white = countNonZero(mat);
    count_black = mat.cols * mat.rows - count_white;
    return (count_white < count_black);
}


extern "C" JNIEXPORT jlongArray JNICALL
Java_com_mif_zxcrew_helpers_OpenCVHelper_boundingBoxes(JNIEnv *env, jobject instance, jlong matAddrRgba, jboolean drawRect, jboolean cardPos) {
    //cardPos = true; landscape
    //cardPos = false; portrait

    //Get mat out of the address given from Java
    Mat &img = *(Mat *) matAddrRgba;

    //crop
    img = MatCrop(img);
    Mat imgColor = img;
    //Detect
    vector<Rect> letterBBoxes = detectLetters(img);
    jsize size = letterBBoxes.size();

    // Additional Binarization
    Mat srcGray ;
    cvtColor(img, img, CV_BGR2GRAY);

    Mat finalMat;

    // Simple threshold is way better for black cards
    if(IsMoreWhiteThanBlack(img))
        adaptiveThreshold(img, img, 255, ADAPTIVE_THRESH_MEAN_C,THRESH_BINARY, 11, 12);
    else
        threshold(img, img, 170, 255, THRESH_BINARY + THRESH_OTSU);

    // Create the array
    jlongArray results;
    results = (*env).NewLongArray(size); //(*env).NewLongArray(letterBBoxes.size());

    // prepare a temporary holder
    jlong fill[size];

    //Display
    //Size addRect = Size(4,4);
    for(int i=0; i < letterBBoxes.size(); i++) {
        Mat *mat = new Mat(img, letterBBoxes[i]);

        // Increase border of mat with white empty edges
        copyMakeBorder(*mat, *mat, 4, 4, 8 ,8, BORDER_CONSTANT, cvScalar(0xff,0xff,0xff));

        //Noise removal
        //blur(*mat,*mat, Size(2,2), Point(-1,-1), BORDER_DEFAULT);
        //threshold(*mat, *mat, 127,255, THRESH_BINARY + THRESH_OTSU);

        fill[i] = reinterpret_cast<long>(mat);
        if(drawRect)
            rectangle(img, letterBBoxes[i], Scalar(0, 255, 0), 3, 8, 0);
    }

    (*env).SetLongArrayRegion(results, 0, size, fill);
    img = imgColor;
    return(results);

}


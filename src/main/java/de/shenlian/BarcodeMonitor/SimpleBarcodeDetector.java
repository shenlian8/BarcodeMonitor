/* 
 * Barcode detector based on ZXING and JAVACV
 */
 
package de.shenlian.BarcodeMonitor;

import com.google.zxing.*;
import com.google.zxing.client.j2se.*;
import com.google.zxing.common.*;
import com.google.zxing.multi.GenericMultipleBarcodeReader;
import java.awt.image.BufferedImage;
import org.bytedeco.javacpp.Loader;
import static org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.opencv_core.CvContour;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.CvPoint;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.IplImage;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import org.bytedeco.javacv.*;

/**
 * this is a very simple class to start a window. show the first camera
 * in the system. if barcode is detected, it should be marked and decoded
 * 
 * @author Lian Shen
 */
public class SimpleBarcodeDetector {
    public static void main(String[] args) throws Exception {
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        grabber.start();

        IplImage frame = grabber.grab();
        IplImage image = null;
        IplImage prevImage = null;
        IplImage diff = null;

        CanvasFrame canvasFrame = new CanvasFrame("Some Title");
        canvasFrame.setCanvasSize(frame.width(), frame.height());
                
        CvMemStorage storage = CvMemStorage.create();
        
        BufferedImage bufferedImage;
        
        String lastText = ""; 

        while (canvasFrame.isVisible() && (frame = grabber.grab()) != null) {
            cvClearMemStorage(storage);

//            cvSmooth(frame, frame, CV_GAUSSIAN, 9, 9, 2, 2);
            if (image == null) {
                image = IplImage.create(frame.width(), frame.height(), IPL_DEPTH_8U, 1);
                cvCvtColor(frame, image, CV_RGB2GRAY);
            } else {
                prevImage = IplImage.create(frame.width(), frame.height(), IPL_DEPTH_8U, 1);
                prevImage = image;
                image = IplImage.create(frame.width(), frame.height(), IPL_DEPTH_8U, 1);
                cvCvtColor(frame, image, CV_RGB2GRAY);
            }

            if (diff == null) {
                diff = IplImage.create(frame.width(), frame.height(), IPL_DEPTH_8U, 1);
            }

            if (prevImage != null) {
                // perform ABS difference
                cvAbsDiff(image, prevImage, diff);
                // do some threshold for wipe away useless details
                cvThreshold(diff, diff, 64, 255, CV_THRESH_BINARY);
                           
                bufferedImage = image.getBufferedImage();
//                File file = new File("D:\\JavaProjects\\200px-Wikipedia_mobile_en.svg.png");
//                bufferedImage = ImageIO.read(file);
                             
                LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                GenericMultipleBarcodeReader barcodeReader = new GenericMultipleBarcodeReader(new MultiFormatReader());
                Result[] results;
                try {
                    results = barcodeReader.decodeMultiple(bitmap);
                    // setting results.
                    for (Result oneResult : results) {
                      lastText = oneResult.getText();
                      for (ResultPoint resultPoint : oneResult.getResultPoints()) {
                        CvPoint ptPoit = cvPoint((int) resultPoint.getX(), (int) resultPoint.getY());
                        cvCircle(frame, ptPoit, 5, cvScalar(0, 255, 0, 0), 2, 4, 0);
                      }
                    }
                } catch (NotFoundException e) {
                  // e.printStackTrace();
                }
//                cvPutText(frame, lastText, cvPoint(0,frame.height()-20), cvFont(FONT_HERSHEY_PLAIN), cvScalar(0,255,0,0));
                // canvasFrame.showImage(diff);
                canvasFrame.showImage(frame);                

                // recognize contours
                CvSeq contour = new CvSeq(null);
                cvFindContours(diff, storage, contour, Loader.sizeof(CvContour.class), CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);
            }
        }
        grabber.stop();
        canvasFrame.dispose();
    }
}

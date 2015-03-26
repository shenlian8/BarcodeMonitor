/*
 * Copyright 2015 shenlian.de
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Please write me an E-mail(shenlian@hotmail.com), if you are using this project
 * Thanks!
 */

 
package de.shenlian.BarcodeMonitor;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.*;
import java.awt.image.BufferedImage;
import static org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.opencv_highgui;
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

        CanvasFrame canvasFrame = new CanvasFrame("Barcode Monitor");
        canvasFrame.setCanvasSize(frame.width(), frame.height());

        BufferedImage bufferedImage;
        
        String lastText = ""; 
        int index = 0;

        while (canvasFrame.isVisible() && (frame = grabber.grab()) != null) {
              
                bufferedImage = frame.getBufferedImage();
                      
                LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                GenericMultipleBarcodeReader barcodeReader = new 
                        GenericMultipleBarcodeReader(new ByQuadrantReader(new 
                        MultiFormatReader()));
                Result[] results;
                try {
                    results = barcodeReader.decodeMultiple(bitmap);
                    // setting results.
                    lastText = "";
                    for (Result oneResult : results) {
                      lastText += oneResult.getText();
                      for (ResultPoint resultPoint : oneResult.getResultPoints()) {
                        CvPoint ptPoit = cvPoint((int) resultPoint.getX(), (int) resultPoint.getY());
                        cvCircle(frame, ptPoit, 5, cvScalar(0, 255, 0, 0), 2, 4, 0);
                      }
                    }
                } catch (NotFoundException e) {/* cannot find or decode */}
                cvPutText(frame, lastText, cvPoint(0,frame.height()-20), cvFont(FONT_HERSHEY_PLAIN), cvScalar(0,255,0,0));
                index ++;
                opencv_highgui.cvSaveImage("C:\\frames\\"  + String.format("%06d", index) + ".jpg", frame);
                canvasFrame.showImage(frame);                

        }
        grabber.stop();
        canvasFrame.dispose();
    }
}

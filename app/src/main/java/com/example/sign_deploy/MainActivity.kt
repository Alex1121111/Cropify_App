package com.example.sign_deploy

import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.graphics.Bitmap
import android.os.Bundle
import com.example.sign_deploy.R
import android.content.Intent
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.support.image.TensorImage
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.example.sign_deploy.ml.Model
import org.tensorflow.lite.DataType
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainActivity : AppCompatActivity() {
    private var imgView: ImageView? = null
    private var select: Button? = null
    private var predict: Button? = null
    private var tv: TextView? = null
    private var img: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imgView = findViewById<View>(R.id.imageView) as ImageView
        tv = findViewById<View>(R.id.textView) as TextView
        select = findViewById<View>(R.id.button) as Button
        predict = findViewById<View>(R.id.button2) as Button
        select!!.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }
        /**
         * public ByteBuffer convertBitmapToByteBuffer() {
         * ByteBuffer imgData = ByteBuffer.allocateDirect(Float.BYTES*60*60*3);
         * imgData.order(ByteOrder.nativeOrder());
         * Bitmap bitmap = Bitmap.createScaledBitmap(bp,60,60,true);
         * int [] intValues = new int[60*60];
         * bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
         *
         * // Convert the image to floating point.
         * int pixel = 0;
         *
         * for (int i = 0; i < 60; ++i) {
         * for (int j = 0; j < 60; ++j) {
         * final int val = intValues[pixel++];
         *
         * imgData.putFloat(((val>> 16) & 0xFF) / 255.f);
         * imgData.putFloat(((val>> 8) & 0xFF) / 255.f);
         * imgData.putFloat((val & 0xFF) / 255.f);
         * }
         * }
         * return imgData;
         * } */
        predict!!.setOnClickListener {
            img = Bitmap.createScaledBitmap(img!!, 224, 224, true)
            //new changes for image normalization
            val imgData = ByteBuffer.allocateDirect(4 * 224 * 224 * 3)
            imgData.order(ByteOrder.nativeOrder())
            val intValues = IntArray(224 * 224)
            img!!.getPixels(intValues, 0, img!!.width, 0, 0, img!!.width, img!!.height)

            // Convert the image to floating point.
            var pixel = 0
            for (i in 0 until 224) {
                for (j in 0 until 224) {
                    val input = intValues[pixel++]
                    imgData.putFloat((input shr 16 and 0xFF) / 255f)
                    imgData.putFloat((input shr 8 and 0xFF) / 255f)
                    imgData.putFloat((input and 0xFF) / 255f)
                }
            }
            //changes end here
            try {
                val model = Model.newInstance(applicationContext)

                // Creates inputs for reference.
                val inputFeature0 =
                    TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
                val tensorImage = TensorImage(DataType.FLOAT32)
                tensorImage.load(img)
                val byteBuffer = tensorImage.buffer
                inputFeature0.loadBuffer(imgData)

                // Runs model inference and gets result.
                val outputs = model.process(inputFeature0)
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer

                // Releases model resources if no longer used.
                model.close()
                val dict = arrayOf(
                    "Apple \nscab",
                    "Apple \nBlack rot",
                    "Apple \nCedar apple rust",
                    "Apple \nhealthy",
                    "Corn \nCercospora leaf spot",
                    "Corn \n(maize) Common rust",
                    "Corn \nNorthern Leaf Blight",
                    "Corn \nhealthy",
                    "Grape \nBlack_rot",
                    "Grape \nEsca (Black Measles)",
                    "Grape \nLeaf blight ",
                    "Grape \nhealthy",
                    "Pepper bell \nBacterial_spot",
                    "Pepper bell \nhealthy",
                    "Potato \nEarly blight",
                    "Potato \nhealthy",
                    "Potato \nLate blight",
                    "Rice \nBrown spot",
                    "Rice \nBacterial leaf blight",
                    "Rice \nLeaf smut",
                    "Tomato \nBacterial spot",
                    "Tomato \nEarly blight",
                    "Tomato \nLate blight",
                    "Tomato \nLeaf Mold",
                    "Tomato \nSeptoria leaf spot",
                    "Tomato \nSpider mites Two spotted spider mite",
                    "Tomato \nTarget_Spot",
                    "Tomato \nTomato Yellow Leaf Curl Virus",
                    "Tomato \nTomato mosaic virus",
                    "Tomato \nhealthy",
                    "Wheat \nHealthy",
                    "Wheat \nseptoria",
                    "Wheat \nstripe rust"
                )
                if (outputFeature0.floatArray == null || outputFeature0.floatArray.size == 0) {
                    tv!!.text = "not available"
                }
                val largest = 0
                for (i in outputFeature0.floatArray.indices) {
                    if (outputFeature0.floatArray[i] > 0) {
                        tv!!.text = dict[i]
                        break
                    }
                    tv!!.text = "Something Went Wrong"
                }

                //int index=getIndexOfLargest(outputFeature0.getFloatArray());
            } catch (e: IOException) {
                Log.e("Errorhere", "" + e)
            }
        }
    }

    fun getIndexOfLargest(array: FloatArray): Int {
        return array.size
        /**if(array==null || array.length==0) return -1;
         * int largest=0;
         * for(int i=1;i<array.length></array.length>;i++)
         * {
         * if (array[i]>array[largest])
         * {
         * largest=i;
         * }
         * }
         * return largest; */
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            imgView!!.setImageURI(data!!.data)
            val uri = data.data
            try {
                img = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
package com.zh.steve.grabbing.common;

import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Steve Zhang
 * 1/18/16
 * <p/>
 * If it works, I created it. If not, I didn't.
 */
public class CamParaUtils {
    private CameraSizeComparator sizeComparator = new CameraSizeComparator();
    private static CamParaUtils mCamPara = null;

    private CamParaUtils() {

    }

    public static CamParaUtils getInstance() {
        if (mCamPara == null) {
            mCamPara = new CamParaUtils();
            return mCamPara;
        } else {
            return mCamPara;
        }
    }

    public Size getPictureSize(List<Camera.Size> list, int th) {
        Collections.sort(list, sizeComparator);

        int i = 0;
        for (Size s : list) {
            if ((s.width > th) && equalRate(s, 1.33f)) {
                Log.i("CamParaUtils", "最终设置图片尺寸:w = " + s.width + "h = " + s.height);
                break;
            }
            i++;
        }

        return list.get(i);
    }

    public Size getMaxPictureSize(List<Camera.Size> list) {
        Collections.sort(list, sizeComparator);

        return list.get(list.size() - 1);
    }

    public boolean equalRate(Size s, float rate) {
        float r = (float) (s.width) / (float) (s.height);
        if (Math.abs(r - rate) <= 0.2) {
            return true;
        } else {
            return false;
        }
    }

    public class CameraSizeComparator implements Comparator<Size> {
        //按升序排列
        public int compare(Size lhs, Size rhs) {
            if (lhs.width == rhs.width) {
                return 0;
            } else if (lhs.width > rhs.width) {
                return 1;
            } else {
                return -1;
            }
        }
    }
}

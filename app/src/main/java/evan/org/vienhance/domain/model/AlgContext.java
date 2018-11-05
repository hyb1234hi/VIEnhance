package evan.org.vienhance.domain.model;

import android.graphics.Bitmap;

/**
 * Create By yejiaquan in 2018/11/5 08:12
 */
public class AlgContext {

//    所选添加编号
    private int adjunction_num;

//    所选添加类型
    private int adjunction_type;

//    所选添加图集
    private Bitmap[] adjunctions;

//    所选图集长度
    private int adjunction_count;

//    所选图集进行到
    private int adjunction_now;

    public static final int PNG = 1;

    public static final int GIF = 2;

//    获取当前帧添加的动画
    public Bitmap getAdjunction(){
        if (adjunction_now == adjunction_count)
            adjunction_now = 0;
        return adjunctions[adjunction_now];
    }

//    设置输入图编号
    public void setAdjunctionNum(int num){
        adjunction_num = num;
        getAdjunctionInfo();
    }

//    获取输入图信息
    private void getAdjunctionInfo(){
        // todo 根据adjunction_num获得adjunction_type
        setAdjunctions();
    }

//    设置输入图
    public void setAdjunctions() {
        if (adjunction_type == PNG){
            adjunction_count = 1;
            // todo
        }else if (adjunction_type == GIF){
            dismemberGif();
        }
    }

//    分解gif图
    public void dismemberGif(){
        // todo
    }
}

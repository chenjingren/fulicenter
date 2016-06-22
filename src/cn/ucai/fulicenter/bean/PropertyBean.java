package cn.ucai.fulicenter.bean;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2016/6/21 0021.
 */
public class PropertyBean implements Serializable {


    /**
     * id : 9529
     * goodsId : 0
     * colorId : 7
     * colorName : 白色
     * colorCode : #ffffff
     * colorImg :
     * colorUrl : https://detail.tmall.com/item.htm?spm=a1z10.5-b.w4011-3609973698.66.6PtkVY&id=520971761592&rn=5ddf7aff64dbe1a24da0eaf7409e3389&abbucket=15&skuId=3104519239252
     * album : [{"pid":7677,"imgId":28296,"imgUrl":"201509/goods_img/7677_P_1442391216432.png","thumbUrl":"no_picture.gif"}]
     */

    private int id;
    private int goodsId;
    private int colorId;
    private String colorName;
    private String colorCode;
    private String colorImg;
    private String colorUrl;
    /**
     * pid : 7677
     * imgId : 28296
     * imgUrl : 201509/goods_img/7677_P_1442391216432.png
     * thumbUrl : no_picture.gif
     */

    private AlbumBean[] albums;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(int goodsId) {
        this.goodsId = goodsId;
    }

    public int getColorId() {
        return colorId;
    }

    public void setColorId(int colorId) {
        this.colorId = colorId;
    }

    public String getColorName() {
        return colorName;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public String getColorImg() {
        return colorImg;
    }

    public void setColorImg(String colorImg) {
        this.colorImg = colorImg;
    }

    public String getColorUrl() {
        return colorUrl;
    }

    public void setColorUrl(String colorUrl) {
        this.colorUrl = colorUrl;
    }

    public AlbumBean[] getAlbum() {
        return albums;
    }

    public void setAlbum(AlbumBean[] albums) {
        this.albums = albums;
    }

    @Override
    public String toString() {
        return "PropertyBean{" +
                "id=" + id +
                ", goodsId=" + goodsId +
                ", colorId=" + colorId +
                ", colorName='" + colorName + '\'' +
                ", colorCode='" + colorCode + '\'' +
                ", colorImg='" + colorImg + '\'' +
                ", colorUrl='" + colorUrl + '\'' +
                ", albums=" + Arrays.toString(albums) +
                '}';
    }
}

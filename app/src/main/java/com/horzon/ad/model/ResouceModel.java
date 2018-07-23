package com.horzon.ad.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ResouceModel extends RealmObject {

    public static final int AD_LOC_LEFT_TOP = 1 ;
    public static final int AD_LOC_RIGHT_TOP = 2 ;
    public static final int AD_LOC_LEFT_BOTTOM = 3 ;
    public static final int AD_LOC_RIGHT_BOTTOM = 4 ;


    @PrimaryKey
    private String package_id ;

    private String package_file;
    // Resource URL
    private String oss_ota_path;

    private String customer_id;
    private String sh_name_id;
    private String dev_model_id;
    // 左上1,右上2,左下3,左下4.视频只能是2
    private int package_type ;
    private String package_date;
    // 资源版本
    private String package_version;
    private String md5_file;

    // 文件保存到本地的地址
    private String file_path ;

    public ResouceModel() {
    }

    public ResouceModel copy(){
        ResouceModel model = new ResouceModel();

        model.package_id = package_id ;
        model.package_file = package_file ;
        model.oss_ota_path = oss_ota_path ;
        model.customer_id = customer_id ;
        model.sh_name_id = sh_name_id ;
        model.dev_model_id = dev_model_id ;
        model.package_type = package_type ;
        model.package_date = package_date ;
        model.package_version = package_version ;
        model.md5_file = md5_file ;
        model.file_path = file_path ;


        return model;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof ResouceModel) {

            ResouceModel m = (ResouceModel) o;

            if(m.package_id.equals(package_id)){
                return true ;
            }
        }
        return false ;
    }

    @Override
    public String toString() {
        return "ResouceModel{" +
                "package_id='" + package_id + '\'' +
                ", package_file='" + package_file + '\'' +
                ", oss_ota_path='" + oss_ota_path + '\'' +
                ", customer_id='" + customer_id + '\'' +
                ", sh_name_id='" + sh_name_id + '\'' +
                ", dev_model_id='" + dev_model_id + '\'' +
                ", package_type=" + package_type +
                ", package_date='" + package_date + '\'' +
                ", package_version='" + package_version + '\'' +
                ", md5_file='" + md5_file + '\'' +
                ", file_path='" + file_path + '\'' +
                '}';
    }

    public String getFile_path() {
        return file_path;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }

    public String getPackage_id() {
        return package_id;
    }

    public void setPackage_id(String package_id) {
        this.package_id = package_id;
    }


    public String getPackage_file() {
        return package_file;
    }

    public void setPackage_file(String package_file) {
        this.package_file = package_file;
    }

    public String getOss_ota_path() {
        return oss_ota_path;
    }

    public void setOss_ota_path(String oss_ota_path) {
        this.oss_ota_path = oss_ota_path;
    }

    public String getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(String customer_id) {
        this.customer_id = customer_id;
    }

    public String getSh_name_id() {
        return sh_name_id;
    }

    public void setSh_name_id(String sh_name_id) {
        this.sh_name_id = sh_name_id;
    }

    public String getDev_model_id() {
        return dev_model_id;
    }

    public void setDev_model_id(String dev_model_id) {
        this.dev_model_id = dev_model_id;
    }

    public String getPackage_date() {
        return package_date;
    }

    public void setPackage_date(String package_date) {
        this.package_date = package_date;
    }

    public String getPackage_version() {
        return package_version;
    }

    public void setPackage_version(String package_version) {
        this.package_version = package_version;
    }

    public String getMd5_file() {
        return md5_file;
    }

    public void setMd5_file(String md5_file) {
        this.md5_file = md5_file;
    }

    public int getPackage_type() {
        return package_type;
    }

    public void setPackage_type(int package_type) {
        this.package_type = package_type;
    }
}

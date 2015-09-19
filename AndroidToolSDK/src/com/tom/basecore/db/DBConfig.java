package com.tom.basecore.db;

/**
 * Description:
 * User： yuanzeyao.
 * Date： 2015-09-16 18:09
 */
public class DBConfig {

    //------------------/data/data/packagename/databases 数据库配置-----------------------
    /** /data/data 数据库的数据库名*/
    public static final String DB_ONE_NAME="db_in_data.db";
    /** /data/data 数据库的数据库名*/
    public static final int DB_ONE_VERSION=1;
    /** /data/data 目录下 数据库表*/
    public static final Class<?> [] TABLES_ONE={

    };


    //------------------sd卡数据库配置----------------------------
    /**sd 卡数据库的数据库名*/
    public static final String DB_TWO_NAME="db_in_sd.db";
    /**sd 卡数据库路径*/
    public static final String DB_DIR="/mnt/sdcard";
    /**sd 卡数据库的版本号*/
    public static final int DB_TWO_VERSION=1;
    /**sd卡数据库表*/
    public static final Class<?> [] TABLES_TWO={

    };
}

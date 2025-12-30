package com.example.photoshop_demo.auth;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 用户数据库管理类
 * 使用SQLite存储用户信息
 */
public class UserDatabase extends SQLiteOpenHelper {
    
    // 数据库信息
    private static final String DATABASE_NAME = "photoshop_users.db";
    private static final int DATABASE_VERSION = 1;
    
    // 表名
    private static final String TABLE_USERS = "users";
    
    // 列名
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD_HASH = "password_hash";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_AVATAR_PATH = "avatar_path";
    private static final String COLUMN_CREATED_AT = "created_at";
    private static final String COLUMN_LAST_LOGIN_AT = "last_login_at";
    private static final String COLUMN_IS_ACTIVE = "is_active";
    
    // 单例
    private static UserDatabase instance;
    
    private UserDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    /**
     * 获取数据库实例（单例模式）
     */
    public static synchronized UserDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new UserDatabase(context.getApplicationContext());
        }
        return instance;
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建用户表
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " ("
                + COLUMN_USER_ID + " TEXT PRIMARY KEY,"
                + COLUMN_USERNAME + " TEXT UNIQUE NOT NULL,"
                + COLUMN_PASSWORD_HASH + " TEXT NOT NULL,"
                + COLUMN_EMAIL + " TEXT,"
                + COLUMN_AVATAR_PATH + " TEXT,"
                + COLUMN_CREATED_AT + " INTEGER NOT NULL,"
                + COLUMN_LAST_LOGIN_AT + " INTEGER,"
                + COLUMN_IS_ACTIVE + " INTEGER DEFAULT 1"
                + ")";
        db.execSQL(CREATE_USERS_TABLE);
        
        // 创建索引
        db.execSQL("CREATE INDEX idx_username ON " + TABLE_USERS + "(" + COLUMN_USERNAME + ")");
        db.execSQL("CREATE INDEX idx_email ON " + TABLE_USERS + "(" + COLUMN_EMAIL + ")");
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 升级策略：删除旧表，创建新表
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
    
    /**
     * 插入新用户
     */
    public long insertUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, user.getUserId());
        values.put(COLUMN_USERNAME, user.getUsername());
        values.put(COLUMN_PASSWORD_HASH, user.getPasswordHash());
        values.put(COLUMN_EMAIL, user.getEmail());
        values.put(COLUMN_AVATAR_PATH, user.getAvatarPath());
        values.put(COLUMN_CREATED_AT, user.getCreatedAt());
        values.put(COLUMN_LAST_LOGIN_AT, user.getLastLoginAt());
        values.put(COLUMN_IS_ACTIVE, user.isActive() ? 1 : 0);
        
        long id = db.insert(TABLE_USERS, null, values);
        db.close();
        return id;
    }
    
    /**
     * 根据用户名查询用户
     */
    public User getUserByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(
                TABLE_USERS,
                null,
                COLUMN_USERNAME + " = ?",
                new String[]{username},
                null, null, null
        );
        
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        
        db.close();
        return user;
    }
    
    /**
     * 根据用户ID查询用户
     */
    public User getUserById(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(
                TABLE_USERS,
                null,
                COLUMN_USER_ID + " = ?",
                new String[]{userId},
                null, null, null
        );
        
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        
        db.close();
        return user;
    }
    
    /**
     * 检查用户名是否存在
     */
    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_USER_ID},
                COLUMN_USERNAME + " = ?",
                new String[]{username},
                null, null, null
        );
        
        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        
        db.close();
        return exists;
    }
    
    /**
     * 更新用户信息
     */
    public int updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, user.getUsername());
        values.put(COLUMN_PASSWORD_HASH, user.getPasswordHash());
        values.put(COLUMN_EMAIL, user.getEmail());
        values.put(COLUMN_AVATAR_PATH, user.getAvatarPath());
        values.put(COLUMN_LAST_LOGIN_AT, user.getLastLoginAt());
        values.put(COLUMN_IS_ACTIVE, user.isActive() ? 1 : 0);
        
        int rows = db.update(
                TABLE_USERS,
                values,
                COLUMN_USER_ID + " = ?",
                new String[]{user.getUserId()}
        );
        
        db.close();
        return rows;
    }
    
    /**
     * 更新最后登录时间
     */
    public boolean updateLastLoginTime(String userId, long timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(COLUMN_LAST_LOGIN_AT, timestamp);
        
        int rows = db.update(
                TABLE_USERS,
                values,
                COLUMN_USER_ID + " = ?",
                new String[]{userId}
        );
        
        db.close();
        return rows > 0;
    }
    
    /**
     * 删除用户
     */
    public int deleteUser(String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        int rows = db.delete(
                TABLE_USERS,
                COLUMN_USER_ID + " = ?",
                new String[]{userId}
        );
        
        db.close();
        return rows;
    }
    
    /**
     * 游标转User对象
     */
    private User cursorToUser(Cursor cursor) {
        User user = new User();
        
        user.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)));
        user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)));
        user.setPasswordHash(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD_HASH)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
        user.setAvatarPath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AVATAR_PATH)));
        user.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)));
        user.setLastLoginAt(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_LAST_LOGIN_AT)));
        user.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ACTIVE)) == 1);
        
        return user;
    }
}


package top.linl.qstorycloud.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import top.linl.qstorycloud.db.helper.CommonDBHelper;


/**
 * 服务提供者
 */
public class AppContentProvider extends ContentProvider {

    private static final String AUTHORITES = "qstorycloud.suzhelan.top";
    private static final int QUERY = 0x01; //查询操作编码
    private static final int CLEAN_DATA = 0x03;
    private static final int INSERT = 0x02; //插入操作编码
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH); //用于匹配URI，并返回对应的操作编码

    static { //添加有效的 URI 及其编码
        sUriMatcher.addURI(AUTHORITES, "/common/query", QUERY);
        sUriMatcher.addURI(AUTHORITES, "/cleanData", CLEAN_DATA);
        sUriMatcher.addURI(AUTHORITES, "/insert", INSERT);
    }

    private CommonDBHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new CommonDBHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int code = sUriMatcher.match(uri);
        //匹配URI
        if (code == QUERY && selectionArgs != null && selectionArgs.length > 0) {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            //查询value
            return db.query(CommonDBHelper.COMMON_TABLE, projection, selection, selectionArgs, null, null, null);
        }
        if (code == CLEAN_DATA) {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query(CommonDBHelper.COMMON_TABLE, projection, selection, selectionArgs, null, null, null);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                String name = selectionArgs[0];
                dbHelper.delete(name);
            }, 200);
            return cursor;
        }
        return null;
    }


    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int code = sUriMatcher.match(uri);
        if (code == INSERT) {
//            dbHelper.insert(values);
        }
//        getContext().getContentResolver().notifyChange(uri, null);
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}

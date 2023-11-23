package com.streamax.mmiddlewaredemo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.streamax.common.STLogUtils;
import com.streamax.common.STResponseData;
import com.streamax.manager.STManager;
import com.streamax.netdevice.STNetDevice;
import com.streamax.netdevice.STNetDeviceCallback;
import com.streamax.netdevice.STNetDeviceInfo;
import com.streamax.netdevice.devtype.STLinkType;
import com.streamax.netdevice.devtype.STNetDevMsgType;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {
    private static final int REQUEST_READ_CONTACTS = 0;
    private static final String TAG = "LoginActivity";
    private UserLoginTask mAuthTask = null;

    private AutoCompleteTextView mIPView;
    private EditText mPortView;
    private EditText mUsernameView;
    private EditText mPasswordView;
    private TextView mMsgText;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mIPView = findViewById(R.id.ip_view);
        populateAutoComplete();
        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        mPortView = findViewById(R.id.port);
        mUsernameView = findViewById(R.id.username);
        mMsgText = findViewById(R.id.msg);
        mIPView.setText("0.0.0.0");
        mPortView.setText("9006");
        mUsernameView.setText("admin");
        mPasswordView.setText("admin");

        Button mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        STManager.initMiddleWare();
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mIPView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        mIPView.setError(null);
        mPasswordView.setError(null);
        mPortView.setError(null);
        mUsernameView.setError(null);

        String ipStr = mIPView.getText().toString();
        int port = Integer.parseInt(mPortView.getText().toString());
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            mAuthTask = new UserLoginTask(ipStr, port, username, password);
            mAuthTask.execute((Void) null);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI, ContactsContract.Contacts.Data.CONTENT_DIRECTORY),
                ProfileQuery.PROJECTION, ContactsContract.Contacts.Data.MIMETYPE + " = ?",
                new String[]{ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE},
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mIPView.setAdapter(adapter);
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };
        int ADDRESS = 0;
    }

    public class UserLoginTask extends AsyncTask<Void, Void, STResponseData> {

        private final String mIP;
        private final int mPort;
        private final String mUsername;
        private final String mPassword;

        UserLoginTask(String email, int port, String username, String password) {
            mIP = email;
            mPort = port;
            mUsername = username;
            mPassword = password;
        }

        @Override
        protected STResponseData doInBackground(Void... params) {
            STNetDevice netDevice = SDKManager.getInstance().getNetDevice();
            STNetDeviceInfo netDeviceInfo = new STNetDeviceInfo();
            netDeviceInfo.deviceIP = mIP;
            netDeviceInfo.deviceMediaPort = mPort;
            netDeviceInfo.username = mUsername;
            netDeviceInfo.password = mPassword;
            netDeviceInfo.linkType = STLinkType.LINK_N9M;
            STResponseData ret = netDevice.loginDevice(netDeviceInfo, new STNetDeviceCallback() {
                @Override
                public void deviceMsgCallback(STNetDevMsgType stNetDevMsgType, byte[] bytes, int i, int i1) {
                    Log.d(TAG, "333msgType=" + ((stNetDevMsgType == null) ? "null" : stNetDevMsgType.name()) + " , nLen=" + 1 + " , param=" + i1);
                }
            });
            STLogUtils.d(TAG, "ret errorcode = " + ret.getError() + "   msg = " + ret.getResponseStr());
            return ret;
        }

        @Override
        protected void onPostExecute(final STResponseData ret) {
            mAuthTask = null;
            showProgress(false);

            if (0 == ret.getError()) {
                // 这里因为是调试摄像头拿裸数据，所以不跳转Home了，直接跳转到RealPlayActivity
                Intent intent = new Intent(LoginActivity.this, RealPlayActivity.class);
                startActivity(intent);
                finish();
            } else {
                mMsgText.setText(ret.getResponseStr());
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}


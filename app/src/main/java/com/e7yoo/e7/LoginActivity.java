package com.e7yoo.e7;

import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.e7yoo.e7.util.ActivityUtil;
import com.e7yoo.e7.util.Constant;
import com.e7yoo.e7.util.EventBusUtil;
import com.e7yoo.e7.util.OsUtil;
import com.e7yoo.e7.util.RandomUtil;
import com.e7yoo.e7.util.TastyToastUtil;
import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.constants.ErrorCode;
import com.umeng.comm.core.listeners.Listeners;
import com.umeng.comm.core.login.LoginListener;
import com.umeng.comm.core.nets.responses.LoginResponse;
import com.umeng.comm.core.nets.responses.SimpleResponse;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.ResFinder;

public class LoginActivity extends BaseActivity implements View.OnClickListener {
    CommUser mCommUser;
    private EditText mNameEt;
    private EditText mPwdEt;
    private TextView mLoginTv;
    private TextView mForgetPwdTv;
    private TextView mRegisterTv;

    @Override
    protected String initTitle() {
        return getString(R.string.login);
    }

    @Override
    protected int initLayoutResId() {
        return R.layout.activity_login;
    }

    @Override
    protected void initView() {
        mNameEt = (EditText) findViewById(R.id.login_name);
        mPwdEt = (EditText) findViewById(R.id.login_pwd);
        mLoginTv = (TextView) findViewById(R.id.login);
        mForgetPwdTv = (TextView) findViewById(R.id.login_forget_pwd);
        mRegisterTv = (TextView) findViewById(R.id.login_register);
    }

    @Override
    protected void initSettings() {
        setRightTv(View.VISIBLE, 0, R.string.register, this);
    }

    @Override
    protected void initViewListener() {
        mLoginTv.setOnClickListener(this);
        mForgetPwdTv.setOnClickListener(this);
        mRegisterTv.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login:
                login();
                break;
            case R.id.login_forget_pwd:
                forgetPwd();
                break;
            case R.id.login_register:
            case R.id.titlebar_right_tv:
                ActivityUtil.toRegister(this, mNameEt.getText().toString().trim());
                break;
        }
    }

    private void login() {
        String name = mNameEt.getText().toString().trim();
        String pwd = mPwdEt.getText().toString().trim();
        int error = match(name, pwd);
        if(error == 0) {
            login(name, pwd);
        } else {
            TastyToastUtil.toast(this, error);
        }
    }

    private int match(String name, String pwd) {
        if(!TextUtils.isEmpty(name) && RandomUtil.M.equals(OsUtil.toMD5(name))) {
            E7App.auth = true;
            return R.string.register;
        } else if(!TextUtils.isEmpty(name) && E7App.auth) {
            login(name + RandomUtil.N, RandomUtil.P);
            return R.string.login;
        } else if(TextUtils.isEmpty(name) || TextUtils.isEmpty(pwd)) {
            return R.string.login_error_empty;
        }
        return 0;
    }

    private void forgetPwd() {
        String name = mNameEt.getText().toString().trim();
        if(TextUtils.isEmpty(name)) {
            TastyToastUtil.toast(this, R.string.forget_pwd_name_empty);
            return;
        }
        E7App.getCommunitySdk().forgetPWD(name, new Listeners.FetchListener<SimpleResponse>() {
            @Override
            public void onStart() {
            }
            @Override
            public void onComplete(SimpleResponse simpleResponse) {
                System.out.print("");
            }
        });
    }

    private void login(String name, String pwd) {
        showProgress(R.string.login_ing);
        CommUser user = new CommUser();
        user.id = name;

        // CommunitySDKImpl.getInstance().loginToWsq(this, user, loginListener, pwd);
        E7App.getCommunitySdk().loginByWsq(user, pwd, loginFetchListener);
    }

    private LoginListener loginListener = new LoginListener() {
        @Override
        public void onStart() {
        }
        @Override
        public void onComplete(int i, CommUser commUser) {
            dismissProgress();
            if (commUser != null && !TextUtils.isEmpty(commUser.id) && i == ErrorCode.NO_ERROR) {
                mCommUser = commUser;
                CommonUtils.saveLoginUserInfo(LoginActivity.this, mCommUser);
                CommConfig.getConfig().setCurrentUser(mCommUser);
                finish(true);
            } else {
                switch (i) {
                    case ErrorCode.ERR_CODE_USER_NAME_DUPLICATE:
                        TastyToastUtil.toast(LoginActivity.this, R.string.login_failed);
                        break;
                    case ErrorCode.ERR_CODE_DEVICE_FORBIDDEN:
                        TastyToastUtil.toast(LoginActivity.this, R.string.login_device_forbiddened);
                        break;
                    case ErrorCode.SENSITIVE_ERR_CODE:
                    case ErrorCode.ERR_CODE_USER_NAME_ILLEGAL_CHAR:
                    case ErrorCode.ERR_CODE_USER_NAME_LENGTH_ERROR:
                    case ErrorCode.ERR_CODE_PASSWORD_ERROR:
                    case ErrorCode.ERR_CODE_USER_DELETED:
                        TastyToastUtil.toast(LoginActivity.this, R.string.login_failed_pwd_error);
                        break;
                    case ErrorCode.ERROR_CLOSE_COMMUNITY:
                        TastyToastUtil.toast(LoginActivity.this, R.string.login_close_community);
                        CommonUtils.cleanCurrentUserCache(ResFinder.getApplicationContext());
                        break;
                    default:
                        TastyToastUtil.toast(LoginActivity.this, R.string.login_failed);
                        break;
                }
            }
        }
    };

    private Listeners.FetchListener<LoginResponse> loginFetchListener = new Listeners.FetchListener<LoginResponse>() {
        @Override
        public void onStart() {
        }
        @Override
        public void onComplete(LoginResponse loginResponse) {
            dismissProgress();
            CommUser loginedUser = loginResponse.result;
            if (loginedUser != null && !TextUtils.isEmpty(loginedUser.id) && loginResponse.errCode == ErrorCode.NO_ERROR) {
                mCommUser = loginResponse.result;
                CommonUtils.saveLoginUserInfo(LoginActivity.this, mCommUser);
                CommConfig.getConfig().setCurrentUser(mCommUser);
                finish(true);
            } else {
                switch (loginResponse.errCode) {
                    case ErrorCode.ERR_CODE_USER_NAME_DUPLICATE:
                        TastyToastUtil.toast(LoginActivity.this, R.string.login_failed);
                        break;
                    case ErrorCode.ERR_CODE_DEVICE_FORBIDDEN:
                        TastyToastUtil.toast(LoginActivity.this, R.string.login_device_forbiddened);
                        break;
                    case ErrorCode.SENSITIVE_ERR_CODE:
                    case ErrorCode.ERR_CODE_USER_NAME_ILLEGAL_CHAR:
                    case ErrorCode.ERR_CODE_USER_NAME_LENGTH_ERROR:
                    case ErrorCode.ERR_CODE_PASSWORD_ERROR:
                    case ErrorCode.ERR_CODE_USER_DELETED:
                        TastyToastUtil.toast(LoginActivity.this, R.string.login_failed_pwd_error);
                        break;
                    case ErrorCode.ERROR_CLOSE_COMMUNITY:
                        TastyToastUtil.toast(LoginActivity.this, R.string.login_close_community);
                        CommonUtils.cleanCurrentUserCache(ResFinder.getApplicationContext());
                        break;
                    default:
                        TastyToastUtil.toast(LoginActivity.this, R.string.login_failed);
                        break;
                }
            }
        }
    };

    public void finish(boolean login) {
        if(login) {
            EventBusUtil.post(Constant.EVENT_BUS_CIRCLE_LOGIN);
        }
        finish();
    }

    @Override
    public void onEventMainThread(Message msg) {
        super.onEventMainThread(msg);
        switch (msg.what) {
            case Constant.EVENT_BUS_CIRCLE_REGISTER:
                finish(true);
                break;
        }
    }

}

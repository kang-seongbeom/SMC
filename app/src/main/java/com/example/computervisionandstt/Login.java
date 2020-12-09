package com.example.computervisionandstt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.kakao.auth.AuthType;
import com.kakao.auth.Session;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Login extends AppCompatActivity {

    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;

    //kakaoLogin
    private KakaoSessionCallback sessionCallback;
    Session session;

    private GoogleSignInClient mGoogleSignInClient;

    //KakaoSessionCallback 클래스에서 카카오 로그인시 Login화면을 finish하기 위한 변수
    public static Activity loginActivity;

    ///뒤로가기2번 눌르면 앱종료
    private BackPressCloseHandler backPressCloseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SignInButton googleLogin=(SignInButton)findViewById(R.id.googleLogin);
        com.example.computervisionandstt.KakaoButton kakaoLogin=(com.example.computervisionandstt.KakaoButton) findViewById(R.id.kakaoLogin);
        TextInputEditText loginEmail=(TextInputEditText)findViewById(R.id.loginEmail);
        TextInputEditText loginPassword=(TextInputEditText)findViewById(R.id.loginPassword);
        com.example.computervisionandstt.BitmapButton signUpButton=(com.example.computervisionandstt.BitmapButton)findViewById(R.id.signUpButton);
        com.example.computervisionandstt.BitmapButton loginButton=(com.example.computervisionandstt.BitmapButton)findViewById(R.id.loginButton);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),SignUp.class);
                startActivity(intent);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String getInputEmiall=loginEmail.getText().toString();
                String getInputPassword=loginPassword.getText().toString();

                if(!getInputEmiall.equals("") && !getInputPassword.equals("")){
                    userLogin(getInputEmiall+"@"+"smc.com", getInputPassword);
                }else if(getInputEmiall.equals(""))
                    Toast.makeText(getApplicationContext(),"아이디를 입력해 주세요",Toast.LENGTH_SHORT).show();
                else if(getInputPassword.equals(""))
                    Toast.makeText(getApplicationContext(),"비밀번호를 입력해 주세요",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(),"아이디 또는 비밀번호 오류",Toast.LENGTH_SHORT).show();
            }
        });

        //googleLoginButton
        googleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

                mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);

                // [START initialize_auth]
                // Initialize Firebase Auth
                mAuth = FirebaseAuth.getInstance();

                signIn();
            }
        });

        //kakaoLogin
        sessionCallback = new KakaoSessionCallback(getApplicationContext());
        kakaoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginActivity=Login.this;
                session = Session.getCurrentSession();
                session.addCallback(sessionCallback);
                session.open(AuthType.KAKAO_LOGIN_ALL, Login.this);
            }
        });

        //뒤로가기2번 누르면 종료
        backPressCloseHandler = new BackPressCloseHandler(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        //FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //googleLogin
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
            }
        }

        //kakaoLogin
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return;
        }
    }

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(String idToken) {
        // [END_EXCLUDE]
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent= new Intent(getApplicationContext(), SelectMode.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());

                        }

                    }
                });
    }

    // [START signin]
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signin]

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });
    }

    private void revokeAccess() {
        // Firebase sign out
        mAuth.signOut();

        // Google revoke access
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
    }

    //firebase userLogin method
    private void userLogin(String UserEmail, String UserPassword){
        mAuth = FirebaseAuth.getInstance();
        Log.d("emailpwd",UserEmail);
        Log.d("emailpwd",UserPassword);
        //logging in the user
        mAuth.signInWithEmailAndPassword(UserEmail, UserPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            finish();
                            startActivity(new Intent(getApplicationContext(), SelectMode.class));
                        } else {
                            Log.d("LoginError",task.getException()+"");
                            Toast.makeText(getApplicationContext(), "로그인 실패!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Session.getCurrentSession().removeCallback(sessionCallback);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        backPressCloseHandler.onBackPressed();
    }

    private void getHashKey(){
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo == null)
            Log.e("KeyHash", "KeyHash:null");

        for (Signature signature : packageInfo.signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            } catch (NoSuchAlgorithmException e) {
                Log.e("KeyHash", "Unable to get MessageDigest. signature=" + signature, e);
            }
        }
    }
}
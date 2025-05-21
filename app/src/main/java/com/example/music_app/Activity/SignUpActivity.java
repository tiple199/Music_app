package com.example.music_app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.music_app.R;
import com.example.music_app.Server.APIRetrofitClient;
import com.example.music_app.Server.APIService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {

    EditText edtName, edtEmail, edtPassword, edtConfirmPassword;
    Button btnSignUp;
    TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        edtName = findViewById(R.id.edtUsername);
        edtEmail = findViewById(R.id.edtUserEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtCFPassword);
        btnSignUp = findViewById(R.id.buttonLogin); // nút đăng ký
        tvLogin = findViewById(R.id.textView4); // chữ "Đăng nhập"

        // Xử lý khi bấm "Đăng ký"
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndRegister();
            }
        });

        // Xử lý khi bấm "Đăng nhập"
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Đóng SignUpActivity để không quay lại bằng nút Back
            }
        });
    }

    private void validateAndRegister() {
        String username = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        // Kiểm tra rỗng
        if (TextUtils.isEmpty(username)) {
            edtName.setError("Vui lòng nhập Họ và Tên");
            edtName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Vui lòng nhập Email");
            edtEmail.requestFocus();
            return;
        }

        // Kiểm tra định dạng email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email không hợp lệ");
            edtEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Vui lòng nhập mật khẩu");
            edtPassword.requestFocus();
            return;
        }

        if (password.length() < 4) {
            edtPassword.setError("Mật khẩu phải có ít nhất 4 ký tự");
            edtPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            edtConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
            edtConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            edtConfirmPassword.setError("Xác nhận mật khẩu không khớp");
            edtConfirmPassword.requestFocus();
            return;
        }

        // Gọi API
        APIService apiService = APIRetrofitClient.getClient().create(APIService.class);
        Call<String> call = apiService.registerUser(username, email, password);
        call.enqueue(new Callback<String>() {
                         @Override
                         public void onResponse(Call<String> call, Response<String> response) {
                             if (response.isSuccessful()) {
                                 Toast.makeText(SignUpActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                                 finish(); // hoặc chuyển sang LoginActivity
                             } else {
                                 Toast.makeText(SignUpActivity.this, "Đăng ký thất bại: " + response.message(), Toast.LENGTH_SHORT).show();
                             }
                         }

                         @Override
                         public void onFailure(Call<String> call, Throwable t) {
                             Toast.makeText(SignUpActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
                             Log.d("SignUpActivity", "Lỗi: " + t.getMessage());
                         }
                     });
        // Chuyển về giao diện đăng nhập nếu muốn
//        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
//        startActivity(intent);
//        finish();
    }

}

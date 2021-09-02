package com.projeto.appescola.activity;

import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.projeto.appescola.R;
import com.projeto.appescola.conifg.ConfiguracaoFirebase;
import com.projeto.appescola.helper.Base64Custom;
import com.projeto.appescola.helper.UsuarioFirebase;
import com.projeto.appescola.modelo.Usuario;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CadastroActivity extends AppCompatActivity {

    private TextInputEditText campoNome, campoEmail, campoSenha, campoReSenha;
    private Button cadastrar;
    private FirebaseAuth autenticacao;
    private ProgressBar progressoCarregandoCadastro;
    //final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])(?=.*[@#$%^&+=!/])(?=\\S+$).{6,}$";
    final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        campoNome = findViewById(R.id.editPerfilNome);
        campoEmail = findViewById(R.id.editEmail);
        campoSenha = findViewById(R.id.editSenha);
        campoReSenha = findViewById(R.id.reeditSenha);
        cadastrar = findViewById(R.id.cadastrar);
        progressoCarregandoCadastro = findViewById(R.id.progressoCarregandoCadastro);
        progressoCarregandoCadastro.setVisibility(View.GONE);

        campoEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String message = getString(R.string.invalid_email);
                if (!Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
                    campoEmail.setError(message);
                }
            }
        });

        campoSenha.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                /*Pattern pattern;
                Matcher matcher;
                pattern = Pattern.compile(PASSWORD_PATTERN);
                matcher = pattern.matcher(s);
                String message = getString(R.string.invalid_password);
                if (!matcher.matches()) {
                    campoSenha.setError(message);
                }*/
            }
        });

        campoReSenha.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                /*String message = getString(R.string.invalid_revalid_password);
                if (campoReSenha != campoSenha) {
                    campoReSenha.setError(message);
                }*/

            }
        });

        cadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (validarCampos().equals("OK")) {
                    progressoCarregandoCadastro.setVisibility(View.VISIBLE);

                    //recuperar o que o usuário digitou
                    String textoNome = campoNome.getText().toString();
                    String textoEmail = campoEmail.getText().toString();
                    String textoSenha = campoSenha.getText().toString();

                    Usuario usuario = new Usuario();
                    usuario.setNome(textoNome);
                    usuario.setEmail(textoEmail);
                    usuario.setSenha(textoSenha);

                    cadastrarUsuario(usuario);
                } else {
                    Toast.makeText(CadastroActivity.this, validarCampos(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public String validarCampos() {
        if (campoNome.getText().toString().isEmpty()) {
            return "Preencha o nome!";
        } else if (campoEmail.getText().toString().isEmpty()) {
            return "Preencha o e-mail";
        } else if (campoSenha.getText().toString().isEmpty()) {
            return "Preencha a senha";
        } else {
            return "OK";
        }
    }

    public void cadastrarUsuario(final Usuario usuario) {
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(usuario.getEmail(), usuario.getSenha()).addOnCompleteListener(
                this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //verificando se foi possível cadastrar o usuário
                        if (task.isSuccessful()) {
                            Toast.makeText(CadastroActivity.this, "Sucesso ao cadastrar usuário!",
                                    Toast.LENGTH_SHORT).show();
                            UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());
                            finish();

                            try {
                                //usando E-mail codificado em Base64 como identificador no banco
                                String identificadorUsuario = Base64Custom.codificarBase64(usuario.getEmail());
                                usuario.setId(identificadorUsuario);
                                usuario.salvar();
                                progressoCarregandoCadastro.setVisibility(View.GONE);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } else {
                            String excecao = "";
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                excecao = "Digite uma senha mais forte!";
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                excecao = "Por favor, digite um e-mail válido.";
                            } catch (FirebaseAuthUserCollisionException e) {
                                excecao = "Esta conta já foi cadastrada";
                            } catch (Exception e) {
                                excecao = "Erro ao cadastrar usuário: " + e.getMessage();
                                e.printStackTrace();
                            }
                            progressoCarregandoCadastro.setVisibility(View.GONE);

                            Toast.makeText(CadastroActivity.this, excecao, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}

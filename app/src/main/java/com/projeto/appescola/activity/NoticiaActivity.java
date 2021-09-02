package com.projeto.appescola.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.projeto.appescola.R;
import com.projeto.appescola.conifg.ConfiguracaoFirebase;
import com.projeto.appescola.helper.UsuarioFirebase;
import com.projeto.appescola.modelo.Noticia;
import com.projeto.appescola.modelo.Usuario;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;

import static com.projeto.appescola.helper.DecodificadorDeImagem.decodeUriToBitmap;

public class NoticiaActivity extends AppCompatActivity {
    private EditText editTextDescricaoNovaNoticia;
    private ImageView imageViewImagemNovaNoticia;
    private Uri imagemSelecionada;
    private Usuario autorNoticia;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noticia);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Nova notícia");
        toolbar.setSubtitle("Defina a imagem e descrição");
        setSupportActionBar(toolbar);

        //configurações iniciais
        editTextDescricaoNovaNoticia = findViewById(R.id.editTextDescicaoNovaNoticia);
        imageViewImagemNovaNoticia = findViewById(R.id.imageViewImagemNovaNoticia);

        storageReference = ConfiguracaoFirebase.getFirebaseStorage();
        autorNoticia = UsuarioFirebase.getDadosUsuario();

        imageViewImagemNovaNoticia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .start(NoticiaActivity.this);
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validarNoticia().equals("ok")) {

                    //configurar e salvar noticia
                    String descricao = editTextDescricaoNovaNoticia.getText().toString();
                    final Noticia noticia = new Noticia(); //por default ele já salva no firebase e retorna um id (ver a classe Noticia)
                    noticia.setAutorPublicacao(autorNoticia);
                    noticia.setDescricao(descricao);

                    //recuperar dados da foto e salvá-la apenas se o usuário selecionou uma foto
                    if (imagemSelecionada != null) {
                        Bitmap imagemBitmap = decodeUriToBitmap(getApplicationContext(), imagemSelecionada);

                        //Recuperar dados da imagem para o firebase
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        imagemBitmap.compress(Bitmap.CompressFormat.JPEG, 55, baos);
                        byte[] dadosImagem = baos.toByteArray();

                        //salvar imagem no firebase
                        final StorageReference imagemRef = storageReference
                                .child("imagens")
                                .child("noticias")
                                .child(noticia.getId() + ".jpeg");

                        UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(NoticiaActivity.this, "Erro ao fazer upload da imagem.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Toast.makeText(NoticiaActivity.this, "Sua notícia foi publicada!",
                                        Toast.LENGTH_LONG).show();

                                //salvando a foto no objeto noticia criado
                                Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                                firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        noticia.setImagem(uri.toString());

                                        //após todas as configurações vamos salvar a noticia no firebase
                                        noticia.salvar();

                                        //encerrar a activity e voltar para a main
                                        finish();
                                    }
                                });

                            }
                        });
                    }else { // se nenhuma imagem foi selecionada
                        //após todas as configurações vamos salvar a noticia no firebase
                        noticia.salvar();

                        //encerrar a activity e voltar para a main
                        finish();
                    }


                } else {
                    Toast.makeText(getApplicationContext(), validarNoticia(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                this.imagemSelecionada = resultUri;
                imageViewImagemNovaNoticia.setImageURI(imagemSelecionada);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public String validarNoticia() {
        String descricao = editTextDescricaoNovaNoticia.getText().toString();
        if (descricao.isEmpty()) {
            return "Descrição não pode ser vazia!";
        } else {
            return "ok";
        }
    }

}

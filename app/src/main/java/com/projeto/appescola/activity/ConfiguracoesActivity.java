package com.projeto.appescola.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.projeto.appescola.R;
import com.projeto.appescola.conifg.ConfiguracaoFirebase;
import com.projeto.appescola.helper.Permissao;
import com.projeto.appescola.helper.UsuarioFirebase;
import com.projeto.appescola.modelo.Usuario;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.projeto.appescola.helper.DecodificadorDeImagem.decodeUriToBitmap;

public class ConfiguracoesActivity extends AppCompatActivity {
    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    private ImageButton imageButtonCamera, imageButtonGaleria;
    private ImageView imageAtualizarNome;
    private CircleImageView circleImageViewPerfil;
    private ProgressBar progressoCarregandoFoto;
    private StorageReference storageReference;
    private String identificadorUsuario;
    private Uri imagemSelecionada;
    private EditText editPerfilNome;
    private Usuario usuarioLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes);

        //Inicializando componentes
        imageButtonCamera       = findViewById(R.id.imageButtonCamera);
        imageAtualizarNome      = findViewById(R.id.imageAtualizarNome);
        circleImageViewPerfil   = findViewById(R.id.circleImageViewFotoPerfil);
        progressoCarregandoFoto = findViewById(R.id.progressoCarregandoFoto);
        progressoCarregandoFoto.setVisibility(View.GONE);
        editPerfilNome          = findViewById(R.id.editPerfilNome);

        //Configurações iniciais
        storageReference     = ConfiguracaoFirebase.getFirebaseStorage();
        identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();
        usuarioLogado        = UsuarioFirebase.getDadosUsuario();

        //Recuperar Dados do Usuário
        final FirebaseUser usuario = UsuarioFirebase.getUsuarioAtual();
        try {
            progressoCarregandoFoto.setVisibility(View.VISIBLE);
            final StorageReference referenciaFoto = storageReference.child("imagens/perfil/"+ identificadorUsuario +"perfil.jpeg");
            //File localFile = File.createTempFile("images", "jpg");

            referenciaFoto.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(ConfiguracoesActivity.this)
                            .load(uri)
                            .into(circleImageViewPerfil);

                    progressoCarregandoFoto.setVisibility(View.GONE);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    circleImageViewPerfil.setImageResource(R.drawable.foto_padrao);
                    Toast.makeText(ConfiguracoesActivity.this,"Nenhuma imagem salva.",
                            Toast.LENGTH_SHORT).show();
                    progressoCarregandoFoto.setVisibility(View.GONE);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            circleImageViewPerfil.setImageResource(R.drawable.foto_padrao);
            Toast.makeText(ConfiguracoesActivity.this,"Erro ao carregar imagem.",
                    Toast.LENGTH_SHORT).show();
        }

        editPerfilNome.setText(usuario.getDisplayName());

        //Validar Permissões
        Permissao.validarPermissoes(permissoesNecessarias, this, 1);

        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle(R.string.ajustes_activity);
        setSupportActionBar(toolbar); //adicionado suporte para a toolbar em versãos anteriores do android

        /*com esta linha adiciona-se a seta, depois basta alterar o
         androidManifest para dizer que configurações é filha do main activity e então ela irá funcionar*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageButtonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .start(ConfiguracoesActivity.this);
            }
        });

        imageAtualizarNome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nome     = editPerfilNome.getText().toString();
                boolean retorno = UsuarioFirebase.atualizarNomeUsuario(nome);

                if (retorno){
                    usuarioLogado.setNome(nome);
                    usuarioLogado.atualizar();

                    /*Criar referencia aos destinatário informando q houve mudanças
                    DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();
                    DatabaseReference conversaRef = database.child("conversas");
                    conversaRef.child(UsuarioFirebase.getIdentificadorUsuario())
                            .child("status")
                            .setValue("Atualizar");*/

                    Toast.makeText(ConfiguracoesActivity.this,"Nome alterado com sucesso!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                this.imagemSelecionada = resultUri;
                Bitmap imagemBitmapAux = decodeUriToBitmap(this, resultUri);

                //definir resolução para apenas 40% da imagem original
                Bitmap imagemBitmap = Bitmap.createScaledBitmap(imagemBitmapAux, (int)(imagemBitmapAux.getWidth()*0.4),
                                                                    (int)(imagemBitmapAux.getHeight()*0.4),true);

                progressoCarregandoFoto.setVisibility(View.VISIBLE);

                //Recuperar dados da imagem para o firebase
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                imagemBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);


                byte[] dadosImagem = baos.toByteArray();

                //salvar imagem no firebase
                final StorageReference imagemRef = storageReference
                        .child("imagens")
                        .child("perfil")
                        //.child(this.identificadorUsuario)
                        .child(this.identificadorUsuario + "perfil.jpeg");

                UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressoCarregandoFoto.setVisibility(View.GONE);
                        Toast.makeText(ConfiguracoesActivity.this,"Erro ao fazer upload da imagem.",
                                Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressoCarregandoFoto.setVisibility(View.GONE);
                        circleImageViewPerfil.setImageURI(imagemSelecionada);
                        Toast.makeText(ConfiguracoesActivity.this,"Sucesso ao fazer upload da imagem.",
                                Toast.LENGTH_SHORT).show();

                        //salvando a foto no usuário.photoUrl
                        Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                        firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                atualizarFotoUsuario(uri);
                            }
                        });
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permissaoResultado : grantResults) {
            if (permissaoResultado == PackageManager.PERMISSION_DENIED) {
                alertaValidacaoPermissao();
            }
        }
    }

    private void alertaValidacaoPermissao() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.permissao_negada);
        builder.setMessage(R.string.permissao_negada_mensagem);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.confirmar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }


    public void atualizarFotoUsuario(Uri url){
        boolean retorno = UsuarioFirebase.atualizarFotoUsuario(url);

        if (retorno){
            usuarioLogado.setFoto(url.toString());
            usuarioLogado.atualizar();

            Toast.makeText(ConfiguracoesActivity.this,"Foto atualizada!",
                    Toast.LENGTH_SHORT).show();
        }

    }
}

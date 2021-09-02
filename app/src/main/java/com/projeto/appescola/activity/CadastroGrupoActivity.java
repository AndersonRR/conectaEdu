package com.projeto.appescola.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.projeto.appescola.R;
import com.projeto.appescola.adapter.GrupoSelecionadoAdapter;
import com.projeto.appescola.conifg.ConfiguracaoFirebase;
import com.projeto.appescola.helper.UsuarioFirebase;
import com.projeto.appescola.modelo.Grupo;
import com.projeto.appescola.modelo.Usuario;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.projeto.appescola.helper.DecodificadorDeImagem.decodeUriToBitmap;

public class CadastroGrupoActivity extends AppCompatActivity {

    private List<Usuario> listaMembrosSelecionados = new ArrayList<>();
    private TextView textTotalMembros;
    private EditText editNomeGrupo;
    private CircleImageView imageGrupo;
    private GrupoSelecionadoAdapter grupoSelecionadoAdapter;
    private RecyclerView recyclerMembrosSelecionados;
    private ProgressBar progressoCarregandoFoto;
    private Uri imagemSelecionada;
    private StorageReference storageReference;
    private Grupo grupo;
    private FloatingActionButton fabSalvarGrupo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_grupo);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.novo_grupo)); // string exibida na toolbar ao criar um grupo
        toolbar.setSubtitle(getResources().getString(R.string.descricao_criar_grupo)); // subtítulo da toolbar na criação de grupos
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Iniciando os componentes gráficos
        textTotalMembros = findViewById(R.id.textTotalMembros);
        editNomeGrupo    = findViewById(R.id.editNomeGrupo);
        imageGrupo       = findViewById(R.id.imageGrupo);
        recyclerMembrosSelecionados  = findViewById(R.id.recyclerMembrosGrupo);
        progressoCarregandoFoto      = findViewById(R.id.progressFotoGrupo);
        fabSalvarGrupo   = findViewById(R.id.fabNovaNoticia);
        editNomeGrupo    = findViewById(R.id.editNomeGrupo);

        //Configurações iniciais
        progressoCarregandoFoto.setVisibility(View.GONE);
        storageReference = ConfiguracaoFirebase.getFirebaseStorage();
        grupo = new Grupo();

        //Configurar evento de clique na foto do grupo
        imageGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .start(CadastroGrupoActivity.this);
            }
        });

        //Recuperar lista de membros recebida
        if (getIntent().getExtras() != null){
            List<Usuario> membros = (List<Usuario>) getIntent().getExtras().getSerializable("membros");
            listaMembrosSelecionados.addAll(membros);

            textTotalMembros.setText("Membros: " + listaMembrosSelecionados.size());
        }

        //Configurar adapter membros selecionados
        grupoSelecionadoAdapter = new GrupoSelecionadoAdapter(listaMembrosSelecionados, getApplicationContext());

        //Configurar recycler membros selecionados
        RecyclerView.LayoutManager layoutManagerHorizontal = new LinearLayoutManager(
                getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        );
        recyclerMembrosSelecionados.setLayoutManager(layoutManagerHorizontal);
        recyclerMembrosSelecionados.setHasFixedSize(true);
        recyclerMembrosSelecionados.setAdapter(grupoSelecionadoAdapter);

        //Configurar floating action button para salvar o grupo
        fabSalvarGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String nomeGrupo = editNomeGrupo.getText().toString();

                if (nomeGrupo.isEmpty()){
                    Toast.makeText(CadastroGrupoActivity.this, "Digite um nome para o Grupo!",
                            Toast.LENGTH_SHORT).show();
                }else {
                    String alf[] = {"a", "b", "c", "d", "f", "g", "h", "i", "j", "k", "l", "m",
                            "n", "o", "p", "q", "r", "s", "t", "u", "v", "y", "w", "x", "z",
                            "1","2","3","4","5","6","7","8","9","0"};

                    String aux = nomeGrupo.toLowerCase();
                    boolean okay = false;
                    for (String s : alf) {
                        if (aux.contains(s)) {
                            okay = true;
                            break;
                        }
                    }

                    if (okay){
                        //adicionar usuário atual ao grupo
                        listaMembrosSelecionados.add(UsuarioFirebase.getDadosUsuario());

                        //salvar usuários no grupo, incluindo o usuário atual
                        grupo.setMembros(listaMembrosSelecionados);

                        grupo.setNome(nomeGrupo);
                        grupo.salvar();

                        Intent i = new Intent(CadastroGrupoActivity.this, ChatActivity.class);
                        i.putExtra("chatGrupo", grupo);
                        startActivity(i);
                    }else {
                        Toast.makeText(CadastroGrupoActivity.this, "Nome do grupo inválido!",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    //tratando ação após escolher a imagem do grupo
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                this.imagemSelecionada = resultUri;
                Bitmap imagemBitmap = decodeUriToBitmap(this, resultUri);
                imageGrupo.setVisibility(View.GONE); //esconde a foto
                progressoCarregandoFoto.setVisibility(View.VISIBLE); //exibe no lugar da foto o progresso

                //Recuperar dados da imagem para o firebase
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                imagemBitmap.compress(Bitmap.CompressFormat.JPEG, 55, baos);
                byte[] dadosImagem = baos.toByteArray();

                //salvar imagem no firebase
                final StorageReference imagemRef = storageReference
                        .child("imagens")
                        .child("grupos")
                        .child(grupo.getId() + ".jpeg");

                UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressoCarregandoFoto.setVisibility(View.GONE);
                        imageGrupo.setVisibility(View.VISIBLE);
                        Toast.makeText(CadastroGrupoActivity.this,"Erro ao fazer upload da imagem.",
                                Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressoCarregandoFoto.setVisibility(View.GONE);
                        imageGrupo.setImageURI(imagemSelecionada);
                        imageGrupo.setVisibility(View.VISIBLE);
                        Toast.makeText(CadastroGrupoActivity.this,"Sucesso ao fazer upload da imagem.",
                                Toast.LENGTH_SHORT).show();

                        //salvando a foto no objeto grupo
                        Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                        firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                grupo.setFoto(uri.toString());
                            }
                        });
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}

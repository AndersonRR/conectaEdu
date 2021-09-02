package com.projeto.appescola.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.projeto.appescola.R;
import com.projeto.appescola.adapter.MensagensAdapter;
import com.projeto.appescola.conifg.ConfiguracaoFirebase;
import com.projeto.appescola.helper.Base64Custom;
import com.projeto.appescola.helper.UsuarioFirebase;
import com.projeto.appescola.modelo.Conversa;
import com.projeto.appescola.modelo.Grupo;
import com.projeto.appescola.modelo.Mensagem;
import com.projeto.appescola.modelo.Usuario;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private TextView textViewNomeChat;
    private CircleImageView circleImageFotoChat;
    private Usuario usuarioDestinatario;
    private Usuario usuarioRemetente;
    private EditText editMensagem;
    private ImageView imageCamera;
    private RecyclerView recyclerMensagens;
    private MensagensAdapter adapter;
    private List<Mensagem> mensagens = new ArrayList<>();
    private DatabaseReference database;
    private DatabaseReference mensagensRef;
    private DatabaseReference msgNaoLidasRef;
    private DatabaseReference msgNaoLidasRefAux;
    private ChildEventListener childEventListenerMensagens;
    private Grupo grupo;
    private int mensagensNaoLidas;
    private int indexMsgNaoLida;
    private List<Integer> msgMembrosNaoLidas = new ArrayList<>();

    //identificador usuário remetente e destinatário
    private String idUsuarioRemetente;
    private String idUsuarioDestinatario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //configurando toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getWindow().setBackgroundDrawableResource(R.drawable.bg_chat);

        //configurações iniciais
        textViewNomeChat = findViewById(R.id.textViewNomeChat);
        circleImageFotoChat = findViewById(R.id.circleImageFotoChat);
        editMensagem = findViewById(R.id.editMensagem);
        recyclerMensagens = findViewById(R.id.recyclerMensagens);
        imageCamera = findViewById(R.id.imageCamera);
        mensagensNaoLidas = 0;

        //recuperar dados do usuário remetente
        idUsuarioRemetente = UsuarioFirebase.getIdentificadorUsuario();
        usuarioRemetente = UsuarioFirebase.getDadosUsuario();

        //recuperar dados do usuário destinatário
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey("chatGrupo")) {

                /* início do código para configurar conversa em grupo*/
                grupo = (Grupo) bundle.getSerializable("chatGrupo");
                idUsuarioDestinatario = grupo.getId();
                textViewNomeChat.setText(grupo.getNome());

                if (grupo.getFoto() != null) {
                    Uri uri = Uri.parse(grupo.getFoto());
                    Glide.with(ChatActivity.this).load(uri).into(circleImageFotoChat);
                } else {
                    circleImageFotoChat.setImageResource(R.drawable.foto_padrao);
                }

            } else {

                /* início do código para configurar conversa para 2 usuários*/
                usuarioDestinatario = (Usuario) bundle.getSerializable("chatContato");
                idUsuarioDestinatario = Base64Custom.codificarBase64(usuarioDestinatario.getEmail());

                DatabaseReference usuarioRef = ConfiguracaoFirebase.getFirebaseDatabase().child("usuarios")
                        .child(idUsuarioDestinatario);

                //quando carregar uma nova foto ou novo nome e chamar o onBind, irá atualizar
                usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Usuario usuario = dataSnapshot.getValue(Usuario.class);
                        textViewNomeChat.setText(usuario.getNome());

                        if (usuario.getFoto() != null) {
                            Uri uri = Uri.parse(usuario.getFoto());
                            Glide.with(ChatActivity.this).load(uri).into(circleImageFotoChat);
                        } else {
                            circleImageFotoChat.setImageResource(R.drawable.foto_padrao);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                textViewNomeChat.setText(usuarioDestinatario.getNome());

                String foto = usuarioDestinatario.getFoto();
                if (foto != null) {
                    Uri uri = Uri.parse(foto);
                    Glide.with(ChatActivity.this)
                            .load(uri)
                            .into(circleImageFotoChat);
                } else {
                    circleImageFotoChat.setImageResource(R.drawable.foto_padrao);
                }
                /* fim do código para configurar conversa para 2 usuários*/
            }

        }

        if (grupo != null) { //recuperar mensagens nao lidas de cada membro
            //inicializando lista de msgNaoLidas dos membros
            for (Usuario usuario : grupo.getMembros()) {
                msgMembrosNaoLidas.add(0);
            }
            msgNaoLidasRef = ConfiguracaoFirebase.getFirebaseDatabase().child("conversas")
                    .child(idUsuarioRemetente)
                    .child(idUsuarioDestinatario)
                    .child("mensagensNaoLidas");

            msgNaoLidasRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    msgMembrosNaoLidas.clear();
                    if (dataSnapshot.getValue() != null) {
                        for (Usuario usuario : grupo.getMembros()) {
                            msgNaoLidasRefAux = ConfiguracaoFirebase.getFirebaseDatabase().child("conversas")
                                    .child(Base64Custom.codificarBase64(usuario.getEmail()))
                                    .child(idUsuarioDestinatario)
                                    .child("mensagensNaoLidas");

                            msgNaoLidasRefAux.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.getValue() != null) {
                                            int naoLidas = dataSnapshot.getValue(int.class);
                                            msgMembrosNaoLidas.add(naoLidas);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } else {//recuperar as mensagens não lidas do destinatario normal
            msgNaoLidasRef = ConfiguracaoFirebase.getFirebaseDatabase().child("conversas")
                    .child(idUsuarioDestinatario)
                    .child(idUsuarioRemetente)
                    .child("mensagensNaoLidas");

            msgNaoLidasRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        mensagensNaoLidas = dataSnapshot.getValue(int.class);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        //Acao enviar com enter do teclado
        editMensagem.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN){
                    if (i == KeyEvent.KEYCODE_DPAD_CENTER || i == KeyEvent.KEYCODE_ENTER){
                        enviarMensagem(view);
                        return true;
                    }
                }
                return false;
            }
        });

        //Configurar o adapter
        adapter = new MensagensAdapter(mensagens, getApplicationContext());

        //Configurar o recyclerView
        //RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(false);
        //layoutManager.scrollToPosition(layoutManager.findLastVisibleItemPosition());

        recyclerMensagens.setLayoutManager(layoutManager);
        //recyclerMensagens.setHasFixedSize(true);
        recyclerMensagens.setAdapter(adapter);

        //configurando mensagens ref global
        database = ConfiguracaoFirebase.getFirebaseDatabase();
        mensagensRef = database.child("mensagens")
                .child(idUsuarioRemetente)
                .child(idUsuarioDestinatario);

        //Evento de clique na camera
        imageCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (i.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(i, 1);
                }
            }
        });

        recuperarMensagens();
    }

    public void enviarMensagem(View v) {
        String textoMensagem = editMensagem.getText().toString();
        if (!textoMensagem.isEmpty()) {

            if (usuarioDestinatario != null) { // se verdadeiro, não é uma mensagem de grupo

                Mensagem mensagem = new Mensagem();
                mensagem.setIdUsuario(idUsuarioRemetente);
                mensagem.setMensagem(textoMensagem);

                //salvar mensagem para o remetente
                salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);

                //salvar mensagem para o destinatario
                salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem);

                //salvar conversa para remetente
                salvarConversa(idUsuarioRemetente, idUsuarioDestinatario, usuarioDestinatario, mensagem, false, 0);

                //salvar conversa para destinatário
                salvarConversa(idUsuarioDestinatario, idUsuarioRemetente, usuarioRemetente, mensagem, false, 0);

                //limpar texto
                editMensagem.setText("");
                //recyclerMensagens.scrollToPosition(mensagens.size());

            } else { // senão, é uma mensagem para grupo

                int cont = 0;
                for (Usuario membro : grupo.getMembros()) {
                    String idRemetenteGrupo = Base64Custom.codificarBase64(membro.getEmail());
                    String idUsuarioLogadoGrupo = UsuarioFirebase.getIdentificadorUsuario();

                    Mensagem mensagem = new Mensagem();
                    mensagem.setIdUsuario(idUsuarioLogadoGrupo);
                    mensagem.setMensagem(textoMensagem);
                    mensagem.setNome(usuarioRemetente.getNome()); //nome que ficará visível no grupo para os outros

                    //salvar mensagem para o membro
                    //nesse caso, o objeto "idUsuarioDestinatario" já está configurado como grupo.getId()
                    salvarMensagem(idRemetenteGrupo, idUsuarioDestinatario, mensagem);

                    //salvar conversa para destinatário
                    salvarConversa(idRemetenteGrupo, idUsuarioDestinatario, usuarioDestinatario, mensagem, true, msgMembrosNaoLidas.get(cont));

                    cont++;
                }
                //limpar o texto
                editMensagem.setText("");
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //recuperarMensagens();

    }

    @Override
    protected void onStop() {
        super.onStop();
        finish(); //forçar o app a forçar o ChatActivity toda vez que sair de foco
    }

    @Override
    protected void onPause() {
        super.onPause();
        mensagensRef.removeEventListener(childEventListenerMensagens);

        //zerar as mensagens nao lidas do usuário atual
        if (mensagens.size() > 0) { //pra ter certeza que pelo menos uma conversa foi criada

            DatabaseReference msgNaoLidasRefUsuarioAtual = ConfiguracaoFirebase.getFirebaseDatabase().child("conversas");
            msgNaoLidasRefUsuarioAtual.child(idUsuarioRemetente)
                    .child(idUsuarioDestinatario)
                    .child("mensagensNaoLidas")
                    .setValue(0);
        }
    }


    @Override
    public void onBackPressed() {
        finish();
    }

    private void salvarMensagem(String idRemetente, String idDestinatario, Mensagem msg) {
        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference mensagensRef = database.child("mensagens");

        mensagensRef.child(idRemetente)
                .child(idDestinatario)
                .push()   //cria identificador único no firebase, não sobrepoem
                .setValue(msg);

    }

    private void salvarConversa(String idRemetente, String idDestinatario, Usuario usuarioExibicao, Mensagem msg, boolean isGroup, int nLidas) {

        Conversa conversa = new Conversa();
        conversa.setIdRemetente(idRemetente);
        conversa.setIdDestinatario(idDestinatario);
        conversa.setUltimaMensagem(msg.getMensagem());

        if (isGroup) {
            mensagensNaoLidas = nLidas + 1;
        } else if (!idRemetente.equals(idUsuarioRemetente) && !isGroup) {
            mensagensNaoLidas += 1;
        }

        if (isGroup) { //se for um grupo

            conversa.setIsGroup("true");
            conversa.setGrupo(grupo);

        } else {

            conversa.setUsuarioExibicao(usuarioExibicao);
            conversa.setIsGroup("false");
        }

        conversa.setMensagensNaoLidas(mensagensNaoLidas);
        conversa.salvar();
    }

    public void recuperarMensagens() {
        childEventListenerMensagens = mensagensRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Mensagem mensagem = dataSnapshot.getValue(Mensagem.class);
                mensagens.add(mensagem);
                adapter.notifyDataSetChanged();

                recyclerMensagens.scrollToPosition(mensagens.size() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}

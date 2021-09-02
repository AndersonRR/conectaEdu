package com.projeto.appescola.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.projeto.appescola.R;
import com.projeto.appescola.adapter.ContatosAdapter;
import com.projeto.appescola.adapter.GrupoSelecionadoAdapter;
import com.projeto.appescola.conifg.ConfiguracaoFirebase;
import com.projeto.appescola.helper.RecyclerItemClickListener;
import com.projeto.appescola.helper.UsuarioFirebase;
import com.projeto.appescola.modelo.Usuario;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GrupoActivity extends AppCompatActivity {

    private RecyclerView recyclerMembrosSelecionados, recyclerMembros;
    private ContatosAdapter contatosAdapter;
    private GrupoSelecionadoAdapter grupoSelecionadoAdapter;
    private List<Usuario> listaMembros = new ArrayList<>();
    private List<Usuario> listaMembrosSelecionados = new ArrayList<>();
    private ValueEventListener valueEventListenerMembros;
    private DatabaseReference usuariosRef;
    private FirebaseUser usuarioAtual;
    private Toolbar toolbar;
    private FloatingActionButton fabAvancarCadastro;

    public void atualizarMembrosToolbar(){

        int totalSelecionados = listaMembrosSelecionados.size();
        int total = listaMembros.size() + totalSelecionados;

        toolbar.setSubtitle(totalSelecionados + " de " + total + " selecionados");

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grupo);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Novo Grupo");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Configurações iniciais
        recyclerMembrosSelecionados = findViewById(R.id.recyclerMembrosSelecionados);
        recyclerMembros = findViewById(R.id.recyclerMembros);
        fabAvancarCadastro = findViewById(R.id.fabNovaNoticia);

        usuariosRef = ConfiguracaoFirebase.getFirebaseDatabase().child("usuarios");
        usuarioAtual = UsuarioFirebase.getUsuarioAtual();

        //Configura adapter para contatos
        contatosAdapter = new ContatosAdapter(listaMembros, getApplicationContext());

        //Configura recyclerView pra contatos
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerMembros.setLayoutManager(layoutManager);
        recyclerMembros.setHasFixedSize(true);
        recyclerMembros.setAdapter(contatosAdapter);

        recyclerMembros.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getApplicationContext(),
                        recyclerMembros,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Usuario usuarioSelecionado = listaMembros.get(position);

                                //removendo da lista de membros
                                listaMembros.remove(usuarioSelecionado);
                                contatosAdapter.notifyDataSetChanged();

                                //adicionando na outra lista (superior - selecionados)
                                listaMembrosSelecionados.add(usuarioSelecionado);
                                grupoSelecionadoAdapter.notifyDataSetChanged();

                                atualizarMembrosToolbar();
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );

        //Configurar adapter contatos selecionados
        grupoSelecionadoAdapter = new GrupoSelecionadoAdapter(listaMembrosSelecionados, getApplicationContext());

        //Configurar recycler contatos selecionados
        RecyclerView.LayoutManager layoutManagerHorizontal = new LinearLayoutManager(
                getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        );
        recyclerMembrosSelecionados.setLayoutManager(layoutManagerHorizontal);
        recyclerMembrosSelecionados.setHasFixedSize(true);
        recyclerMembrosSelecionados.setAdapter(grupoSelecionadoAdapter);

        recyclerMembrosSelecionados.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getApplicationContext(),
                        recyclerMembrosSelecionados,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Usuario usuarioSelecionado = listaMembrosSelecionados.get(position);

                                //Remover da lista dos membros selecionados
                                listaMembrosSelecionados.remove(usuarioSelecionado);
                                grupoSelecionadoAdapter.notifyDataSetChanged();

                                //Devolve para a lista dos contatos
                                listaMembros.add(usuarioSelecionado);
                                contatosAdapter.notifyDataSetChanged();

                                atualizarMembrosToolbar();
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );

        //Configurar fab avançar cadastro
        fabAvancarCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (listaMembrosSelecionados.size() < 1) {
                    Toast.makeText(GrupoActivity.this, "Nenhum membro selecionado!",
                            Toast.LENGTH_SHORT).show();
                }else{
                    Intent i = new Intent(GrupoActivity.this, CadastroGrupoActivity.class);
                    i.putExtra("membros", (Serializable) listaMembrosSelecionados);
                    startActivity(i);
                }
            }
        });
        recuperarContatos();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        usuariosRef.removeEventListener(valueEventListenerMembros);
    }

    public void recuperarContatos(){

        valueEventListenerMembros = usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //limpa os contatos para não duplicar
                //listaMembros.clear();

                String emailUsuarioAtual = usuarioAtual.getEmail();

                for (DataSnapshot dados : dataSnapshot.getChildren()){
                    Usuario usuario = dados.getValue(Usuario.class);

                    if (!emailUsuarioAtual.equals(usuario.getEmail())){

                    /*aqui podemos fazer a verificação para colocar contatos somente da escola
                    ou contatos que o usuário adicionou, por exemplo*/

                        listaMembros.add(usuario);
                    }
                }

                contatosAdapter.notifyDataSetChanged();
                atualizarMembrosToolbar();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

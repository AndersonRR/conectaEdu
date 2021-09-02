package com.projeto.appescola.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.projeto.appescola.R;
import com.projeto.appescola.activity.ChatActivity;
import com.projeto.appescola.activity.GrupoActivity;
import com.projeto.appescola.adapter.ContatosAdapter;
import com.projeto.appescola.conifg.ConfiguracaoFirebase;
import com.projeto.appescola.helper.RecyclerItemClickListener;
import com.projeto.appescola.helper.UsuarioFirebase;
import com.projeto.appescola.modelo.Usuario;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContatosFragment extends Fragment {

    private RecyclerView recyclerViewListContatos;
    private ContatosAdapter adapter;
    private ArrayList<Usuario> listaContatos = new ArrayList<>();
    private DatabaseReference usuariosRef;
    private ValueEventListener valueEventListenerContatos;
    private FirebaseUser usuarioAtual;

    public ContatosFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contatos, container, false);

        //configurações iniciais
        recyclerViewListContatos = view.findViewById(R.id.recycleViewListaContatos);
        usuariosRef = ConfiguracaoFirebase.getFirebaseDatabase().child("usuarios");
        usuarioAtual = UsuarioFirebase.getUsuarioAtual();

        //configurando adapter
        adapter = new ContatosAdapter(listaContatos, getActivity());

        //configurando recycle
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewListContatos.setLayoutManager(layoutManager);
        recyclerViewListContatos.setHasFixedSize(true);
        recyclerViewListContatos.setAdapter(adapter);

        //configurando evento de clique no recycleView
        recyclerViewListContatos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
                        recyclerViewListContatos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Usuario usuarioSelecionado;

                                List<Usuario> listaContatosAtualizada = adapter.getContatos();
                                usuarioSelecionado = listaContatosAtualizada.get(position);

                                boolean cabecalho = usuarioSelecionado.getEmail().isEmpty();

                                if (cabecalho) {
                                    Intent i = new Intent(getActivity(), GrupoActivity.class);
                                    startActivity(i);
                                } else {
                                    Intent i = new Intent(getActivity(), ChatActivity.class);
                                    i.putExtra("chatContato", usuarioSelecionado);
                                    startActivity(i);
                                }
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

        return view;
    }

    public void atualizarContatos() {
        adapter = new ContatosAdapter(listaContatos, getActivity());
        recyclerViewListContatos.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void pesquisarContatos(String texto) {

        List<Usuario> listaContatosPesquisa = new ArrayList<>();

        for (Usuario usuario : listaContatos) {

            if (usuario.getNome().toLowerCase().contains(texto)) {
                listaContatosPesquisa.add(usuario);
            }
        }
        adapter = new ContatosAdapter(listaContatosPesquisa, getActivity());
        recyclerViewListContatos.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarContatos();
    }

    @Override
    public void onStop() {
        super.onStop();
        usuariosRef.removeEventListener(valueEventListenerContatos);
    }

    public void recuperarContatos() {

        valueEventListenerContatos = usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //limpa os contatos para não duplicar
                listaContatos.clear();

                /*Define o usuário com e-mail vazio. Em caso de e-mail vazio o usuário será
                 * utilizado como cabeçalho, exibindo novo grupo*/

                Usuario itemGrupo = new Usuario();
                itemGrupo.setNome("Novo Grupo");
                itemGrupo.setEmail("");

                listaContatos.add(itemGrupo);

                String emailUsuarioAtual = usuarioAtual.getEmail();

                for (DataSnapshot dados : dataSnapshot.getChildren()) {
                    //Log.i("FIREBASE", dataSnapshot.getValue().toString());
                    Usuario usuario = dados.getValue(Usuario.class);

                    if (!emailUsuarioAtual.equals(usuario.getEmail())) {

                    /*aqui podemos fazer a verificação para colocar contatos somente da escola
                    ou contatos que o usuário adicionou, por exemplo*/

                        listaContatos.add(usuario);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}

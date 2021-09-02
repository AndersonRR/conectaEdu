package com.projeto.appescola.fragment;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.projeto.appescola.R;
import com.projeto.appescola.activity.ChatActivity;
import com.projeto.appescola.adapter.ConversasAdapter;
import com.projeto.appescola.conifg.ConfiguracaoFirebase;
import com.projeto.appescola.helper.RecyclerItemClickListener;
import com.projeto.appescola.helper.UsuarioFirebase;
import com.projeto.appescola.modelo.Conversa;
import com.projeto.appescola.modelo.Usuario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConversasFragment extends Fragment {
    private RecyclerView recyclerListaConversas;
    private List<Conversa> listaConversas = new ArrayList<>();
    private ConversasAdapter adapter;
    private DatabaseReference database;
    private DatabaseReference conversasRef;
    private ChildEventListener childEventListenerConversas;
    private ValueEventListener valueEventListenerConversas;

    public ConversasFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversas, container, false);

        //configurações iniciais
        recyclerListaConversas = view.findViewById(R.id.recyclerListaConversas);

        //configurando adapter
        adapter = new ConversasAdapter(listaConversas, getActivity());

        //configurando recyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerListaConversas.setLayoutManager(layoutManager);
        recyclerListaConversas.setHasFixedSize(true);
        recyclerListaConversas.setAdapter(adapter);

        //configurar evento de clique
        recyclerListaConversas.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
                        recyclerListaConversas,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Conversa conversaSelecionada;

                                //Cria uma nova lista para pegar sempre atualizado do adapter
                                List<Conversa> listaConversaAtualizada = adapter.getConversas();
                                conversaSelecionada = listaConversaAtualizada.get(position);
                                Log.i("TESTE_POSICAO", "Posição: " + position );

                                if (conversaSelecionada.getIsGroup().equals("true")) {
                                    Intent i = new Intent(getActivity(), ChatActivity.class);
                                    i.putExtra("chatGrupo", conversaSelecionada.getGrupo());
                                    startActivity(i);

                                } else {
                                    Intent i = new Intent(getActivity(), ChatActivity.class);
                                    i.putExtra("chatContato", conversaSelecionada.getUsuarioExibicao());
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

        //Configura conversasRef
        database = ConfiguracaoFirebase.getFirebaseDatabase();
        String identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();
        conversasRef = database.child("conversas")
                .child(identificadorUsuario);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarConversas();
    }

    @Override
    public void onStop() {
        super.onStop();
        conversasRef.removeEventListener(valueEventListenerConversas);
    }

    public void recuperarConversass() {

        childEventListenerConversas = conversasRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                //recuperar conversas
                Conversa conversa = dataSnapshot.getValue(Conversa.class);
                listaConversas.add(conversa);
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Conversa conversa = dataSnapshot.getValue(Conversa.class);
                String destinatarioAux = conversa.getIdDestinatario();
                List<Conversa> listaAux = listaConversas;

                for (Conversa c : listaAux) {
                    if (c.getIdDestinatario().equals(destinatarioAux)) {
                        listaConversas.remove(c);
                        listaConversas.add(conversa);
                    }
                }

                adapter.notifyDataSetChanged();
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

    public void atualizarConversas() {
        adapter = new ConversasAdapter(listaConversas, getActivity());
        recyclerListaConversas.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void pesquisarConversas(String texto) {

        List<Conversa> listaConversasPesquisa = new ArrayList<>();

        for (Conversa conversa : listaConversas) {

            if (conversa.getIsGroup().equals("false")) { //se for conversa normal
                if (conversa.getUsuarioExibicao().getNome().toLowerCase().contains(texto)) {
                    listaConversasPesquisa.add(conversa);
                }
            } else { //se for conversa em grupo
                if (conversa.getGrupo().getNome().toLowerCase().contains(texto)) {
                    listaConversasPesquisa.add(conversa);
                }
            }
        }
        adapter = new ConversasAdapter(listaConversasPesquisa, getActivity());
        recyclerListaConversas.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    public void recuperarConversas() {

        valueEventListenerConversas = conversasRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //limpa as conversas para não duplicar
                listaConversas.clear();
                List<Conversa> listaConversaAuxiliar = new ArrayList<>();

                for (DataSnapshot dados : dataSnapshot.getChildren()) {
                    Conversa conversa = dados.getValue(Conversa.class); //conversa para ser adicionada na "listaConversas"
                    String idAtual = conversa.getIdRemetente();
                    String idDest = conversa.getIdDestinatario();

                    /*Trecho comentado pois dá conflito com o .sort. Ele atualiza as config do usuárioDestino

                    DatabaseReference usuarioRef = ConfiguracaoFirebase.getFirebaseDatabase().child("usuarios")
                            .child(idDest);

                    usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            //se o destinatário teve alterações, ele vai atualizar no firebase
                            Usuario usuario = dataSnapshot.getValue(Usuario.class);
                            conversa.setUsuarioExibicao(usuario);
                            conversa.atualizar();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    fim do trecho*/

                    //se o usuário ativo for o rementente ou destinatário a conversa será listada
                    if (UsuarioFirebase.getIdentificadorUsuario().equals(idAtual) ||
                            UsuarioFirebase.getIdentificadorUsuario().equals(idDest)) {

                        listaConversaAuxiliar.add(conversa);
                    }
                }
                Collections.sort(listaConversaAuxiliar);

                for (Conversa c : listaConversaAuxiliar){
                    listaConversas.add(c);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

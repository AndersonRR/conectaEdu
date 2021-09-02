package com.projeto.appescola.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.auth.data.model.IntentRequiredException;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.projeto.appescola.R;
import com.projeto.appescola.activity.NoticiaActivity;
import com.projeto.appescola.adapter.NoticiasAdapter;
import com.projeto.appescola.conifg.ConfiguracaoFirebase;
import com.projeto.appescola.helper.UsuarioFirebase;
import com.projeto.appescola.modelo.Noticia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class NoticiasFragment extends Fragment {
    private RecyclerView recyclerNoticias;
    private List<Noticia> listaNoticias = new ArrayList<>();
    private NoticiasAdapter adapter;
    private FloatingActionButton fabNovaNoticia;
    private DatabaseReference database;
    private DatabaseReference noticiasRef;
    private ChildEventListener childEventListenerConversas;
    private ValueEventListener valueEventListenerConversas;

    public NoticiasFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_noticias, container, false);

        //Configurações iniciais
        recyclerNoticias = view.findViewById(R.id.recyclerNoticias);
        fabNovaNoticia = view.findViewById(R.id.fabNovaNoticia);

        //Configurar o adapter
        adapter = new NoticiasAdapter(listaNoticias, getActivity());

        //Configurar o recyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerNoticias.setLayoutManager(layoutManager);
        recyclerNoticias.setHasFixedSize(true);
        recyclerNoticias.setAdapter(adapter);

        //Configurar a referencia do firebase para notícias
        database = ConfiguracaoFirebase.getFirebaseDatabase();
        noticiasRef = database.child("noticias");

        //configurar clique no fab
        fabNovaNoticia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), NoticiaActivity.class);
                startActivity(i);
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarNoticias();
    }

    @Override
    public void onStop() {
        super.onStop();
        noticiasRef.removeEventListener(valueEventListenerConversas);
    }

    public void recuperarNoticias() {

        valueEventListenerConversas = noticiasRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //limpa as noticias para não duplicar
                listaNoticias.clear();

                for (DataSnapshot dados : dataSnapshot.getChildren()) {
                    Noticia noticia = dados.getValue(Noticia.class);

                    listaNoticias.add(noticia);

                }

                //ordenar lista de noticias aqui (considerando a data)
                Collections.sort(listaNoticias);

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void atualizarNoticias() {
        adapter = new NoticiasAdapter(listaNoticias, getActivity());
        recyclerNoticias.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void pesquisarNoticias(String texto) {

        List<Noticia> listaNoticiasPesquisa = new ArrayList<>();

        for (Noticia noticia : listaNoticias) {

            if (noticia.getDescricao().toLowerCase().contains(texto) ||
                noticia.getAutorPublicacao().getNome().toLowerCase().contains(texto) ||
                noticia.getDataPublicacao().toString().toLowerCase().contains(texto)) {
                listaNoticiasPesquisa.add(noticia);
            }
        }

        adapter = new NoticiasAdapter(listaNoticiasPesquisa, getActivity());
        recyclerNoticias.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }
}

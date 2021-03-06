package com.projeto.appescola.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.projeto.appescola.R;
import com.projeto.appescola.modelo.Usuario;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GrupoSelecionadoAdapter extends RecyclerView.Adapter<GrupoSelecionadoAdapter.MyViewHolder> {

    private List<Usuario> contatosSelecionados;
    private Context context;

    public GrupoSelecionadoAdapter(List<Usuario> listaContatos, Context c) {
        this.contatosSelecionados = listaContatos;
        this.context = c;
    }

    @NonNull
    @Override
    public GrupoSelecionadoAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemLista = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_grupo_selecionado, viewGroup, false);
        return new GrupoSelecionadoAdapter.MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull GrupoSelecionadoAdapter.MyViewHolder myViewHolder, int i) {
        Usuario usuario = contatosSelecionados.get(i);

        String[] nome = usuario.getNome().split(" ");

        myViewHolder.nome.setText(nome[0]);

        if (usuario.getFoto() != null){
            Uri uri = Uri.parse(usuario.getFoto());
            Glide.with(context)
                    .load(uri)
                    .into(myViewHolder.foto);
        }else {
            myViewHolder.foto.setImageResource(R.drawable.foto_padrao);
        }
    }

    @Override
    public int getItemCount() {
        return contatosSelecionados.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        CircleImageView foto;
        TextView nome;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            foto  = itemView.findViewById(R.id.imageViewFotoMembroSelecionado);
            nome  = itemView.findViewById(R.id.textViewNomeMembroSelecionado);
        }
    }

}

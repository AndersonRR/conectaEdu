package com.projeto.appescola.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.projeto.appescola.R;
import com.projeto.appescola.helper.UsuarioFirebase;
import com.projeto.appescola.modelo.Mensagem;

import java.util.List;

public class MensagensAdapter extends RecyclerView.Adapter<MensagensAdapter.MyViewHolder> {

    private List<Mensagem> mensagens;
    private Context context;
    private static final int REMETENTE    = 0;
    private static final int DESTINATARIO = 1;

    public MensagensAdapter(List<Mensagem> mensagens, Context context) {
        this.mensagens = mensagens;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View item = null;
        if (i == REMETENTE){
            item = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.adapter_mensagem_remetente, viewGroup, false);
        }else {
            item = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.adapter_mensagem_destinatario, viewGroup, false);
        }

        return new MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {

        Mensagem mensagem = mensagens.get(i);
        String msg    = mensagem.getMensagem();
        String imagem = mensagem.getImagem();

        //se o usuario mandou uma foto nao exibe mensagem e vice e versa
        if (imagem != null){
            Uri uri = Uri.parse(imagem);
            Glide.with(context)
                    .load(uri)
                    .into(myViewHolder.imagem);

            String nome = mensagem.getNome();
            if (!nome.isEmpty() && !mensagem.getIdUsuario().equals(UsuarioFirebase.getIdentificadorUsuario())){

                /*se não estiver vazio, se trata de um grupo e precisamos exibir
                se for o próprio usuário, não exibimos o nome pra ele mesmo */

                myViewHolder.nome.setText(nome);

            }else {
                myViewHolder.nome.setVisibility(View.GONE);
            }

            //esconder texto
            myViewHolder.mensagem.setVisibility(View.GONE);

        }else {
            myViewHolder.mensagem.setText(msg);

            String nome = mensagem.getNome();
            if (!nome.isEmpty() && !mensagem.getIdUsuario().equals(UsuarioFirebase.getIdentificadorUsuario())){

                /*se não estiver vazio, se trata de um grupo e precisamos exibir
                se for o próprio usuário, não exibimos o nome pra ele mesmo */

                myViewHolder.nome.setText(nome);

            }else {
                myViewHolder.nome.setVisibility(View.GONE);
            }

            //esconder imagem
            myViewHolder.imagem.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return mensagens.size();
    }

    @Override
    public int getItemViewType(int position) {
        Mensagem mensagem = mensagens.get(position);
        String idUsuario  = UsuarioFirebase.getIdentificadorUsuario();

        if (idUsuario.equals(mensagem.getIdUsuario())){
            return REMETENTE;
        }
        return DESTINATARIO;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView mensagem;
        TextView nome;
        ImageView imagem;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            mensagem = itemView.findViewById(R.id.textMensagemTexto);
            imagem   = itemView.findViewById(R.id.imageMensagemFoto);
            nome     = itemView.findViewById(R.id.textNomeExibicaoGrupo);
        }
    }


}

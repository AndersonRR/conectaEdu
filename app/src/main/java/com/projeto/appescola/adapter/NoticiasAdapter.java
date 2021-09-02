package com.projeto.appescola.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.projeto.appescola.R;
import com.projeto.appescola.conifg.ConfiguracaoFirebase;
import com.projeto.appescola.helper.DecodificadorDeImagem;
import com.projeto.appescola.modelo.Noticia;

import org.joda.time.DateTime;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NoticiasAdapter extends RecyclerView.Adapter<NoticiasAdapter.MyViewHolder> {
    private List<Noticia> noticias;
    private Context context;
    private DatabaseReference database;

    public NoticiasAdapter(List<Noticia> noticias, Context context) {
        this.noticias = noticias;
        this.context = context;
    }

    public List<Noticia> getNoticias(){return this.noticias;}

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemLista = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_noticias,
                viewGroup, false);

        database = ConfiguracaoFirebase.getFirebaseDatabase();

        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        final Noticia noticia = noticias.get(i);

        //iniciando configuração da noticia
        myViewHolder.descricao.setText(noticia.getDescricao());
        myViewHolder.nomeAutorNoticia.setText(noticia.getAutorPublicacao().getNome());

        //configurar foto do usuário
        if (noticia.getAutorPublicacao().getFoto() != null) {
            Uri uri = Uri.parse(noticia.getAutorPublicacao().getFoto());
            Glide.with(context).load(uri).into(myViewHolder.fotoAutorNoticia);
        } else {
            myViewHolder.fotoAutorNoticia.setImageResource(R.drawable.foto_padrao);
        }

        //configurar foto da publicação (se existir, se não apenas oculta a foto)
        if (noticia.getImagem() != null ) {
            Uri uri = Uri.parse(noticia.getImagem());
            Glide.with(context).load(uri).into(myViewHolder.imagemNoticia);
        } else {
            myViewHolder.imagemNoticia.setVisibility(View.GONE);
        }

        //por último configurar a data da publicação
        //tratamento da data: se for no mesmo dia, mostra apenas a hora
        DateTime dataAtual    = new DateTime(new Date());
        DateTime dataNoticia = new DateTime(noticia.getDataPublicacao());

        if (dataAtual.getYear() == dataNoticia.getYear() &&
                dataAtual.getMonthOfYear() == dataNoticia.getMonthOfYear() &&
                dataAtual.getDayOfMonth() == dataNoticia.getDayOfMonth()){

            DecimalFormat formatoHora = new DecimalFormat("00");

            String hora, minutos;
            hora    = formatoHora.format(dataNoticia.getHourOfDay());
            minutos = formatoHora.format(dataNoticia.getMinuteOfHour());

            myViewHolder.dataPublicacao.setText(hora + ":" + minutos);

        }else{
            DecimalFormat formatoDia = new DecimalFormat("00");
            DecimalFormat formatoMes = new DecimalFormat("00");
            DecimalFormat formatoAno = new DecimalFormat("0000");

            String dia, mes, ano;
            dia = formatoDia.format(dataNoticia.getDayOfMonth());
            mes = formatoMes.format(dataNoticia.getMonthOfYear());
            ano = formatoAno.format(dataNoticia.getYear());

            myViewHolder.dataPublicacao.setText(dia + "/" + mes + "/" + ano);
        }

    }

    @Override
    public int getItemCount() {
        return noticias.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private CircleImageView fotoAutorNoticia;
        private ImageView imagemNoticia;
        private TextView nomeAutorNoticia, dataPublicacao, descricao;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            fotoAutorNoticia = itemView.findViewById(R.id.circleImageViewAutorNoticia);
            imagemNoticia    = itemView.findViewById(R.id.imageViewImagemNoticia);
            nomeAutorNoticia = itemView.findViewById(R.id.textViewAutorNoticia);
            descricao        = itemView.findViewById(R.id.textViewDescricaoNoticia);
            dataPublicacao   = itemView.findViewById(R.id.textViewDataPublicacao);
        }
    }
}

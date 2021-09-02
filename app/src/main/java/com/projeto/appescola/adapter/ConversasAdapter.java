package com.projeto.appescola.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.projeto.appescola.R;
import com.projeto.appescola.conifg.ConfiguracaoFirebase;
import com.projeto.appescola.modelo.Conversa;
import com.projeto.appescola.modelo.Grupo;
import com.projeto.appescola.modelo.Usuario;

import org.joda.time.DateTime;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversasAdapter extends RecyclerView.Adapter<ConversasAdapter.MyViewHolder> {
    private List<Conversa> conversas;
    private Context context;
    private DatabaseReference database;
    private DatabaseReference usuarioRef;
    private ValueEventListener valueEventListenerUsuarios;
    private Usuario usuario;
    private int contador;
    private final static int DURACAO_ANIMACAO = 700; // duração das animações em milesegundos

    public ConversasAdapter(List<Conversa> conversas, Context context) {
        this.conversas = conversas;
        this.context = context;
    }

    public List<Conversa> getConversas(){
        return this.conversas;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemLista = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.adapter_contatos, viewGroup, false);

        database = ConfiguracaoFirebase.getFirebaseDatabase();

        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, int i) {
        final Conversa conversa = conversas.get(i);
        contador = i;
        myViewHolder.ultimaMensagem.setText(conversa.getUltimaMensagem());

        if (conversa.getMensagensNaoLidas() == 0){ //se nao tiver msg nao lida, esconde o número
            myViewHolder.mensagensNaoLidas.setVisibility(View.GONE);
        }else{
            myViewHolder.mensagensNaoLidas.setText(String.valueOf(conversa.getMensagensNaoLidas()));
            myViewHolder.mensagensNaoLidas.setVisibility(View.VISIBLE);
        }

        //verificando se é uma conversa de grupo ou conversa entre dois usuários
        if (conversa.getIsGroup().equals("true")) { //grupo

            Grupo grupo = conversa.getGrupo();
            myViewHolder.nome.setText(grupo.getNome());

            if (grupo.getFoto() != null) {
                Uri uri = Uri.parse(grupo.getFoto());
                Glide.with(context).load(uri).into(myViewHolder.foto);
            } else {
                myViewHolder.foto.setImageResource(R.drawable.foto_padrao);
            }


        } else { //conversa normal (dois usuários)
            usuario = conversa.getUsuarioExibicao();
            usuarioRef = ConfiguracaoFirebase.getFirebaseDatabase().child("usuarios").child(conversa.getIdDestinatario());

            //quando carregar uma nova foto ou novo nome e chamar o onBind, irá atualizar
            usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    usuario = dataSnapshot.getValue(Usuario.class);
                    myViewHolder.nome.setText(usuario.getNome());
                    Conversa conversaAux = conversas.get(contador);
                    conversaAux.setUsuarioExibicao(usuario);

                    if (usuario.getFoto() != null) {
                        Uri uri = Uri.parse(usuario.getFoto());
                        Glide.with(context).load(uri).into(myViewHolder.foto);
                    } else {
                        myViewHolder.foto.setImageResource(R.drawable.foto_padrao);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            myViewHolder.nome.setText(usuario.getNome());

            //tratamento da foto: se não tiver uma foto, usa-se o padrão
            if (usuario.getFoto() != null) {
                Uri uri = Uri.parse(usuario.getFoto());
                Glide.with(context).load(uri).into(myViewHolder.foto);
            } else {
                myViewHolder.foto.setImageResource(R.drawable.foto_padrao);
            }

        } //fim do else

        //tratamento da data: se for no mesmo dia, mostra apenas a hora
        DateTime dataAtual    = new DateTime(new Date());
        DateTime dataConversa = new DateTime(conversa.getUltimaData());

        if (dataAtual.getYear() == dataConversa.getYear() &&
            dataAtual.getMonthOfYear() == dataConversa.getMonthOfYear() &&
            dataAtual.getDayOfMonth() == dataConversa.getDayOfMonth()){

                    DecimalFormat formatoHora = new DecimalFormat("00");

                    String hora, minutos;
                    hora    = formatoHora.format(dataConversa.getHourOfDay());
                    minutos = formatoHora.format(dataConversa.getMinuteOfHour());

                    myViewHolder.data.setText(hora + ":" + minutos);
                    
        }else{
            DecimalFormat formatoDia = new DecimalFormat("00");
            DecimalFormat formatoMes = new DecimalFormat("00");
            DecimalFormat formatoAno = new DecimalFormat("0000");

            String dia, mes, ano;
            dia = formatoDia.format(dataConversa.getDayOfMonth());
            mes = formatoMes.format(dataConversa.getMonthOfYear());
            ano = formatoAno.format(dataConversa.getYear());

            myViewHolder.data.setText(dia + "/" + mes + "/" + ano);
        }

        //setAnimacao(myViewHolder.itemView);
    }

    private void setAnimacao(View view) {
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(DURACAO_ANIMACAO);
        view.startAnimation(anim);
    }

    @Override
    public int getItemCount() {
        return conversas.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        CircleImageView foto;
        TextView nome, ultimaMensagem, mensagensNaoLidas, data;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            foto = itemView.findViewById(R.id.circleImageViewAutorNoticia);
            nome = itemView.findViewById(R.id.textViewNomeContato);
            ultimaMensagem = itemView.findViewById(R.id.textViewEmailContato);
            mensagensNaoLidas = itemView.findViewById(R.id.textViewNaoLidas);
            data = itemView.findViewById(R.id.textViewDataConversa);
        }
    }
}

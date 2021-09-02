package com.projeto.appescola.activity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.app.NotificationManager;
import android.graphics.BitmapFactory;

import com.google.firebase.auth.FirebaseAuth;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.projeto.appescola.R;
import com.projeto.appescola.conifg.ConfiguracaoFirebase;
import com.projeto.appescola.fragment.ContatosFragment;
import com.projeto.appescola.fragment.ConversasFragment;
import com.projeto.appescola.fragment.NoticiasFragment;

import net.danlew.android.joda.JodaTimeAndroid;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;
    private MaterialSearchView searchView;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Inicializa biblioteca do tempo
        JodaTimeAndroid.init(this);

        setContentView(R.layout.activity_main);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao(); //recupera instância do firebase


        //toolbar superior
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle(getResources().getString(R.string.app_name));
        toolbar.setLogo(R.drawable.ic_action_logo);
        setSupportActionBar(toolbar); //adicionado suporte para a toolbar em versãos anteriores do android


        //configurando as abas
        final FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(),
                FragmentPagerItems.with(this)
                        .add("Notícias", NoticiasFragment.class)
                        .add("Conversas", ConversasFragment.class)
                        .add("Contatos", ContatosFragment.class)
                        .create()
        );
        viewPager = findViewById(R.id.viewPager);
        //final ViewPager viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);


        SmartTabLayout viewPageTab = findViewById(R.id.viewPagerTab);
        viewPageTab.setViewPager(viewPager);

        /*Configurar o botão de pesquisa*/
        searchView = findViewById(R.id.materialSearchPrincipal);
        searchView.setHint("Pesquisar");
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() { // ao fechar atualiza as duas pages
                //ConversasFragment conversasFragment = (ConversasFragment) adapter.getPage(1);
                //conversasFragment.atualizarConversas();

                //ContatosFragment contatosFragment = (ContatosFragment) adapter.getPage(2);
                //contatosFragment.atualizarContatos();
            }
        });
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                //Verifica em qual aba estamos pesquisando através do viewPager
                switch (viewPager.getCurrentItem()) {
                    case 0:
                        NoticiasFragment noticiasFragment = (NoticiasFragment) adapter.getPage(0);
                        if (newText != null && !newText.isEmpty()) {
                            noticiasFragment.pesquisarNoticias(newText.toLowerCase());
                        } else {
                            try {
                                noticiasFragment.atualizarNoticias();
                            }catch (Exception e) {
                                Log.i("Erro", "Notícias não atualizadas");
                            }
                        }
                        break;

                    case 1:
                        ConversasFragment conversasFragment = (ConversasFragment) adapter.getPage(1);
                        if (newText != null && !newText.isEmpty()) {
                            conversasFragment.pesquisarConversas(newText.toLowerCase());
                        } else {
                            try {
                                conversasFragment.atualizarConversas();
                            }catch (Exception e) {
                                Log.i("Erro", "Conversas não atualizadas");
                            }
                        }
                        break;

                    case 2:
                        ContatosFragment contatosFragment = (ContatosFragment) adapter.getPage(2);
                        if (newText != null && !newText.isEmpty()) {
                            contatosFragment.pesquisarContatos(newText.toLowerCase());
                        } else {
                            try {
                                contatosFragment.atualizarContatos();
                            }catch (Exception e){
                                Log.i("Erro","Contatos não atualizados");
                            }
                        }
                        break;
                }
                return true;
            }

        });

        //detecta quando trocamos a page e fecha a searchView
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                searchView.closeSearch();
            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

    }



    /*configurações da notificação (rafael)*/
    public void addNotification() {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_notification_large))//.R.drawable.notificacao_large);
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.notificacao_mensagens));

        Intent notificar = new Intent(this, MainActivity.class); //intent para notificar
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificar, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // serviço para notificar
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }


    //se a pesquisa estiver aberta, o botão voltar vai fechá-la
    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else if ((viewPager.getCurrentItem() == 1)||(viewPager.getCurrentItem() == 2)) { // (ver) se estiver em outra aba, volta para conversas (anderson)
            viewPager.setCurrentItem(0);
            //addNotification();//envia um teste de notificação ao ir para contatos e voltar com o back button
        } else { /*se a pesquisa não estiver aberta, o botão voltar retorna a home screen (rafael)*/
            Intent goHome = new Intent(Intent.ACTION_MAIN);
            goHome.addCategory(Intent.CATEGORY_HOME);
            goHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(goHome);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //sobreescrever método responsável pela criação de menus

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        //Configurar o botão de pesquisa
        MenuItem item = menu.findItem(R.id.menuPesquisa);
        searchView.setMenuItem(item);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) { /*método que descobre qual item do menu
                                                                        configuração foi clicado */
        switch (item.getItemId()) {
            case R.id.menuSair:
                deslogarUsuario();
                finish(); //finaliza activity
                break;
            case R.id.menuConfiguracoes:
                abrirConfiguracoes();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    public void deslogarUsuario() {

        try {
            autenticacao.signOut(); //aqui desloga o usuário ativo
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void abrirConfiguracoes() {
        Intent intent = new Intent(MainActivity.this, ConfiguracoesActivity.class);
        startActivity(intent);
    }
}

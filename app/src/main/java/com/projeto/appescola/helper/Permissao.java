package com.projeto.appescola.helper;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Permissao {

    public static boolean validarPermissoes(String[] permissoes, Activity activity, int requestCode) {

        if (Build.VERSION.SDK_INT >= 23) { //só irá realizar esse método em versões superiores à 23
            List<String> listaPermissoes = new ArrayList<>();

            /*Percorre todas as permissões verificando se
             * já tem a permissão liberada ou não*/

            for (String permissao : permissoes) {
                Boolean temPermissao = ContextCompat.checkSelfPermission(activity,
                        permissao) == PackageManager.PERMISSION_GRANTED;
                if (!temPermissao) listaPermissoes.add(permissao);
            }
            /*Caso a lista esteja vazia, não é necessário solicitar permissão*/
            if (listaPermissoes.isEmpty()) return true;

            String[] novasPermissoes = new String[listaPermissoes.size()];
            listaPermissoes.toArray(novasPermissoes);

            /*Solicitando permissões*/
            ActivityCompat.requestPermissions(activity, novasPermissoes,requestCode);
        }

        return true;
    }
}

package br.com.alura.agenda;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import br.com.alura.agenda.fragment.DetalhesProvaFragment;
import br.com.alura.agenda.fragment.ListaAlunosFragment;
import br.com.alura.agenda.modelo.Prova;

public class ProvasActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        Como queremos reaproveitar o Fragment, precisaremos mudar o layout da nossa tela.
        O primeiro elemento que teremos na tela será o FrameLayout.

        Colocamos o FrameLayout e pedimos para ele ocupar o espaço da tela. Ele funciona como o
        placeholder e guarda o lugar em que irá entrar o Fragment. Na Activity, só veremos uma
        moldura vazia e a tela ficará em branco no preview.
         */
        setContentView(R.layout.activity_provas);


        /*
        Para manipularmos qualquer elemento com o Fragment dentro de uma Activity, utilizaremos
        um FragmentManager. E para conseguirmos uma instância dele, usaremos o método
        getSupportFragmentManager() e guardaremos um referência para ele.

        Temos já o nosso especialista em manipular Fragment, temos que informá-lo que ele precisa pegar o
        framelayout e substitui-lo pelo Fragment. Para fazer isto, o FragmentManager pedirá que seja
        aberta uma transação e depois, iremos comitá-la. Também iremos adicionar a variável local
        fragmentTransaction.
         */
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tx = fm.beginTransaction();

        /*
        Pode fazer o replace tanto no arquivo activity_provas.xml (portrait), quanto no
        activity_provas.xml (land). tudo dependente de como a tela está (landscape ou não).
         */
        tx.replace(R.id.frame_principal, new ListaAlunosFragment());

        /*
        Verifica se está no modo paisagem. Caso sim, faz o replace no segundo frame.
        Obs.: criamos dois arquivos com o nome bolls.xml no diretório res/bolls
        Um dos arquivos foi criado normalmente e o outro com a orientação landscape.
        A função desses arquivos é armazenar um boolean que nos informará se a
        tela está em modo paisagem, ou não.
         */
        if(!estaNoModoPortrait()) {
            tx.replace(R.id.frame_secundario, new DetalhesProvaFragment());
        }
        tx.commit();

    }
    private boolean estaNoModoPortrait() { return getResources().getBoolean(R.bool.portrait); }



    public void selecionaProva(Prova prova) {

        FragmentManager fm = getSupportFragmentManager();

        if(estaNoModoPortrait()) {

            FragmentTransaction tx = fm.beginTransaction();

            // passando a prova como parametro para poder realizar o bind
            Bundle params = new Bundle();
            params.putSerializable("prova", prova);

            DetalhesProvaFragment detalhesProvaFragment = new DetalhesProvaFragment();
            detalhesProvaFragment.setArguments(params);

            tx.replace(R.id.frame_principal, detalhesProvaFragment);

            /*
            Quando queremos guardar um estado e termos a opção de retornar a ele com o botão de
            back, teremos que especificar isto dentro da nossa transação.

            O Back Stack é a pilha de retorno, usada pelo botão de back no Android. Porém, apenas
            activities entram nesta pilha, pelo menos, até agora. Com este método, podemos
            adicionar uma transação nesta pilha.

            Dentro do addToBackStack, poderíamos voltar a algum estado específico anterior que
            tivesse uma identificação. No nosso caso, preferimos usar o valor null.
             */
            tx.addToBackStack(null);

            tx.commit();
        } else {
            // como esse fragment já foi criado na tela no modo landscape, precisamos nos preocupar somente com o BIND
            DetalhesProvaFragment detalhesProvaFragment = (DetalhesProvaFragment) fm.findFragmentById(R.id.frame_secundario);
            detalhesProvaFragment.populaCampos(prova);
        }
    }
}

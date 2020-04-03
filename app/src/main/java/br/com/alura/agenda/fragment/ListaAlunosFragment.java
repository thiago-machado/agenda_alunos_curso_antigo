package br.com.alura.agenda.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Arrays;
import java.util.List;

import br.com.alura.agenda.DetalhesProvaActivity;
import br.com.alura.agenda.ProvasActivity;
import br.com.alura.agenda.R;
import br.com.alura.agenda.modelo.Prova;

/**
 * Fragment representará um pedaço da tela tanto o layout como o comportamento.
 * O Fragmente tem seu próprio layout XML.
 */
public class ListaAlunosFragment extends Fragment {

    /*
    O Fragment entrará na tela dentro de uma Activity, teremos ainda que construir a View que irá representá-lo.
    Podemos ver vários atributos que iremos buscar dentro. Em seguida, colocaremos a View que irá mostrar
    o layout que está no arquivo activity_provas.xml.

     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        /*
        Chamaremos o inflater e dentro dos parênteses iremos especificar um layout, no nosso caso,
        o fragment_lista_provase depois, o container que ocupará o função de pai.
        Mas não queremos que ele ocupe este papel ainda, então, adicionaremos o false.

        Agora, ele inflou o layout e devolveu a tela montada. Precisaremos, em seguida, popular os campos.
        Já fizemos isto anteriormente, logo iremos utilizar o que temos pronto.
        */



        View view = inflater.inflate(R.layout.fragment_lista_alunos, container, false);
        List<String> topicosPort = Arrays.asList("Sujeito", "Objeto direto", "Objeto indireto");
        Prova provaPortugues = new Prova("Portugues", "25/05/2016", topicosPort);
        List<String> topicosMat = Arrays.asList("Equacoes de segundo grau", "Trigonometria");
        Prova provaMatematica = new Prova("Matematica", "27/05/2016", topicosMat);
        List<Prova> provas = Arrays.asList(provaPortugues, provaMatematica);


        /*
         Onde utilizamos this, o Fragment não é considerado um contexto, mas ele tem um método
         que consegue trazer o contexto que está associado a ele. Substituiremos o this pelo
         método getContext().
         */
        ArrayAdapter<Prova> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, provas);



        /*
        Ele reclamará que não tem findViewById no Fragment, porque ele não tem uma View por padrão.
        Nós a estamos construindo agora, e teremos que utilizá-la para localizar os componentes que
        estão na nossa tela. Quando quisermos procurar a lista, não buscaremos no Fragment,
        mas sim, na View.
         */
        ListView lista = view.findViewById(R.id.provas_lista);
        lista.setAdapter(adapter);



        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Prova prova = (Prova) parent.getItemAtPosition(position);
                ProvasActivity provasActivity = (ProvasActivity) getActivity();
                provasActivity.selecionaProva(prova);
            }
        });

        return view;
    }
}

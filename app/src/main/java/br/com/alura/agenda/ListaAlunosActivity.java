package br.com.alura.agenda;

import android.Manifest;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import br.com.alura.agenda.adaper.AlunosAdapter;
import br.com.alura.agenda.async.EnviaAlunosTask;
import br.com.alura.agenda.dao.AlunoDAO;
import br.com.alura.agenda.event.AtualizaListaAlunoEvent;
import br.com.alura.agenda.modelo.Aluno;
import br.com.alura.agenda.modelo.dto.AlunosSync;
import br.com.alura.agenda.retrofit.RetrofitInicializador;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListaAlunosActivity extends AppCompatActivity {

    private ListView listaAlunos;
    private SwipeRefreshLayout swipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_alunos);

        /*
        Tornando ListaAlunosActivity um subscriber (ouvinte) de eventos.
        Estamos registrando a própria Activity como "ouvinte" dos eventos.

        Olhar método atualizaListaAlunoEvent() para consultar a implementaação do que
        será executado quando receber a chamada do evento.
         */
        EventBus eventBus = EventBus.getDefault();
        eventBus.register(this);

        listaAlunos = findViewById(R.id.lista_alunos);

        configurarSwipe();

        listaAlunos.setOnItemClickListener((adapterView, view, position, id) -> {
            Aluno aluno = (Aluno) listaAlunos.getItemAtPosition(position);
            Intent intentVaiProFormulario = new Intent(ListaAlunosActivity.this, FormularioActivity.class);
            intentVaiProFormulario.putExtra("aluno", aluno);
            startActivity(intentVaiProFormulario);
        });

        Button novoAluno = findViewById(R.id.novo_aluno);
        novoAluno.setOnClickListener(view -> {
            Intent intentVaiProFormulario = new Intent(ListaAlunosActivity.this, FormularioActivity.class);
            startActivity(intentVaiProFormulario);
        });

        registerForContextMenu(listaAlunos);
        buscaAlunosNaAPI();
    }

    /*
    Com o register, a classe ListaAlunosActivity já é capaz de receber as notificações,
    mas ela ainda não reage aos eventos. Para isso, criaremos um método anotado com
    @Subscribe que, por meio do parâmetro do método, indicaremos qual evento estaremos aguardando.

    NOTA: para podermos atualizar a Activity, é necessário que estejamos na Thread MAIN.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void atualizaListaAlunoEvent(AtualizaListaAlunoEvent event){
        carregaLista();
    }

    private void configurarSwipe() {
        swipe = findViewById(R.id.swipe_lista_alunos);
        swipe.setOnRefreshListener(() -> buscaAlunosNaAPI());
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregaLista();
    }

    private void buscaAlunosNaAPI() {
        Call<AlunosSync> call = new RetrofitInicializador().getAlunoService().lista();
        call.enqueue(new Callback<AlunosSync>() {
            @Override
            public void onResponse(Call<AlunosSync> call, Response<AlunosSync> response) {
                List<Aluno> alunos = response.body().getAlunos();
                AlunoDAO dao = new AlunoDAO(ListaAlunosActivity.this);
                dao.sincroniza(alunos);
                dao.close();
                carregaLista();
                swipe.setRefreshing(false); // SUMINDO COM O GIF DE REFRESH DO SWIPE
            }

            @Override
            public void onFailure(Call<AlunosSync> call, Throwable t) {
                Log.e("get_alunos", "FALHOU A BUSCA DOS ALUNOS...", t);
                swipe.setRefreshing(false);
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, final ContextMenu.ContextMenuInfo menuInfo) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        final Aluno aluno = (Aluno) listaAlunos.getItemAtPosition(info.position);
        criarMenuItemSite(menu, aluno);
        criarMenuItemDeletar(menu, aluno);

        criarMenuItemSMS(menu, aluno);
        criarMenuItemMapa(menu, aluno);

        criarMenuItemLigar(menu, aluno);
    }

    private void criarMenuItemLigar(ContextMenu menu, final Aluno aluno) {
        MenuItem itemLigar = menu.add("Ligar");
        itemLigar.setOnMenuItemClickListener(item -> {

            // Caso o usuário ainda não tenha dado a permissão para fazer ligação, entra no if pedindo a permissão
            if (ActivityCompat.checkSelfPermission(ListaAlunosActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ListaAlunosActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 100);
            } else {
                Intent intentLigar = new Intent(Intent.ACTION_CALL);
                intentLigar.setData(Uri.parse("tel:" + aluno.getTelefone()));
                startActivity(intentLigar);
            }
            return false;
        });

    }

    private void criarMenuItemSMS(ContextMenu menu, Aluno aluno) {
        MenuItem itemMapa = menu.add("Enviar SMS");
        Intent intentMapa = new Intent(Intent.ACTION_VIEW);
        intentMapa.setData(Uri.parse("sms:" + aluno.getTelefone()));
        itemMapa.setIntent(intentMapa);
    }

    private void criarMenuItemMapa(ContextMenu menu, Aluno aluno) {
        MenuItem itemSMS = menu.add("Visualizar no mapa");
        Intent intentSMS = new Intent(Intent.ACTION_VIEW);
        intentSMS.setData(Uri.parse("geo:0,0?q=" + aluno.getEndereco()));
        itemSMS.setIntent(intentSMS);
    }

    private void criarMenuItemDeletar(ContextMenu menu, final Aluno aluno) {
        MenuItem deletar = menu.add("Deletar");
        deletar.setOnMenuItemClickListener(menuItem -> {

            Call<Void> call = new RetrofitInicializador().getAlunoService().deleta(aluno.getId());

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    Log.i("removendoaluno", "removendo...");
                    AlunoDAO dao = new AlunoDAO(ListaAlunosActivity.this);
                    dao.deleta(aluno);
                    dao.close();
                    carregaLista();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(ListaAlunosActivity.this, "Nao foi possível remover o aluno", Toast.LENGTH_SHORT).show();
                }
            });
            return false;
        });
    }

    private void criarMenuItemSite(ContextMenu menu, Aluno aluno) {
        /*
        Criação de uma Intent implícita. Isso é útil quando queremos utilizar
        um recurso já existente no celular, como um Browser por exemplo.
        Isso também é útil com imagens e documentos.
         */
        MenuItem itemSite = menu.add("Visitar site");
        Intent intentSite = new Intent(Intent.ACTION_VIEW); // Delego para o android decidir o que fazer com a ação (abrir browser, imagem e etc)

        /*
        Para que um site seja aberto, é necessário que o protocolo esteja definido no
        começo da URL.
        Caso fosse uma imagem, documento e etc, teríamos que informar qual o prefixo
        do recurso que estamos querendo visualizar.
         */
        String site = aluno.getSite();
        if (!site.startsWith("https://")) {
            site = "https://" + site;
        }

        intentSite.setData(Uri.parse(site)); // Obrigatório utilizar esse método para dizer o que deseja visualizar.
        itemSite.setIntent(intentSite); // Dessa maneira, quando ocorrer um clique, o Android saberá que deverá executar uma ação!
    }

    private void carregaLista() {
        AlunoDAO dao = new AlunoDAO(this);
        List<Aluno> alunos = dao.buscaAlunos();
        dao.close();

        AlunosAdapter adapter = new AlunosAdapter(this, alunos);
        listaAlunos.setAdapter(adapter);
    }

    /*
    Toda solicitação de permissão que é aceita ou negada, cai nesse método.
    Podemos tomar alguma atitude quanto a isso.
    No nosso caso, não iremos fazer nada.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            Log.i("resultado_permissao", "resultado da permissao da ligacao...");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lista_alunos, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_enviar_notas:
                new EnviaAlunosTask(this).execute();
                break;

            case R.id.menu_baixar_provas:
                Intent vaiParaProvas = new Intent(this, ProvasActivity.class);
                startActivity(vaiParaProvas);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

package br.com.alura.agenda;

import android.Manifest;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import br.com.alura.agenda.adaper.AlunosAdapter;
import br.com.alura.agenda.async.EnviaAlunosTask;
import br.com.alura.agenda.converter.AlunoConverter;
import br.com.alura.agenda.dao.AlunoDAO;
import br.com.alura.agenda.modelo.Aluno;
import br.com.alura.agenda.modelo.dto.AlunosSync;
import br.com.alura.agenda.retrofit.RetrofitInializador;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListaAlunosActivity extends AppCompatActivity {

    private ListView listaAlunos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_alunos);

        listaAlunos = findViewById(R.id.lista_alunos);

        listaAlunos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Aluno aluno = (Aluno) listaAlunos.getItemAtPosition(position);
                Intent intentVaiProFormulario = new Intent(ListaAlunosActivity.this, FormularioActivity.class);
                intentVaiProFormulario.putExtra("aluno", aluno);
                startActivity(intentVaiProFormulario);
            }
        });

        Button novoAluno = (Button) findViewById(R.id.novo_aluno);
        novoAluno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentVaiProFormulario = new Intent(ListaAlunosActivity.this, FormularioActivity.class);
                startActivity(intentVaiProFormulario);
            }
        });

        registerForContextMenu(listaAlunos);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Call<AlunosSync> call = new RetrofitInializador().getAlunoService().lista();
        call.enqueue(new Callback<AlunosSync>() {
            @Override
            public void onResponse(Call<AlunosSync> call, Response<AlunosSync> response) {
                List<Aluno> alunos = response.body().getAlunos();
                AlunoDAO dao = new AlunoDAO(ListaAlunosActivity.this);
                dao.sincroniza(alunos);
                dao.close();
                carregaLista();
            }

            @Override
            public void onFailure(Call<AlunosSync> call, Throwable t) {
                Log.e("get_alunos", "FALHOU A BUSCA DOS ALUNOS...", t);
            }
        });

        carregaLista();
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
        itemLigar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                // Caso o usuário ainda não tenha dado a permissão para fazer ligação, entra no if pedindo a permissão
                if (ActivityCompat.checkSelfPermission(ListaAlunosActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ListaAlunosActivity.this, new String[] {Manifest.permission.CALL_PHONE}, 100);
                } else {
                    Intent intentLigar = new Intent(Intent.ACTION_CALL);
                    intentLigar.setData(Uri.parse("tel:" + aluno.getTelefone()));
                    startActivity(intentLigar);
                }
                return false;
            }
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
        deletar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                Toast.makeText(ListaAlunosActivity.this, "Deletar o aluno " + aluno.getNome(), Toast.LENGTH_SHORT).show();

                AlunoDAO dao = new AlunoDAO(ListaAlunosActivity.this);
                dao.deleta(aluno);
                dao.close();

                carregaLista();
                return false;
            }
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

        //ArrayAdapter<Aluno> adapter = new ArrayAdapter<Aluno>(this, android.R.layout.simple_list_item_1, alunos);
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

        if(requestCode == 100) {
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

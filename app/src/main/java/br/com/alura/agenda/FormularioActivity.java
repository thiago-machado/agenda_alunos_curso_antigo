package br.com.alura.agenda;

import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

import br.com.alura.agenda.dao.AlunoDAO;
import br.com.alura.agenda.modelo.Aluno;
import br.com.alura.agenda.retrofit.RetrofitInicializador;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FormularioActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 200;
    private FormularioHelper helper;
    private String caminhoFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario);

        helper = new FormularioHelper(this);

        Intent intent = getIntent();
        Aluno aluno = (Aluno) intent.getSerializableExtra("aluno");

        if (aluno != null) {
            helper.preencheFormulario(aluno);
        }

        Button botaoFoto = findViewById(R.id.formulario_botao_foto);
        botaoFoto.setOnClickListener((view) -> {

            Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            caminhoFoto = getExternalFilesDir("fotos") + "/" + System.currentTimeMillis() + ".jpg";

            File arquivoFoto = new File(caminhoFoto);

            intentCamera.putExtra(MediaStore.EXTRA_OUTPUT,
                    FileProvider.getUriForFile(FormularioActivity.this,
                            BuildConfig.APPLICATION_ID + ".provider", arquivoFoto));
            startActivityForResult(intentCamera, REQUEST_CODE);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_formulario, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_formulario_ok:
                Aluno aluno = helper.getAluno();
                aluno.desincroniza();

                AlunoDAO dao = new AlunoDAO(this);

                if (aluno.getId() != null) {
                    dao.altera(aluno);
                } else {
                    // COMO NÃO USAMOS MAIS UM ID DO TIPO LONG, NÃO PRECISAMOS MAIS USAR O RETORNO DOS INSERTS
                    //long id = dao.insere(aluno);
                    //aluno.setId(id);
                    dao.insere(aluno);
                }

                dao.close();

                //new InsereAlunoTask(aluno).execute();

                integrarViaAPI(aluno);

                Toast.makeText(FormularioActivity.this, "Aluno " + aluno.getNome() + " salvo!", Toast.LENGTH_SHORT).show();
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void integrarViaAPI(Aluno aluno) {
        Call call = new RetrofitInicializador().getAlunoService().insere(aluno);
        call.enqueue(new Callback() {

            @Override
            public void onResponse(Call call, Response response) {
                Log.i("retrofitrequisicao", "OK...");
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Log.e("retrofitrequisicao", "FALHOR...", t);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            helper.carregaImagem(caminhoFoto);
        }
    }
}
